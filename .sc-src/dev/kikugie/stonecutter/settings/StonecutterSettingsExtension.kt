package dev.kikugie.stonecutter.settings

import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.ProjectReference
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.deserialization.SCConverter
import dev.kikugie.stonecutter.data.version.SemanticOperations
import dev.kikugie.stonecutter.data.version.VersionOperations
import dev.kikugie.stonecutter.settings.properties.StructuredPropertiesProvider
import dev.kikugie.stonecutter.settings.tree.TreeBuilder
import dev.kikugie.stonecutter.settings.tree.TreeBuilderImpl
import dev.kikugie.stonecutter.util.doFirst
import org.gradle.api.Action
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.newInstance
import java.io.File

@DslMarker @Retention(AnnotationRetention.BINARY)
private annotation class SettingsDsl

/**
 * Extension interface applied to `settings.gradle(.kts)`.
 *
 * The extension provides Stonecutter project registration functions
 * and buildscript DSL configuration.
 *
 * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params">Wiki #1</a>
 * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings">Wiki #2</a>
 */
@SettingsDsl
public abstract class StonecutterSettingsExtension(
    protected val objects: ObjectFactory,
    protected val providers: ProviderFactory
) : VersionOperations<Version>, StructuredPropertiesProvider {
    /**
     * Shared [Action] for configuring instances of [TreeBuilder].
     *
     * This action is applied when using [create] methods with no explicit configuration,
     * and can be added manually with `shared.execute(this)` in a [TreeBuilder] scope.
     */
    public var shared: Action<TreeBuilder> = Action {}
        private set

    /**
     * Name for the shared build script across all subprojects.
     *
     * Can't be set to `stonecutter.gradle(.kts)`.
     * Defaults to `build.gradle.kts`, but uses `build.gradle` if the file already exists.
     *
     * This value applies to all registered project trees, and can be overridden with (by increasing priority):
     * 1. [TreeBuilder.centralScript]
     * 2. [TreeBuilder.mapBuilds]
     * 3. [NodeBuilder.buildscript][dev.kikugie.stonecutter.settings.tree.NodeBuilder.buildscript]
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#buildscript-mapping">Build Script Mapping Documentation</a>
     */
    public abstract val centralScript: Property<String>

    /**
     * Kotlin DSL switch for `stonecutter.gradle.kts`/`stonecutter.gradle`.
     *
     * Defaults to `true`, and respectively `stonecutter.gradle.kts`.
     *
     * This value applies to all registered project trees, and can be overridden with
     * [TreeBuilder.kotlinController]
     */
    public abstract val kotlinController: Property<Boolean>

    /**
     * Accessor for strictly SemVer version evaluation.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/build#arbitrary-operations">Semantic Operations Documentation</a>
     */
    public val semantics: VersionOperations<SemanticVersion> get() = SemanticOperations

    /* Shared configuration */
    /**
     * Defines a shared [Action] for configuring instances of [TreeBuilder].
     *
     * This action is applied when using [create] methods with no explicit configuration,
     * and can be added manually with `shared.execute(this)` in a [TreeBuilder] scope.
     */
    public fun shared(action: Action<TreeBuilder>) {
        shared = action
    }

    /* File configuration */
    /**
     * Registers the given [project] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are provided by the [file] contents.
     * The supported formats are `toml`, `json`, `json5`, `yml` and `yaml`.
     *
     * The file's parameters may be overridden with the [action] parameter or the [shared]
     * configuration.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#data-driven-setup">Data-driven Setup Documentation</a>
     */
    @JvmOverloads
    public fun create(project: ProjectReference, file: File, action: Action<TreeBuilder> = shared): Unit =
        create(listOf(project), action.doFirst { load(file) })

    /**
     * Registers the given [project] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are provided by the [file] contents.
     * The data is converted to Json types with the custom [converter].
     * For default-supported formats use the overload without the converter parameter.
     *
     * The file's parameters may be overridden with the [action] parameter or the [shared]
     * configuration.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#data-driven-setup">Data-driven Setup Documentation</a>
     */
    @JvmOverloads
    public fun create(project: ProjectReference, file: File, converter: SCConverter, action: Action<TreeBuilder> = shared): Unit =
        create(listOf(project), action.doFirst { load(file, converter) })

    /**
     * Registers the given [projects] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are provided by the [file] contents.
     * The supported formats are `toml`, `json`, `json5`, `yml` and `yaml`.
     *
     * The file's parameters may be overridden with the [action] parameter or the [shared]
     * configuration.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#data-driven-setup">Data-driven Setup Documentation</a>
     */
    @JvmOverloads
    public fun create(vararg projects: ProjectReference, file: File, action: Action<TreeBuilder> = shared): Unit =
        create(projects.asIterable(), action.doFirst { load(file) })

    /**
     * Registers the given [projects] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are provided by the [file] contents.
     * The data is converted to Json types with the custom [converter].
     * For default-supported formats use the overload without the converter parameter.
     *
     * The file's parameters may be overridden with the [action] parameter or the [shared]
     * configuration.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#data-driven-setup">Data-driven Setup Documentation</a>
     */
    @JvmOverloads
    public fun create(vararg projects: ProjectReference, file: File, converter: SCConverter, action: Action<TreeBuilder> = shared): Unit =
        create(projects.asIterable(), action.doFirst { load(file, converter) })

    /**
     * Registers the given [projects] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are provided by the [file] contents.
     * The supported formats are `toml`, `json`, `json5`, `yml` and `yaml`.
     *
     * The file's parameters may be overridden with the [action] parameter or the [shared]
     * configuration.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#data-driven-setup">Data-driven Setup Documentation</a>
     */
    @JvmOverloads
    public fun create(projects: Iterable<ProjectReference>, file: File, action: Action<TreeBuilder> = shared): Unit =
        create(projects, action.doFirst { load(file) })

    /**
     * Registers the given [projects] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are provided by the [file] contents.
     * The data is converted to Json types with the custom [converter].
     * For default-supported formats use the overload without the converter parameter.
     *
     * The file's parameters may be overridden with the [action] parameter or the [shared]
     * configuration.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#data-driven-setup">Data-driven Setup Documentation</a>
     */
    @JvmOverloads
    public fun create(projects: Iterable<ProjectReference>, file: File, converter: SCConverter, action: Action<TreeBuilder> = shared): Unit =
        create(projects, action.doFirst { load(file, converter) })

    /* Action configuration */
    /**
     * Registers the given [project] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are configured in the provided [action],
     * or the [shared] one.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     */
    @JvmOverloads
    public fun create(project: ProjectReference, action: Action<TreeBuilder> = shared): Unit =
        create(listOf(project), action)

    /**
     * Registers the given [projects] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are configured in the provided [action],
     * or the [shared] one.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     */
    @JvmOverloads
    public fun create(vararg projects: ProjectReference, action: Action<TreeBuilder> = shared): Unit =
        create(projects.asIterable(), action)

    /**
     * Registers the given [projects] as multi-versioned with Stonecutter.
     *
     * The subprojects and their associated versions are configured in the provided [action],
     * or the [shared] one.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings">Project Setup Guide</a>
     */
    @JvmOverloads
    public fun create(projects: Iterable<ProjectReference>, action: Action<TreeBuilder> = shared): Unit = projects.forEach {
        val builder = objects.newInstance<TreeBuilderImpl>(providers, it.toProjectHierarchy())
        action.execute(builder)
        create(it, builder)
    }

    /* Base configuration */
    protected abstract fun create(ref: ProjectReference, builder: TreeBuilder)
}

private tailrec fun ProjectReference.toProjectHierarchy(): String = when (this) {
    is CharSequence -> ":${trim(':')}"
    is ProjectDescriptor -> path
    is Provider<*> -> get().toProjectHierarchy()
    else -> throw IllegalArgumentException("Unsupported project type ${this::class.qualifiedName}")
}
