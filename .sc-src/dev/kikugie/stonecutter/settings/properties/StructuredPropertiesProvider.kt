package dev.kikugie.stonecutter.settings.properties

import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.semver.impl.VersionParsingException
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.hierarchy
import dev.kikugie.stonecutter.data.deserialization.SCConverter
import dev.kikugie.stonecutter.data.deserialization.SCElement
import dev.kikugie.stonecutter.data.deserialization.determineConverter
import dev.kikugie.stonecutter.data.version.LenientOperations
import dev.kikugie.stonecutter.data.version.SemanticOperations
import dev.kikugie.stonecutter.util.toRegularFile
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensions.core.extra
import org.gradle.kotlin.dsl.the
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.reflect.KClass

public interface StructuredPropertiesProvider {
    /**
     * Configuration holder for extended Gradle properties.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/properties#centralized-properties">Structured Properties Documentation</a>
     */
    public val properties: Config

    /** Configures the [properties] extension. */
    public infix fun properties(action: Action<Config>): Unit = action.execute(properties)

    /**
     * Loads additional structured properties from a [file].
     *
     * This function accepts structured data formats, supporting `toml`, `json`, `json5`, `yml` and `yaml`
     * by default or other by providing a [SCConverter] implementation.
     * Loaded values are added to the Gradle's property container, making them available with
     * [Project.property] and [Project.extra][ExtraPropertiesExtension].
     *
     * During that process the path leading to each value is transformed as follows:
     * - Map keys and array indexes are converted to the path notation, such as `1.21.1:array:0`.
     * - Certain parts of the parts are cut down with [tags][Config.tags],
     * shortening the [sc.current.project][dev.kikugie.stonecutter.build.StonecutterBuildExtension.current] part by default.
     * - The remainder is converted to the dot notation, i.e. `array:0` -> `array.0`.
     *
     * See the code samples for concrete examples.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/properties#centralized-properties">Structured Properties Documentation</a>
     * @sample dev.kikugie.stonecutter.samples.properties.load_cutting
     * @sample dev.kikugie.stonecutter.samples.properties.load_flattening
     */
    public infix fun properties(file: File): Unit = properties.load(file)

    /**
     * Loads additional structured properties from a [file].
     *
     * This function accepts structured data formats, supporting `toml`, `json`, `json5`, `yml` and `yaml`
     * by default or other by providing a [converter] implementation.
     * Loaded values are added to the Gradle's property container, making them available with
     * [Project.property] and [Project.extra][ExtraPropertiesExtension].
     *
     * During that process the path leading to each value is transformed as follows:
     * - Map keys and array indexes are converted to the path notation, such as `1.21.1:array:0`.
     * - Certain parts of the parts are cut down with [tags][Config.tags],
     * shortening the [sc.current.project][dev.kikugie.stonecutter.build.StonecutterBuildExtension.current] part by default.
     * - The remainder is converted to the dot notation, i.e. `array:0` -> `array.0`.
     *
     * See the code samples for concrete examples.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/properties#centralized-properties">Structured Properties Documentation</a>
     * @sample dev.kikugie.stonecutter.samples.properties.load_cutting
     * @sample dev.kikugie.stonecutter.samples.properties.load_flattening
     */
    public fun properties(file: File, converter: SCConverter): Unit = properties.load(file, converter)

    public abstract class Config protected constructor(protected val objects: ObjectFactory) {
        /**
         * Loads additional structured properties from a [file].
         *
         * This function accepts structured data formats, supporting `toml`, `json`, `json5`, `yml` and `yaml`
         * by default or other by providing a [converter] implementation.
         * Loaded values are added to the Gradle's property container, making them available with
         * [Project.property] and [Project.extra][ExtraPropertiesExtension].
         *
         * During that process the path leading to each value is transformed as follows:
         * - Map keys and array indexes are converted to the path notation, such as `1.21.1:array:0`.
         * - Certain parts of the parts are cut down with [tags][Config.tags],
         * shortening the [sc.current.project][dev.kikugie.stonecutter.build.StonecutterBuildExtension.current] part by default.
         * - The remainder is converted to the dot notation, i.e. `array:0` -> `array.0`.
         *
         * See the code samples for concrete examples.
         * @see <a href="https://stonecutter.kikugie.dev/wiki/config/properties#centralized-properties">Structured Properties Documentation</a>
         * @sample dev.kikugie.stonecutter.samples.properties.load_cutting
         * @sample dev.kikugie.stonecutter.samples.properties.load_flattening
         */
        @JvmOverloads
        public fun load(file: File, converter: SCConverter = determineConverter(file)): Unit =
            loadImpl(file.toRegularFile(objects), converter).getOrThrow()

