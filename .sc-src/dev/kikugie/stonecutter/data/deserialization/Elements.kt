@file:OptIn(ExperimentalContracts::class)

package dev.kikugie.stonecutter.data.deserialization

import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import dev.eav.tomlkt.TomlElement
import dev.kikugie.commons.takeAs
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.util.escape
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@DslMarker @Retention(AnnotationRetention.BINARY)
private annotation class SCStruct

/**
 * Represents a sealed hierarchical data structure used for de/serialization.
 *
 * Whenever Stonecutter has to deserialize data, it is first converted to this type
 * to avoid format-specific differences.
 *
 * The element itself is not serializable by design as it doesn't represent any
 * format but a common structure among them.
 *
 * In contrast to generic [Map<*, *>][Map], [List<*>][List] and [Any?][Any],
 * it ensures a set of valid types and provides a [Visitor] interface to navigate them.
 */
@SCStruct
public sealed class SCElement {
    /**
     * Underlying data; depends on the subclass.
     */
    public abstract val content: Any?

    /**
     * Returns the [String] representation of the element.
     *
     * The implementations of this method recursively traverse the structure.
     */
    public abstract override fun toString(): String

    /**
     * Submits the element to the visitor.
     *
     * *What kind of roleplay is this?*
     */
    public abstract fun <C, T> accept(visitor: Visitor<C, T>, ctx: C): T

    /**
     * Casts this element as a [SCPrimitive].
     * @throws ClassCastException If it's not a [SCPrimitive]
     */
    public fun asPrimitive(): SCPrimitive {
        contract { returns() implies (this@SCElement is SCPrimitive) }
        return takeAs()
    }

    /**
     * Casts this element as a [SCPrimitive] or returns `null` otherwise
     */
    public fun asPrimitiveOrNull(): SCPrimitive? {
        contract { returnsNotNull() implies (this@SCElement is SCPrimitive) }
        return takeAsOrNull()
    }

    /**
     * Casts this element as a [SCList].
     * @throws ClassCastException If it's not a [SCList]
     */
    public fun asList(): SCList {
        contract { returns() implies (this@SCElement is SCList) }
        return takeAs()
    }

    /**
     * Casts this element as a [SCList] or returns `null` otherwise
     */
    public fun asListOrNull(): SCList? {
        contract { returnsNotNull() implies (this@SCElement is SCList) }
        return takeAsOrNull()
    }

    /**
     * Casts this element as a [SCMap].
     * @throws ClassCastException If it's not a [SCMap]
     */
    public fun asMap(): SCMap {
        contract { returns() implies (this@SCElement is SCMap) }
        return takeAs()
    }

    /**
     * Casts this element as a [SCNull] or returns `null` otherwise
     */
    public fun asMapOrNull(): SCMap? {
        contract { returnsNotNull() implies (this@SCElement is SCMap) }
        return takeAsOrNull()
    }

    /**
     * Converts this element to [kotlinx.serialization.json](https://github.com/Kotlin/kotlinx.serialization)
     * element using the [JsonConverter].
     *
     * The types are mapped as follows:
     * - [SCNull] -> [JsonNull][kotlinx.serialization.json.JsonNull]
     * - [SCPrimitive] -> [JsonPrimitive][kotlinx.serialization.json.JsonPrimitive]
     * - [SCList] -> [JsonArray][kotlinx.serialization.json.JsonArray]
     * - [SCMap] -> [JsonObject][kotlinx.serialization.json.JsonObject]
     *
     * [String] [SCPrimitive]s are quoted when converted to Json.
     */
    public fun toJson(): JsonElement = accept(JsonConverter, null)

