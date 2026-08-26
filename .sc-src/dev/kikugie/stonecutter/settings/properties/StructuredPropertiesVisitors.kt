package dev.kikugie.stonecutter.settings.properties

import dev.kikugie.commons.text.getOrDefault
import dev.kikugie.stonecutter.data.deserialization.*
import dev.kikugie.stonecutter.util.join

internal abstract class PropertiesVisitor<T>(private val tags: Set<String>) : SCElement.Visitor<String, T> {
    protected fun cut(path: String): String {
        var offset = 0
        val builder = StringBuilder(path)
        val used = mutableSetOf<String>()

        while (true) {
            var candidate = ""
            for (tag in tags) if (tag.passes(offset, builder, candidate, used))
                candidate = tag

            if (candidate.isNotEmpty())
                builder.cutSegment(offset, candidate, used)
            else {
                val next = builder.indexOf(':', offset)
                if (next < 0) break else offset = next + 1
            }
        }

        return builder.toString()
    }

    private fun StringBuilder.cutSegment(offset: Int, candidate: String, used: MutableSet<String>) {
        if (getOrDefault(offset - 1) == ':') replace(offset - 1, offset + candidate.length, "")
        else if (getOrDefault(offset + candidate.length) == ':') replace(offset, offset + candidate.length + 1, "")
        else replace(offset, candidate.length, "")
        used += candidate
    }

    private fun String.passes(offset: Int, path: CharSequence, cand: String, used: Set<String>): Boolean = when {
        length < cand.length -> false // Shorter than the last best
        length >= path.length -> false // Out of bounds
        this in used -> false // Can't use more than once
        path.getOrDefault(offset + length, ':') != ':' -> false // Doesn't stop at :
        else -> path.startsWith(this, offset) // Doesn't match the segment
    }
}

internal class PropertiesCollector(tags: Set<String>, private val consumer: Result.() -> Unit) : PropertiesVisitor<Unit>(tags) {
    class Result(val key: String, val primitive: SCPrimitive, val depth: UInt)
    private val depths: MutableMap<String, UInt> = mutableMapOf()

    override fun visitNull(ctx: String) {
        // Don't add nulls
    }

    override fun visitPrimitive(prim: SCPrimitive, ctx: String) = Result(cut(ctx), prim, ctx.pathDepth).run {
        if (depths.getOrDefault(key, 0u) > depth) return@run

        depths[key] = depth
        consumer(this)
    }

    override fun visitList(list: SCList, ctx: String) {
        for ((i, e) in list.withIndex())
            e.accept(this, join(ctx, i.toString(), ":"))
    }

    override fun visitMap(map: SCMap, ctx: String) {
        for ((k, v) in map) join(ctx, k, ":").let {
            if (isValidPath(k)) v.accept(this, it)
            else PROPERTIES_LOGGER.warn("Invalid property path '$it'")
        }
    }
}

internal class PropertiesRetriever(tags: Set<String>, private val path: String) : PropertiesVisitor<PropertiesRetriever.Result>(tags) {
    class Result(val key: String, val element: SCElement?, val depth: UInt)

    override fun visitNull(ctx: String): Result =
        Result(ctx, SCNull)

    override fun visitPrimitive(prim: SCPrimitive, ctx: String): Result =
        Result(ctx, prim)

    override fun visitList(list: SCList, ctx: String): Result = list.withIndex().findCandidate(Result(ctx, list)) { (i, e) ->
        e.accept(this, join(ctx, i.toString(), ":"))
    }

    override fun visitMap(map: SCMap, ctx: String): Result = map.entries.findCandidate(Result(ctx, map)) { (k, v) ->
        v.accept(this, join(ctx, k, ":"))
    }

    private fun Result(ctx: String, element: SCElement?): Result {
        val cut = cut(ctx)
        return Result(cut, element.takeIf { cut == path }, ctx.pathDepth)
    }
}

internal inline fun <T> Iterable<T>.findCandidate(
    initial: PropertiesRetriever.Result = PropertiesRetriever.Result("", null, 0u),
    mapping: (T) -> PropertiesRetriever.Result?
): PropertiesRetriever.Result {
    var candidate = initial
    for (element in this) {
        val result = mapping(element) ?: continue
        if (result.element != null && result.depth >= candidate.depth) candidate = result
    }
    return candidate
}

internal val String.pathDepth: UInt
    get() = if (isEmpty()) 0u else count { it == ':' }.toUInt() + 1u

internal fun isValidPath(key: String): Boolean =
    !(key.isEmpty() || key.startsWith(':') || key.endsWith(':') || "::" in key)