        /**
         * Adds a list of tags to the config.
         *
         * Tags represent map keys which are removed from the property paths,
         * as described in the [load] documentation.
         *
         * Tags may be removed from **any** part of the path, as long as it's not left empty.
         * Tags may include `:`s inside them to enforce a specific order of shortened paths.
         * Tags are applied to already loaded property files, as well as to the future ones.
         *
         * @see <a href="https://stonecutter.kikugie.dev/wiki/config/properties#property-tags">Property Tags Documentation</a>
         * @see <a href="https://codeberg.org/stonecutter/stonecutter/issues/49">Tag resolution proposal and full specification</a>
         * @sample dev.kikugie.stonecutter.samples.properties.tags
         * @sample dev.kikugie.stonecutter.samples.properties.ordered_tags
         */
        public abstract fun tags(vararg tags: String)

        /**
         * Retrieves the structured property as converted [SCElement]
         * at the path defined by [segments].
         *
         * The [segments] are converted to the colon path notation described in the [load]
         * documentation (i.e. `raw("nested", "key")` is equivalent to `raw("nested:key")`.
         * Values are only queried from files registered with the [load] function.
         * Gradle's native properties are **not included**.
         *
         * @return [SCElement] at the requested location; see its docs to learn what you can do with it
         * @throws IllegalArgumentException if no element is found
         * @sample dev.kikugie.stonecutter.samples.properties.raw
         */
        public fun raw(vararg segments: String): SCElement = requireNotNull(rawOrNull(*segments)) {
            "No element at path '${segments.joinToString(":")}'"
        }

        /**
         * Retrieves the structured property as converted [SCElement]
         * at the path defined by [segments].
         *
         * The [segments] are converted to the colon path notation described in the [load]
         * documentation (i.e. `rawOrNull("nested", "key")` is equivalent to `rawOrNull("nested:key")`.
         * Values are only queried from files registered with the [load] function.
         * Gradle's native properties are **not included**.
         *
         * @return [SCElement] at the requested location or null if it's not found;
         * see its docs to learn what you can do with it
         * @sample dev.kikugie.stonecutter.samples.properties.raw
         */
        public abstract fun rawOrNull(vararg segments: String): SCElement?

        /**
         * Retrieves a property with the given [name], converted to the [T] type.
         *
         * Properties are queried from [ExtraPropertiesExtension], making use
         * of ones defined with Gradle's regular mechanisms as well.
         *
         * The conversion process first stringifies the property value,
         * and then parses it as the requested type.
         *
         * @return Property [name] converted to [T]
         * @param T Requested value type; may be one of:
         * - [Boolean]: case-insensitive `"true"` or `"false"`
         * - [Short], [Int], [Long], [Float], [Double]:
         *   parsed with `java.lang.<type>.parse<type>()`
         * - [Version], [StringVersion], [SemanticVersion]:
         *   parsed with the corresponding [Version.parse] override
         * @throws [IllegalArgumentException] If the property with this [name] is not defined
         * @throws [IllegalArgumentException] If [T] is not one of the listed supported types
         * @throws [IllegalArgumentException] If [T] is [Boolean] and the value can't be parsed as such
         * @throws [NumberFormatException] If [T] is a [Number] and the value can't be parsed as such
         * @throws [VersionParsingException] If [T] is a [Version] and the value can't be parsed as such
         */
        public inline operator fun <reified T : Any> get(name: String): T = requireNotNull(getTypedImpl(name, T::class)) {
            "No such property '$name'"
        }

