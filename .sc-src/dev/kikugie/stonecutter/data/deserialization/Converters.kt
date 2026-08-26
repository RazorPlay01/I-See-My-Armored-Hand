package dev.kikugie.stonecutter.data.deserialization

import com.charleskorn.kaml.*
import dev.eav.tomlkt.*
import dev.kikugie.commons.collections.present
import dev.kikugie.stonecutter.data.deserialization.SCPrimitive.Kind
import dev.kikugie.stonecutter.util.SCJSON
import dev.kikugie.stonecutter.util.unescape
import kotlinx.serialization.json.*
import java.io.File
import java.util.*

internal val SUPPORTED_CONVERTIBLE: List<String> = listOf("json", "json5", "yml", "yaml", "toml")

internal inline fun <reified T> SCConverter.deserialize(text: String): T = when (this) {
    is JsonConverter -> SPEC.decodeFromString(text)
    else -> JsonConverter.SPEC.decodeFromJsonElement(convert(text).toJson())
}

internal fun File.defaultConverterOrNull(): SCConverter? = when (extension.lowercase(Locale.US)) {
    "json", "json5" -> JsonConverter
    "yml", "yaml" -> YamlConverter
    "toml" -> TomlConverter
    else -> null
}

internal fun determineConverter(
    file: File,
    docs: String = "https://stonecutter.kikugie.dev/wiki/config/projects"
): SCConverter = requireNotNull(file.defaultConverterOrNull()) {
    """
    file://${file.absolutePath} is not of a supported format
    - Hint: use one of ${SUPPORTED_CONVERTIBLE.present()} formats or provide a custom converter
    - Docs: $docs
    """.trimIndent()
}

public fun interface SCConverter {
    public fun convert(text: String): SCElement
}

public object JsonConverter : SCConverter, SCElement.Visitor<Nothing?, JsonElement> {
    @JvmField public val SPEC: Json = SCJSON

    override fun visitNull(ctx: Nothing?): JsonNull = JsonNull

    override fun visitPrimitive(prim: SCPrimitive, ctx: Nothing?): JsonPrimitive = when (prim.kind) {
        Kind.String -> JsonPrimitive(prim.toString())
        else -> JsonUnquotedLiteral(prim.toString())
    }

    override fun visitList(list: SCList, ctx: Nothing?): JsonArray =
        JsonArray(list.map { it.accept(this, null) })

    override fun visitMap(map: SCMap, ctx: Nothing?): JsonObject =
        JsonObject(map.mapValues { (_, it) -> it.accept(this, null) })

    override fun convert(text: String): SCElement {
        val obj: JsonElement = SPEC.decodeFromString(text)
        return visitElement(obj)
    }

    private fun visitElement(element: JsonElement): SCElement = when (element) {
        JsonNull -> SCNull
        is JsonPrimitive -> element.parseToLiteral()
        is JsonArray -> SCList(element.map(::visitElement))
        is JsonObject -> SCMap(element.mapValues { (_, it) -> visitElement(it) })
    }

    private fun JsonPrimitive.parseToLiteral(): SCPrimitive =
        if (isString) SCPrimitive(content.removeSurrounding("\"").unescape())
        else when (content) {
            "true" -> SCPrimitive(true)
            "false" -> SCPrimitive(false)
            "Infinity" -> SCPrimitive(Double.POSITIVE_INFINITY)
            "-Infinity" -> SCPrimitive(Double.NEGATIVE_INFINITY)
            "NaN" -> SCPrimitive(Double.NaN)
            else if (content.none(Char::isDigit)) -> SCPrimitive(content)
            else -> content.toLongOrNull()?.let(::SCPrimitive)
                ?: content.toDoubleOrNull()?.let(::SCPrimitive)
                ?: SCPrimitive(content)
        }
}

public object TomlConverter : SCConverter, SCElement.Visitor<Nothing?, TomlElement> {
    @JvmField public val SPEC: Toml = Toml { ignoreUnknownKeys = true }

    override fun visitNull(ctx: Nothing?): TomlNull = TomlNull

