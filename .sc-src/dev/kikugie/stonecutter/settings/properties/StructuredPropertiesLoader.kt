package dev.kikugie.stonecutter.settings.properties

import dev.kikugie.stonecutter.data.ProjectHierarchy
import dev.kikugie.stonecutter.data.deserialization.SCConverter
import dev.kikugie.stonecutter.data.deserialization.SCElement
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.namedDomainObjectList
import org.slf4j.Logger
import javax.inject.Inject

internal val PROPERTIES_LOGGER: Logger = Logging.getLogger("StructuredPropertiesLoader")
internal val DEFAULT_FILES: List<String> = listOf(
    "stonecutter.properties.toml",
    "stonecutter.properties.yaml",
    "stonecutter.properties.yml",
    "stonecutter.properties.json5",
    "stonecutter.properties.json"
)

internal abstract class StructuredPropertiesLoader @Inject constructor(val objects: ObjectFactory, val providers: ProviderFactory) {
    private val container: NamedDomainObjectCollection<StructuredPropertiesEntry> = objects.namedDomainObjectList(StructuredPropertiesEntry::class)

    fun apply(project: ProjectHierarchy, extra: ExtraPropertiesExtension, tags: Set<String> = emptySet()) {
        val collector = PropertiesCollector(tags, extra::set)
        for (it in entriesFor(project)) it.entry.accept(collector, "")
    }

    fun get(project: ProjectHierarchy, path: String, tags: Set<String> = emptySet()): PropertiesRetriever.Result {
        val retriever = PropertiesRetriever(tags, path)
        return entriesFor(project).findCandidate { it.entry.accept(retriever, "") }
    }

    fun load(project: ProjectHierarchy, file: RegularFile, converter: SCConverter): StructuredPropertiesEntry? = synchronized(container) {
        PROPERTIES_LOGGER.debug("Loading properties from {}", file.asFile.absolutePath)
        container.findByName(StructuredPropertiesEntry.name(file, project))
            ?: loadNew(file, project, converter)?.also(container::add)
    }

    private fun loadNew(file: RegularFile, project: ProjectHierarchy, converter: SCConverter): StructuredPropertiesEntry? {
        val contents = providers.fileContents(file).asText.orNull ?: return null
        return StructuredPropertiesEntry(converter.convert(contents), project, file)
    }

    private fun entriesFor(project: ProjectHierarchy): List<StructuredPropertiesEntry> = container
        .filter { project.startsWith(it.requestee) }
        .sortedBy { it.requestee.size }
}

internal class StructuredPropertiesEntry(val entry: SCElement, val requestee: ProjectHierarchy, file: RegularFile) : Named {
    private val id: String = name(file, requestee)

    override fun getName(): String = id
    fun apply(extra: ExtraPropertiesExtension, tags: Set<String> = emptySet()) = entry.accept(PropertiesCollector(tags, extra::set), "")
    fun get(path: String, tags: Set<String> = emptySet()): PropertiesRetriever.Result = entry.accept(PropertiesRetriever(tags, path), "")

    companion object {
        fun name(file: RegularFile, project: ProjectHierarchy): String =
            "${file.asFile.absolutePath}#$project"
    }
}

private fun ExtraPropertiesExtension.set(result: PropertiesCollector.Result): Unit =
    set(result.key.replace(':', '.'), result.primitive.toString())
