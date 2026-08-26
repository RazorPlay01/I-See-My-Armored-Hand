package dev.kikugie.stonecutter.controller

import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.StonecutterPlugin
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerManager.Companion.getController
import dev.kikugie.stonecutter.controller.file.FileHandlerContainer
import dev.kikugie.stonecutter.controller.flag.StonecutterFlag
import dev.kikugie.stonecutter.controller.flag.StonecutterFlagStorage
import dev.kikugie.stonecutter.controller.flag.StonecutterFlags
import dev.kikugie.stonecutter.controller.flag.StonecutterFlagsImpl
import dev.kikugie.stonecutter.controller.task.StonecutterControllerTasksImpl
import dev.kikugie.stonecutter.data.ParsedVersion
import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.ProjectHierarchy.Companion.hierarchy
import dev.kikugie.stonecutter.data.StonecutterProject
import dev.kikugie.stonecutter.data.container.GradleContainerExtension.Companion.getContainer
import dev.kikugie.stonecutter.data.container.ProjectNodeContainer
import dev.kikugie.stonecutter.data.container.TreeBuilderContainer
import dev.kikugie.stonecutter.data.tree.ProjectBranchImpl
import dev.kikugie.stonecutter.data.tree.ProjectNodeImpl
import dev.kikugie.stonecutter.data.tree.ProjectTreeImpl
import dev.kikugie.stonecutter.data.version.LenientOperations
import dev.kikugie.stonecutter.data.version.VersionOperations
import dev.kikugie.stonecutter.settings.properties.StructuredPropertyConfigImpl
import dev.kikugie.stonecutter.settings.properties.StructuredPropertiesProvider
import dev.kikugie.stonecutter.settings.task.StonecutterIdeaConfigTask
import dev.kikugie.stonecutter.settings.tree.BranchBuilderImpl
import dev.kikugie.stonecutter.settings.tree.TreeBuilderImpl
import dev.kikugie.stonecutter.util.isIdeaSync
import dev.kikugie.stonecutter.util.propertyKey
import dev.kikugie.stonecutter.util.requestTasks
import dev.kikugie.stonecutter.util.toRegularFile
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.configuration.BuildFeatures
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

internal abstract class StonecutterControllerImpl @Inject constructor(val root: Project, objects: ObjectFactory) :
    StonecutterControllerExtension, VersionOperations<Version> by LenientOperations {
    override val tree: ProjectTreeImpl = constructTree(root)
    override val tasks: StonecutterControllerTasksImpl = root.objects.newInstance(this)
    override val flags: StonecutterFlagsImpl = StonecutterFlagStorage { root.findProperty(it.propertyKey)?.toString() }
        .let(::StonecutterFlagsImpl)
    override val handlers: FileHandlerContainer = root.gradle.getContainer()
    override val properties: StructuredPropertiesProvider.Config = setupDefaultProperties(root, objects)

    private val nodeContainer: ProjectNodeContainer = root.gradle.getContainer<ProjectNodeContainer>().also { it += tree }
    private var hasInitialized: Boolean = false

    init {
        root.afterEvaluate {
            if (!hasInitialized) {
                val function =
                    if (buildFile.name.endsWith("kts")) "stonecutter active \"<version>\"/stonecutter active file(\"<path>\")"
                    else "stonecutter.active '<version>'/stonecutter.active file('<path>')"
                error("Stonecutter has not been initialized. Use `$function` to initialize it.")
            }

            if (plugins.hasPlugin("base"))
                logger.warn("Stonecutter branch root $hierarchy should not be a buildable project. Remove the `base` or `java` plugin to fix the issue.")
        }
    }

    override fun active(provider: Any?) {
        check(!hasInitialized) { "Stonecutter has already been initialized!" }
        tree.assignActive(root, tasks, provider.resolveActive())

        if (flags[StonecutterFlag.APPLY_PLUGIN_TO_NODES])
            for (it in tree.nodes) it.project.plugins.apply(StonecutterPlugin::class)

        if (tree.current != null) tree.createSwitchTasks(root, tasks)
        tree.createModelTasks(tasks)
        tree.configureSyncTask(root, flags)

        hasInitialized = true
    }

    override fun parameters(action: Action<StonecutterBuildExtension>) {
        nodeContainer.configure(tree, action)
    }
}

private fun setupDefaultProperties(project: Project, objects: ObjectFactory): StructuredPropertiesProvider.Config {
    return StructuredPropertyConfigImpl(project, objects).apply { loadDefaults(project.layout.projectDirectory) }
}

