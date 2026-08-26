package dev.kikugie.stonecutter.data

import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.invocation.Gradle

private const val SEP_CH: Char = ':'
private const val SEP_STR: String = ":"

private typealias PathString = String

@DslMarker @Retention(AnnotationRetention.BINARY)
private annotation class ProjectHierarchyDsl

/**
 * Represents an absolute Gradle project path.
 */
@JvmInline @Serializable @JvmExposeBoxed @ProjectHierarchyDsl @OptIn(ExperimentalStdlibApi::class)
public value class ProjectHierarchy private constructor(@PublishedApi internal val path: PathString) :
    List<PathString>, java.io.Serializable {
    override val size: Int
        get() = if (isEmpty()) 0 else path.count { it == SEP_CH }

    public fun orEmpty(): String =
        if (isEmpty()) "" else path

    @Deprecated("Use 'orEmpty()'", replaceWith = ReplaceWith("orEmpty()"))
    public fun orBlank(): String = orEmpty()

    public fun startsWith(other: ProjectHierarchy): Boolean =
        other.isEmpty() || path.startsWith(other.path) && path.sepAt(other.path.length)

    public fun relativeTo(base: ProjectHierarchy): PathString? = when {
        base.isEmpty() -> path.drop(1)
        path == base.path -> ""
        startsWith(base) -> path.substring(base.path.length + 1)
        else -> null
    }

    public fun relativize(longer: ProjectHierarchy): PathString? =
        longer.relativeTo(this)

    public fun resolve(path: PathString): ProjectHierarchy =
        if (isEmpty()) invoke(path)
        else invoke("${this.path}:$path")

    public fun removeSuffix(path: PathString): ProjectHierarchy {
        if (isEmpty()) return this
        val norm = normalize(path) ?: return this
        val path = this.path.removeSuffix(norm)

        return if (path.endsWith(':')) invoke(path.dropLast(1))
        else this
    }

    public operator fun plus(path: PathString): ProjectHierarchy =
        resolve(path)

    public operator fun minus(path: PathString): ProjectHierarchy =
        removeSuffix(path)

    /**Returns the absolute path string.*/
    override fun toString(): PathString = path

    /**Returns `true` for the root project path `:`.*/
    override fun isEmpty(): Boolean = path.length == 1

    /**
     * Checks if the [element] is a part of the path.
     *
     * Leading and trailing ':' of the [element] are excluded.
     * The remainder must be adjacent on both ends either to ':' or the string boundary.
     *
     * To perform a lenient check, use `toString().contains(element)`.
     *
     * @sample dev.kikugie.stonecutter.samples.hierarchy.contains
     */
    override fun contains(element: PathString): Boolean {
        val norm = normalize(element) ?: return false
        val index = path.indexOf(norm)
        return index > 0
            && path.sepAt(index - 1)
            && path.sepAt(index + norm.length)
    }

    /**
     * Checks if all [elements] is a part of the path.
     *
     * Leading and trailing ':' of each are excluded.
     * The remainder must be adjacent on both ends either to ':' or the string boundary.
     *
     * To perform a lenient check, use `toString().containsAll(elements)`.
     *
     * @sample dev.kikugie.stonecutter.samples.hierarchy.contains
     */
    override fun containsAll(elements: Collection<PathString>): Boolean =
        elements.all(::contains)

    /**
     * Gets the path segment at the specified [index].
     *
     * The [index] corresponds to the nth ':' in the path, not the string index.
     *
     * @throws IndexOutOfBoundsException If [index] is outside the path.
     * @sample dev.kikugie.stonecutter.samples.hierarchy.get
     */
    override fun get(index: Int): PathString {
        val start = path.locateChecked(index)
        val end = path.indexOf(SEP_CH, start + 1)

        return if (end < 0) path.substring(start + 1)
        else path.substring(start + 1, end)
    }

    /**
     * Finds the index of the [element] or `-1` if it's not in the path.
     *
     * The resulting index corresponds to the path segment.
     * The [element] must be adjacent on both ends either to ':' or the string boundary.
     */
    override fun indexOf(element: PathString): Int {
        val norm = normalize(element)
        if (norm == null || isEmpty() || norm.length >= path.length) return -1

        var start = 1
        var index = 0
        while (start > 0) {
            if (path.startsWith(norm, start) && path.sepAt(start + norm.length))
                return index
            start = path.indexOf(SEP_CH, start)  + 1
            index++
        }
        return -1
    }

    /**
     * Finds the last index of the [element] or `-1` if it's not in the path.
     *
     * The resulting index corresponds to the path segment.
     * The [element] must be adjacent on both ends either to ':' or the string boundary.
     */
    override fun lastIndexOf(element: PathString): Int {
        val norm = normalize(element)
        if (norm == null || isEmpty() || norm.length >= path.length) return -1

        var count = 0
        for (i in path.lastIndex downTo 0) if (path[i] == SEP_CH) {
            count++
            if (path.startsWith(norm, i + 1) && path.sepAt(i + norm.length + 1))
                return size - count
        }
        return -1
    }

    override fun iterator(): Iterator<PathString> = makeIterator()
    override fun listIterator(): ListIterator<PathString> = makeIterator()
    override fun listIterator(index: Int): ListIterator<PathString> = makeIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<PathString> =
        path.drop(1).split(':').subList(fromIndex, toIndex)

    private fun makeIterator(index: Int = 0): ListIterator<PathString> =
        if (isEmpty()) EMPTY_ITERATOR else PathIterator(path, index)

    private class PathIterator(private val path: PathString, private var index: Int) : ListIterator<PathString> {
        private var cursor: Int = if (index == 0) 1 else path.locateChecked(index) + 1

        override fun hasNext(): Boolean = cursor < path.length
        override fun hasPrevious(): Boolean = cursor > 0

        override fun nextIndex(): Int = index
        override fun previousIndex(): Int = index - 1

        override fun next(): PathString {
            if (!hasNext()) throw NoSuchElementException()

            val end = path.indexOf(SEP_CH, cursor)
            index++

            return if (end < 0) path.substring(cursor).also { cursor = path.length }
            else path.substring(cursor, end).also { cursor = end + 1 }
        }

        override fun previous(): PathString {
            if (!hasPrevious()) throw NoSuchElementException()

            var start = 0
            for (i in cursor - 2 downTo 0) if (path[i] == SEP_CH) {
                start = i
                break
            }
            index--

            return path.substring(start + 1, cursor - 1).also { cursor = start }
        }
    }

    @JvmExposeBoxed
    public companion object {
        private val EMPTY_ITERATOR: ListIterator<PathString> = emptyList<PathString>().listIterator()

        public val ROOT: ProjectHierarchy = ProjectHierarchy(SEP_STR)

        public val Project.hierarchy: ProjectHierarchy
            get() = ProjectHierarchy(path)

        public val ProjectDescriptor.hierarchy: ProjectHierarchy
            get() = ProjectHierarchy(path)

        @Deprecated("Use 'get()'", replaceWith = ReplaceWith("get(hierarchy)"))
        public fun Project.locate(hierarchy: ProjectHierarchy): Project =
            project(hierarchy.path)

        @Deprecated("Use 'get()'", replaceWith = ReplaceWith("get(hierarchy)"))
        public fun Gradle.locate(hierarchy: ProjectHierarchy): Project =
            rootProject.project(hierarchy.path)

        public operator fun Project.get(hierarchy: ProjectHierarchy): Project =
            project(hierarchy.path)

        public operator fun Gradle.get(hierarchy: ProjectHierarchy): Project =
            rootProject.project(hierarchy.path)

        @JvmName("of")
        public operator fun invoke(path: PathString): ProjectHierarchy {
            val norm = normalize(path) ?: return ROOT
            require(norm.splitToSequence(SEP_CH).none(PathString::isBlank)) { "Path '$SEP_CH$norm' must not contain blank segments" }
            return ProjectHierarchy("$SEP_CH$norm")
        }

        @JvmName("of")
        public operator fun invoke(components: Iterable<PathString>): ProjectHierarchy {
            require(components.none(PathString::isBlank)) { "Path '$SEP_CH${components.joinToString(SEP_STR)}' must not contain blank segments" }
            val norm = components.joinToString(SEP_STR).ifEmpty { return ROOT }
            return ProjectHierarchy("$SEP_CH$norm")
        }

        @JvmName("of")
        public operator fun invoke(vararg components: String): ProjectHierarchy {
            if (components.isEmpty()) return ROOT
            require(components.none(PathString::isBlank)) { "Path '$SEP_CH${components.joinToString(SEP_STR)}' must not contain blank segments" }
            return ProjectHierarchy("$SEP_CH${components.joinToString(SEP_STR)}")
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun PathString.locateChecked(index: Int, char: Char = SEP_CH): Int =
    locate(index, char).also { if (it < 0) throw IndexOutOfBoundsException("Index $index is out of bounds for path $this") }

private fun PathString.locate(index: Int, char: Char = SEP_CH): Int {
    if (index < 0 || length == 1) return -1
    if (index == 0) return 0

    var start = 1
    repeat(index) {
        start = indexOf(char, start)
        if (start < 0) return -1 else start++
    }
    return start - 1
}

private fun PathString.sepAt(index: Int, char: Char = SEP_CH): Boolean =
    index !in indices || get(index) == char

private fun normalize(string: String): String? =
    string.trim(SEP_CH).takeIf(String::isNotEmpty)
