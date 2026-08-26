package dev.kikugie.stonecutter.controller

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.file.FileHandlerContainer
import dev.kikugie.stonecutter.controller.flag.StonecutterFlags
import dev.kikugie.stonecutter.controller.task.StonecutterControllerTasks
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.tree.ProjectTree
import dev.kikugie.stonecutter.data.version.SemanticOperations
import dev.kikugie.stonecutter.data.version.VersionOperations
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.settings.properties.StructuredPropertiesProvider
import org.gradle.api.Action

@DslMarker @Retention(AnnotationRetention.BINARY)
private annotation class ControllerDsl

/**
 * Extension interface applied to `stonecutter.gradle(.kts)`.
 *
 * The extension provides active version configuration, task aggregation utilities,
 * and custom file format registration.
 *
 * @see <a href="https://stonecutter.kikugie.dev/wiki/config/controller">Controller Utilities Documentation</a>
 * @see <a href="https://stonecutter.kikugie.dev/wiki/config/params">Preprocessor Configuration Documentation</a>
 */
@ControllerDsl
public interface StonecutterControllerExtension : VersionOperations<Version>, StructuredPropertiesProvider {
    /**
     * Tree associated with the applied Gradle [Project][org.gradle.api.Project]..
     *
     * The project tree corresponds to the subprojects initially registered
     * with `stonecutter.create()` in `settings.gradle(.kts)`.
     * The tree instance provides access to shared properties, such as
     * the active and VCS versions and all existing nodes.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/settings#project-branches">Project Structure Documentation</a>
     */
    public val tree: ProjectTree

    /**
     * Project descriptor for the version assigned with the [active] function.
     *
     * The active subproject has its sources linked to the shared `src/` directory.
     */
    public val current: StonecutterProject? get() = tree.current

    /**
     * Project descriptor for the VCS reset version.
     *
     * The VCS version can be switched to with the `Reset active project` task,
     * which modifies the shared `src/` directory to be in a consistent state
     * before submitting a commit.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/start/settings#the-vcs-reset-point">VCS Version Documentation</a>
     */
    public val vcsVersion: StonecutterProject get() = tree.vcs

    /**
     * Set of all unique project descriptors in the [tree].
     *
     * Each branch may contain a different set of defined descriptors,
     * which are however consistent by their [project][StonecutterProject.project]
     * and [version][StonecutterProject.version].
     * This property can be used to iterate on each possible descriptor exactly once.
     */
    public val versions: Set<StonecutterProject> get() = tree.versions

    /**
     * Accessor for strictly SemVer version evaluation.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/build#arbitrary-operations">Semantic Operations Documentation</a>
     */
    public val semantics: VersionOperations<SemanticVersion> get() = SemanticOperations

    /**
     * Mutable view of Stonecutter configuration flags.
     *
     * Flags are used to configure the internal behaviour of the plugin.
     * **The flags should be configured before the [active] call.**
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/controller#flags">Flags Documentation</a>
     */
    public val flags: StonecutterFlags

    /**Stonecutter version switch tasks and aggregation utility extension.*/
    public val tasks: StonecutterControllerTasks

    /**Custom file format descriptor registry extension.*/
    public val handlers: FileHandlerContainer

    /**
     * Initializes the plugin and declares an active version.
     * **Must be called exactly once**.
     *
     * The [provider] can be:
     * - [String]: A string **literal** (not a variable reference!)
     *   that is modified by version switching tasks.
     * - [File][java.io.File]: A file that contains the active version string.
     *   The file is expected to be in UTF-8 encoding and must only contain the version text,
     *   with an optional line break.
     * - `null`: Initializes the plugin in detached source mode.
     *   In this mode the shared `src/` directory is not attached as a project source.
     *   Instead its contents are processed as each version's generated sources.
     *   This mode can be used in CI workflows that build the project with multiple runners.
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/controller#active-version">Active Version Documentation</a>
     */
    public infix fun active(provider: Any?)

    /**
     * Configures the code preprocessor parameters for all nodes.
     *
     * This function should be used in favour of [subprojects { }][org.gradle.api.Project.subprojects],
     * as it's evaluated lazily when the plugin is applied to the versioned build script.
     *
     * However, it **must be used only for Stonecutter configuration**. It's safe to access project properties,
     * but other plugins may not be applied when the [action] is executed.
     */
    public infix fun parameters(action: Action<StonecutterBuildExtension>)

    /**
     * Configures Stonecutter configuration flags.
     *
     * Flags are used to configure the internal behaviour of the plugin.
     * **The flags should be configured before the [active] call.**
     * @see <a href="https://stonecutter.kikugie.dev/wiki/config/controller#flags">Flags Documentation</a>
     */
    public infix fun flags(action: Action<StonecutterFlags>): Unit = action.execute(flags)

    /**Configures the [tasks] extension.*/
    public infix fun tasks(action: Action<StonecutterControllerTasks>): Unit = action.execute(tasks)

    /**Configures the [handlers] extension.*/
    public infix fun handlers(action: Action<FileHandlerContainer>): Unit = action.execute(handlers)
}