    override fun visitPrimitive(prim: SCPrimitive, ctx: Nothing?): TomlLiteral = when (prim.kind) {
        Kind.Boolean -> TomlLiteral(prim.toBoolean())
        Kind.Integer -> TomlLiteral(prim.toLong())
        Kind.Float -> TomlLiteral(prim.toDouble())
        Kind.String -> TomlLiteral(prim.toString())
    }

    override fun visitList(list: SCList, ctx: Nothing?): TomlArray =
        TomlArray(list.map { it.accept(this, null) })

    override fun visitMap(map: SCMap, ctx: Nothing?): TomlTable =
        TomlTable(map.mapValues { (_, it) -> it.accept(this, null) })

    override fun convert(text: String): SCElement {
        val table: TomlElement = SPEC.decodeFromString(text)
        return visitElement(table)
    }

    private fun visitElement(element: TomlElement): SCElement = when (element) {
        TomlNull -> SCNull
        is TomlLiteral -> element.parseToLiteral()
        is TomlArray -> SCList(element.map(::visitElement))
        is TomlTable -> SCMap(element.mapValues { (_, it) -> visitElement(it) })
    }

    private fun TomlLiteral.parseToLiteral(): SCPrimitive = when (type) {
        TomlLiteral.Type.Boolean -> SCPrimitive(toBoolean())
        TomlLiteral.Type.Integer -> SCPrimitive(toLong())
        TomlLiteral.Type.Float -> SCPrimitive(toDouble())
        else -> SCPrimitive(content)
    }
}

public object YamlConverter : SCConverter, SCElement.Visitor<YamlPath, YamlNode> {
    @JvmField public val SPEC: Yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
    private val NOWHERE: Location = Location(1, 1)

    override fun visitNull(ctx: YamlPath): YamlNull = YamlNull(ctx)

    override fun visitPrimitive(prim: SCPrimitive, ctx: YamlPath): YamlScalar =
        YamlScalar(prim.toYamlString(), ctx)

    override fun visitList(list: SCList, ctx: YamlPath): YamlList {
        val items = list.mapIndexed { i, e ->
            e.accept(this, ctx.withListEntry(i, NOWHERE))
        }
        return YamlList(items, ctx)
    }

    override fun visitMap(map: SCMap, ctx: YamlPath): YamlMap {
        val entries = map.map { (k, v) ->
            val key = YamlScalar(k, ctx.withMapElementKey(k, NOWHERE))
            val value = v.accept(this, key.path.withMapElementValue(NOWHERE))
            key to value
        }
        return YamlMap(entries.toMap(), ctx)
    }

    private fun SCPrimitive.toYamlString(): String = if (kind != Kind.Float) toString() else when(val d = toDouble()) {
        Double.POSITIVE_INFINITY -> ".inf"
        Double.NEGATIVE_INFINITY -> "-.inf"
        else -> if (d.isNaN()) ".nan" else toString()
    }

    override fun convert(text: String): SCElement {
        val node: YamlNode = SPEC.parseToYamlNode(text)
        return visitNode(node)
    }

    private fun visitNode(node: YamlNode): SCElement = when (node) {
        is YamlNull -> SCNull
        is YamlScalar -> node.parseToLiteral()
        is YamlList -> SCList(node.items.map(::visitNode))
        is YamlMap -> SCMap(node.entries.map { (k, it) -> k.content to visitNode(it) }.toMap())
        is YamlTaggedNode -> visitNode(node.innerNode)
    }

    private fun YamlScalar.parseToLiteral(): SCPrimitive = when (val str = content) {
        ".inf", ".Inf", ".INF" -> SCPrimitive(Double.POSITIVE_INFINITY)
        "-.inf", "-.Inf", "-.INF" -> SCPrimitive(Double.NEGATIVE_INFINITY)
        ".nan", ".NaN", ".NAN" -> SCPrimitive(Double.NaN)
        "true", "True", "TRUE" -> SCPrimitive(true)
        "false", "False", "FALSE" -> SCPrimitive(false)
        else -> if (str.none(Char::isDigit)) SCPrimitive(str)
        else str.toLongOrNull()?.let(::SCPrimitive)
            ?: str.toDoubleOrNull()?.let(::SCPrimitive)
            ?: SCPrimitive(str)
    }
}