        /**
         * Retrieves a property with the given [name], converted to the [T] type if it exists.
         *
         * Properties are queried from [ExtraPropertiesExtension], making use
         * of ones defined with Gradle's regular mechanisms as well.
         *
         * The conversion process first stringifies the property value,
         * and then parses it as the requested type.
         *
         * @return Property [name] converted to [T] or `null` if it's not defined.
         * @param T Requested value type; may be one of:
         * - [Boolean]: case-insensitive `"true"` or `"false"`
         * - [Short], [Int], [Long], [Float], [Double]:
         *   parsed with `java.lang.<type>.parse<type>()`
         * - [Version], [StringVersion], [SemanticVersion]:
         *   parsed with the corresponding [Version.parse] override
         * @throws [IllegalArgumentException] If [T] is not one of the listed supported types
         * @throws [IllegalArgumentException] If [T] is [Boolean] and the value can't be parsed as such
         * @throws [NumberFormatException] If [T] is a [Number] and the value can't be parsed as such
         * @throws [VersionParsingException] If [T] is a [Version] and the value can't be parsed as such
         */
        public inline fun <reified T : Any> getOrNull(name: String): T? =
            getTypedImpl(name, T::class)

        @PublishedApi @JvmSynthetic
        internal abstract fun <T : Any> getTypedImpl(name: String, cls: KClass<T>): T?

        @JvmSynthetic
        internal abstract fun loadImpl(file: RegularFile, converter: SCConverter): Result<Unit>
    }
}

internal class StructuredPropertyConfigImpl(
    val project: ProjectHierarchy,
    val extra: ExtraPropertiesExtension,
    val loader: StructuredPropertiesLoader,
    objects: ObjectFactory,
) : StructuredPropertiesProvider.Config(objects) {
    constructor(project: Project, objects: ObjectFactory) : this(project.hierarchy, project.extra, project.gradle.the(), objects)

    private val tags: MutableSet<String> = mutableSetOf()

    override fun rawOrNull(vararg segments: String): SCElement? =
        loader.get(project, segments.joinToString(":"), this.tags).element

    override fun <T : Any> getTypedImpl(name: String, cls: KClass<T>): T? =
        if (!extra.has(name)) null
        else extra[name].toString().mapTo(cls)

    override fun tags(vararg tags: String) {
        val tags = tags
            .filter { it.isNotBlank() }
            .onEach { require(isValidPath(it)) { "Invalid tag '$it'" } }
            .toSet()
        if (tags.isEmpty()) return

        this.tags += tags
        loader.apply(project, extra, this.tags)
    }

    override fun loadImpl(file: RegularFile, converter: SCConverter): Result<Unit> {
        val entry = loader.load(project, file, converter)
            ?: return Result.failure(FileNotFoundException(file.asFile.absolutePath))
        entry.apply(extra, tags)
        return Result.success(Unit)
    }

    internal fun loadDefaults(directory: Directory) {
        val file = DEFAULT_FILES.firstNotNullOfOrNull { name -> directory.file(name).takeIf { it.asFile.exists() } }
        if (file != null) loadImpl(file, determineConverter(file.asFile))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> String.mapTo(cls: KClass<T>): T = when (cls) {
        String::class -> this
        Boolean::class -> lowercase(Locale.US).toBooleanStrict()
        Int::class -> toInt()
        Long::class -> toLong()
        Byte::class -> toByte()
        Short::class -> toShort()
        Double::class -> toDouble()
        Float::class -> toFloat()
        Version::class -> LenientOperations.tryParse(this).getOrThrow()
        StringVersion::class -> StringVersion.parse(this)
        SemanticVersion::class -> SemanticOperations.tryParse(this).getOrThrow()
        else -> throw IllegalArgumentException("Value '$this' can't be converted to ${cls.simpleName}")
    } as T
}