private fun constructTree(root: Project): ProjectTreeImpl {
    val builder = checkNotNull(root.gradle.getContainer<TreeBuilderContainer>()[root.hierarchy]) {
        "Project ${root.path} is not registered. This might've been caused by removing a project while it's active"
    }
    val branches = builder.constructBranches(root, root.hierarchy)
    return ProjectTreeImpl(root.gradle, root.hierarchy, builder.getVcsProject(), branches).apply {
        for (it in branches) it.tree = this
    }
}

private fun TreeBuilderImpl.constructBranches(root: Project, tree: ProjectHierarchy): List<ProjectBranchImpl> = branchBuilders.values.map {
    val hierarchy = tree.resolve(it.name)
    val nodes = it.constructNodes(root, hierarchy)
    ProjectBranchImpl(root.gradle, hierarchy, it.name, nodes).apply {
        for (node in nodes) node.branch = this
    }
}

private fun BranchBuilderImpl.constructNodes(root: Project, branch: ProjectHierarchy): List<ProjectNodeImpl> = allProjects().map {
    ProjectNodeImpl(root.gradle, branch.resolve(it.project), it)
}

private fun ProjectTreeImpl.findByName(name: String): StonecutterProject = checkNotNull(versions.find { it.project == name }) {
    "Version '$name' is not registered. This might've been caused by removing a version that is set to be active."
}

private fun ProjectTreeImpl.assignActive(root: Project, tasks: StonecutterControllerTasksImpl, provider: Any?): Unit = when (provider) {
    is String -> {
        current = findByName(provider)
        val manager = checkNotNull(root.getController()) { "Tree $hierarchy has no Stonecutter controller" }
        for ((project) in versions) tasks.registerScriptSwitchTask(project, manager)
    }

    is File -> {
        val contents = provider.toRegularFile(root.objects).let(root.providers::fileContents).asText.orNull
            ?: throw NoSuchFileException(provider)
        current = findByName(contents.trim())
        for ((project) in versions) tasks.registerExternalSwitchTask(project, provider)
    }

    else -> {
    }
}

private fun ProjectTreeImpl.createSwitchTasks(root: Project, impls: StonecutterControllerTasksImpl): Unit = with(root) {
    tasks.register("Reset active project") {
        group = "stonecutter"
        description = "Sets active version to ${vcs.project}. Run this before making a commit."
        dependsOn(impls.switch.getOrThrow(vcs.project))
    }

    tasks.register("Refresh active project") {
        group = "stonecutter"
        description = "Runs the comment processor on the active version. Useful for fixing comments in wrong states."
        dependsOn(impls.switch.getOrThrow(current!!.project))
    }

    val sorting: Comparator<StonecutterProject> = Comparator.comparing<StonecutterProject, ParsedVersion> { it.parsed }.thenComparing { it.project }
    for ((project) in versions.sortedWith(sorting)) tasks.register("Set active project to $project") {
        group = "stonecutter"
        description = "Sets the active project to $project, processing all versioned comments."
        dependsOn(impls.switch.getOrThrow(project))
    }
}

private fun ProjectTreeImpl.createModelTasks(impls: StonecutterControllerTasksImpl) {
    val aggregate = impls.registerModelGroupingTask()
    val delegates = buildList {
        this += impls.registerTreeModelTask()
        for (branch in branches)
            this += impls.registerBranchModelTask(branch)
    }

    aggregate.configure {
        dependsOn(delegates)
    }
}

private fun ProjectTreeImpl.configureSyncTask(root: Project, flags: StonecutterFlags): Unit = root.afterEvaluate {
    if (flags[StonecutterFlag.GENERATE_SWITCH_ACTIONS]) rootProject.tasks.named<StonecutterIdeaConfigTask>("stonecutterIdea") {
        projects.put(hierarchy.path, versions.map(StonecutterProject::project))
    }

    if (flags[StonecutterFlag.SERIALIZE_TREE_MODEL] && isIdeaSync)
        root.gradle.requestTasks(listOf("stonecutterSaveModels"), root.path, root.projectDir)
}

private tailrec fun Any?.resolveActive(): Any? = when(this) {
    null,
    is String,
    is File -> this
    is Path -> toFile()
    is RegularFile -> asFile
    is Provider<*> -> get().resolveActive()
    else -> throw IllegalArgumentException("Unsupported active project type ${this::class.qualifiedName}")
}