    /**
     * Converts this element to a [dev.eav.tomlkt](https://github.com/eav-eav-eav/tomlkt)
     * element using the [TomlConverter].
     *
     * The types are mapped as follows:
     * - [SCNull] -> [TomlNull][dev.eav.tomlkt.TomlNull]
     * - [SCPrimitive] -> [TomlLiteral][dev.eav.tomlkt.TomlLiteral]
     * - [SCList] -> [TomlArray][dev.eav.tomlkt.TomlArray]
     * - [SCMap] -> [TomlTable][dev.eav.tomlkt.TomlTable]
     *
     * Note that datetime elements are not supported and will be represented as
     * [TomlLiteral.Type.String][dev.eav.tomlkt.TomlLiteral.Type.String] when converted.
     */
    public fun toToml(): TomlElement = accept(TomlConverter, null)

    /**
     * Converts this element to [com.charleskorn.kaml](https://github.com/charleskorn/kaml)
     * element using the [YamlConverter].
     *
     * The types are mapped as follows:
     * - [SCNull] -> [YamlNull][com.charleskorn.kaml.YamlNull]
     * - [SCPrimitive] -> [YamlScalar][com.charleskorn.kaml.YamlScalar]
     * - [SCList] -> [YamlList][com.charleskorn.kaml.YamlList]
     * - [SCMap] -> [YamlList][com.charleskorn.kaml.YamlMap]
     *
     * The conversion process creates [YamlPath]s for the entries;
     * however, the path's [location][com.charleskorn.kaml.YamlPathSegment.location]
     * is always line 1 and column 1.
     */
    public fun toYaml(): YamlNode = accept(YamlConverter, YamlPath.root)

    /**
     * Converts this element to [T] using its [DeserializationStrategy].
     *
     * Deserialization is done with [Json][kotlinx.serialization.json.Json],
     * allowing the use of [JsonContentPolymorphicSerializer][kotlinx.serialization.json.JsonContentPolymorphicSerializer]s
     * and [JsonTransformingSerializer][kotlinx.serialization.json.JsonTransformingSerializer]s.
     *
     * @throws [SerializationException] If the element is not serializable
     * @throws [IllegalArgumentException] If the element can't be represented as [T] or contains star projections (i.e. `List<*>`)
     */
    public inline fun <reified T> to(): T =
        JsonConverter.SPEC.decodeFromJsonElement(toJson())

    /**
     * Converts this element to [T] using its [deserializer].
     *
     * Deserialization is done with [Json][kotlinx.serialization.json.Json],
     * allowing the use of [JsonContentPolymorphicSerializer][kotlinx.serialization.json.JsonContentPolymorphicSerializer]s
     * and [JsonTransformingSerializer][kotlinx.serialization.json.JsonTransformingSerializer]s.
     *
     * @throws [SerializationException] If the element is not serializable
     * @throws [IllegalArgumentException] If the element can't be represented as [T] or contains star projections (i.e. `List<*>`)
     */
    public fun <T> to(deserializer: DeserializationStrategy<T>): T =
        JsonConverter.SPEC.decodeFromJsonElement(deserializer, toJson())

    /**
     * Visitor interface for [SCElement] traversal.
     *
     * @param C The context type to be passed and used during the visit
     * @param T The return type of the visit result
     */
    public interface Visitor<C, T> {
        public fun visitNull(ctx: C): T
        public fun visitPrimitive(prim: SCPrimitive, ctx: C): T
        public fun visitList(list: SCList, ctx: C): T
        public fun visitMap(map: SCMap, ctx: C): T
    }
}

@SCStruct
public object SCNull : SCElement() {
    /**
     * It is `null`.
     */
    override val content: Nothing? get() = null

    override fun toString(): String = "null"
    override fun <C, T> accept(visitor: Visitor<C, T>, ctx: C): T = visitor.visitNull(ctx)
}

/**
 * Represents a primitive value within the sealed data structure.
 *
 * @param content A [Boolean], [Long], [Double], or [String].
 * @param kind Descriptor of the [content]'s type
 */
