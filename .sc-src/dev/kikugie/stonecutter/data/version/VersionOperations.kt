package dev.kikugie.stonecutter.data.version

import dev.kikugie.commons.result.inherit
import dev.kikugie.commons.result.mapResult
import dev.kikugie.commons.takeAs
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.semver.impl.VersionParsingException
import dev.kikugie.stonecutter.AnyVersion
import java.util.concurrent.ConcurrentHashMap

/**Marker interface for extensions supporting version-based conditional evaluation.*/
public interface VersionOperations<T : Version> {
    /**
     * Parses the provided [version] string as the [T] version.
     * @throws IllegalArgumentException if parsing fails, with [VersionParsingException] as its cause
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     */
    public fun parse(version: AnyVersion): T

    /**
     * Tries to parse the provided [version] string as the [T] version.
     * @return [Result] with [VersionParsingException] if parsing fails.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     */
    public fun tryParse(version: AnyVersion): Result<T>

    /**
     * Checks if the provided [version] satisfies the [predicates].
     *
     * Predicates can be separated with spaces in the same string or provided as separate strings:
     * ```kt
     * val version = parse("1.1")
     * assert(eval(version, ">1.0", "<2.0"))
     * assert(eval(version, ">1.0 <2.0"))
     * ```
     * @throws IllegalArgumentException if parsing fails, with [VersionParsingException] as its cause
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-predicates">Predicate specification</a>
     */
    public fun eval(version: Version, vararg predicates: String): Boolean

    /**
     * Checks if the provided [version] satisfies the [predicates].
     *
     * Predicates can be separated with spaces in the same string or provided as separate strings:
     * ```kt
     * val version = parse("1.1")
     * assert(tryEval(version, ">1.0", "<2.0").getOrThrow())
     * assert(tryEval(version, ">1.0 <2.0").getOrThrow())
     * ```
     * @return [Result] with [VersionParsingException] if parsing fails
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-predicates">Predicate specification</a>
     */
    public fun tryEval(version: Version, vararg predicates: String): Result<Boolean>

    /**
     * Checks if the provided [version] satisfies the [predicates].
     *
     * Predicates can be separated with spaces in the same string or provided as separate strings:
     * ```kt
     * assert(eval("1.1", ">1.0", "<2.0"))
     * assert(eval("1.1", ">1.0 <2.0"))
     * ```
     * @throws IllegalArgumentException if parsing fails, with [VersionParsingException] as its cause
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-predicates">Predicate specification</a>
     */
    public fun eval(version: String, vararg predicates: String): Boolean

    /**
     * Checks if the provided [version] satisfies the [predicates].
     *
     * Predicates can be separated with spaces in the same string or provided as separate strings:
     * ```kt
     * assert(tryEval("1.1", ">1.0", "<2.0").getOrThrow())
     * assert(tryEval("1.1", ">1.0 <2.0").getOrThrow())
     * ```
     * @return [Result] with [VersionParsingException] if parsing fails
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-predicates">Predicate specification</a>
     */
    public fun tryEval(version: String, vararg predicates: String): Result<Boolean>


    /**
     * Parses each version as [T] and compares them
     * according to the [Comparable] specification.
     * @throws IllegalArgumentException if parsing fails, with [VersionParsingException] as its cause
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     */
    public fun compare(version: String, other: String): Int

    /**
     * Parses each version as [T] and compares them
     * according to the [Comparable] specification.
     * @return [Result] with [VersionParsingException] if parsing fails
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params#version-types">Version specification</a>
     */
    public fun tryCompare(version: String, other: String): Result<Int>
}

internal open class BasicOperations<T : Version>(
    val vops: Version.Operations,
    val pops: VersionPredicate.Operations, // NOT "father"
) : VersionOperations<T> {
    protected val cache: MutableMap<String, Version> = ConcurrentHashMap()
    override fun parse(version: AnyVersion): T = tryParse(version).getOrElse {
        it as VersionParsingException
        throw it.asIllegalArg(version)
    }

    override fun tryParse(version: AnyVersion): Result<T> =
        cache.getOrParse(version, vops).takeAs()

    override fun eval(version: Version, vararg predicates: String): Boolean = tryEval(version, *predicates).getOrElse {
        it as VersionParsingException
        // What the fuck am I cooking here
        val position = kotlin.run {
            var sum = 0
            it.position + 2 * predicates.count { p -> (sum + p.length <= it.position).also { sum += p.length } }
        }
        throw it.asIllegalArg(predicates.joinToString(), position = position)
    }

    override fun tryEval(version: Version, vararg predicates: String): Result<Boolean> =
        kotlin.runCatching { predicates.unpack(pops).all { it(version) } }

    override fun eval(version: String, vararg predicates: String): Boolean =
        eval(parse(version), *predicates)

    override fun tryEval(version: String, vararg predicates: String): Result<Boolean> =
        tryParse(version).mapResult { tryEval(it as Version, *predicates) }

    override fun compare(version: String, other: String): Int =
        parse(version) compareTo parse(other)

    override fun tryCompare(version: String, other: String): Result<Int> =
        // Is this what functional programming shills dream of?
        tryParse(version).mapResult { a -> tryParse(other).map { b -> a compareTo b } }
}

internal object LenientOperations : BasicOperations<Version>(Version, VersionPredicate)

internal object SemanticOperations : BasicOperations<SemanticVersion>(SemanticVersion, VersionPredicate.Semantic)

@Suppress("NOTHING_TO_INLINE")
internal inline fun VersionParsingException.asIllegalArg(str: String, message: String = this.message, position: Int = this.position): IllegalArgumentException {
    val pointer = " ".repeat(position) + '^'
    val message = "$message:\n| $str\n| $pointer"
    return IllegalArgumentException(message).apply { addSuppressed(this@asIllegalArg) }
}

private fun MutableMap<AnyVersion, Version>.getOrParse(version: AnyVersion, ops: Version.Operations): Result<Version> =
    kotlin.runCatching { computeIfAbsent(version, ops::parse) }

private fun Array<out String>.unpack(ops: VersionPredicate.Operations): List<VersionPredicate> = buildList {
    var length = 0
    for (str in this@unpack) {
        var cursor = 0
        while (cursor < str.length) when(str[cursor]) {
            ' ', '\t' -> cursor++
            else -> cursor = parsePredicate(str, cursor, length, ops, this)
        }
        length += str.length
    }
}

private fun parsePredicate(str: String, pos: Int, len: Int, ops: VersionPredicate.Operations, list: MutableList<VersionPredicate>): Int {
    val end = ops.locate(str, pos)
    if (end < 0) throw VersionParsingException("Not a valid predicate", pos)

    list += ops.parse(str.substring(pos, end)).getOrElse {
        it as VersionParsingException
        throw VersionParsingException(it.message, it.position + pos + len).inherit(it)
    }
    return end
}