@SCStruct
public class SCPrimitive private constructor(override val content: Any, public val kind: Kind) : SCElement() {
    public constructor(value: Boolean) : this(value, Kind.Boolean)
    public constructor(value: Int) : this(value.toLong(), Kind.Integer)
    public constructor(value: Long) : this(value, Kind.Integer)
    public constructor(value: Float) : this(value.toDouble(), Kind.Float)
    public constructor(value: Double) : this(value, Kind.Float)
    public constructor(value: CharSequence) : this(value.toString(), Kind.String)
    public constructor(value: String) : this(value, Kind.String)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SCPrimitive) return false

        if (content != other.content) return false
        if (kind != other.kind) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + kind.hashCode()
        return result
    }

    /**
     * Returns the [content] as a [String].
     */
    override fun toString(): String = content.toString()
    override fun <C, T> accept(visitor: Visitor<C, T>, ctx: C): T = visitor.visitPrimitive(this, ctx)

    /**
     * Returns this primitive as a [Boolean].
     * @throws IllegalStateException If [kind] is not [Kind.Boolean]
     */
    public fun toBoolean(): Boolean = of<Boolean>(Kind.Boolean)

    /**
     * Returns this primitive as a [Boolean] or `null` [kind] is not [Kind.Boolean] .
     */
    public fun toBooleanOrNull(): Boolean? = content as? Boolean

    /**
     * Returns this primitive as an [Int].
     * @throws IllegalStateException If [kind] is not [Kind.Integer]
     */
    public fun toInt(): Int = of<Long>(Kind.Integer).toInt()

    /**
     * Returns this primitive as an [Int] or `null` [kind] is not [Kind.Integer] .
     */
    public fun toIntOrNull(): Int? = content.takeAsOrNull<Long>()?.toInt()

    /**
     * Returns this primitive as a [Long].
     * @throws IllegalStateException If [kind] is not [Kind.Integer]
     */
    public fun toLong(): Long = of(Kind.Integer)

    /**
     * Returns this primitive as a [Long] or `null` [kind] is not [Kind.Integer] .
     */
    public fun toLongOrNull(): Long? = content as? Long

    /**
     * Returns this primitive as a [Float].
     * @throws IllegalStateException If [kind] is not [Kind.Float]
     */
    public fun toFloat(): Float = of<Double>(Kind.Float).toFloat()

    /**
     * Returns this primitive as a [Float] or `null` [kind] is not [Kind.Float] .
     */
    public fun toFloatOrNull(): Float? = content.takeAsOrNull<Double>()?.toFloat()

    /**
     * Returns this primitive as a [Double].
     * @throws IllegalStateException If [kind] is not [Kind.Float]
     */
    public fun toDouble(): Double = of(Kind.Float)

    /**
     * Returns this primitive as a [Double] or `null` [kind] is not [Kind.Float] .
     */
    public fun toDoubleOrNull(): Double? = content as? Double

    private inline fun <reified T> of(kind: Kind): T {
        check(kind == this.kind) { "Value ${content::class.simpleName} is not of kind $kind" }
        return content as T
    }

    /**
     * Represents the exhaustive variants of the [SCPrimitive.content].
     */
    public enum class Kind {
        /**Matches the [Boolean][kotlin.Boolean] type.*/
        Boolean,
        /**Matches the [Int][kotlin.Int] and [Long][kotlin.Long] types.*/
        Integer,
        /**Matches the [Float][kotlin.Float] and [Double][kotlin.Double] types.*/
        Float,
        /**Matches the [String][kotlin.String] type.*/
        String
    }
}

@SCStruct
@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
public class SCList(override val content: List<SCElement>) : SCElement(), List<SCElement> by content {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SCList) return false

        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    /**
     * Returns the [content] as `[item1, item2, ...]`.
     */
    override fun toString(): String = content.joinToString(prefix = "[", postfix = "]")
    override fun <C, T> accept(visitor: Visitor<C, T>, ctx: C): T = visitor.visitList(this, ctx)
}

@SCStruct
public class SCMap(override val content: Map<String, SCElement>) : SCElement(), Map<String, SCElement> by content {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SCMap) return false

        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    /**
     * Returns the [content] as `{key1: value1, key2: value2, ...}`
     */
    override fun toString(): String = content.entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "${k.escape()}: $v" }
    override fun <C, T> accept(visitor: Visitor<C, T>, ctx: C): T = visitor.visitMap(this, ctx)
}
