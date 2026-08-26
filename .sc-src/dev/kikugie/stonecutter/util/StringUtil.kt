package dev.kikugie.stonecutter.util

import dev.kikugie.commons.then
import dev.kikugie.stonecutter.controller.flag.StonecutterFlag

private val ASCII_ESCAPES: List<String> = buildList(128) {
    for (i in 0x00..0x0f) {
        add(i, "\\u000$i")
    }
    for (i in 0x10..0x1f) {
        add(i, "\\u00$i")
    }
    for (i in 0x20..0x7f) {
        add(i, i.toChar().toString())
    }
    set('\b'.code, "\\b")
    set('\t'.code, "\\t")
    set('\n'.code, "\\n")
    set(12, "\\f")
    set('\r'.code, "\\r")
    set('\"'.code, "\\\"")
    set('\\'.code, "\\\\")
}

internal fun join(a: String, b: String, separator: String): String =
    if (a.isEmpty()) b else "$a$separator$b"

internal fun join(vararg parts: String, separator: String): String = when (parts.size) {
    0 -> ""
    1 -> parts.first()
    2 -> join(parts[0], parts[1], separator)
    else -> parts.filter(String::isNotEmpty).joinToString(separator)
}

/**
 * Converts the character to its escaped representation based on the input flag.
 *
 * @param multiline Indicates whether to use multiline-friendly escaping. If true,
 * certain characters like newline (`\n`) and carriage return (`\r`) are converted
 * to their literal forms instead of escape sequences.
 */
public fun Char.escape(multiline: Boolean = false): String = when(this) {
    '\\' -> "\\\\"
    '\t' -> if (multiline) toString() else "\\t"
    '\n' -> if (multiline) toString() else "\\n"
    '\r' -> if (multiline) toString() else "\\r"
    else -> if (code >= 128) toString() else ASCII_ESCAPES[code]
}

/**
 * Escapes the characters in the string based on the input flag.
 *
 * @param multiline Specifies whether multiline-friendly escaping should be applied.
 * If true, certain characters like newline (`\n`) and carriage return (`\r`) are kept
 * in their literal forms instead of being escaped.
 */
public fun String.escape(multiline: Boolean = false): String = buildString {
    for (it in this@escape) append(it.escape(multiline))
}

/**
 * Returns a new string with escape sequences in the original string replaced by their corresponding characters.
 *
 * Common escape sequences such as `\n`, `\t`, `\b`, and Unicode escapes (e.g., `\u1234`) are processed.
 * Escape sequences that are not recognised are retained in their original form.
 */
public fun String.unescape(): String {
    if (isBlank()) return this

    val builder = StringBuilder(length)
    var index = 0
    while (index < length) {
        val char = get(index)

        if (char != '\\' || index == length - 1) {
            builder.append(char)
            index++
            continue
        }

        index += builder.next(this, index, get(index + 1))
    }
    return builder.toString()
}

private fun StringBuilder.next(input: String, index: Int, it: Char): Int = when (it) {
    '"', '\\', '/' -> append(it) then 2
    'b' -> append('\b') then 2
    'n' -> append('\n') then 2
    'r' -> append('\r') then 2
    't' -> append('\t') then 2
    'f' -> append('\u000c') then 2
    'u' -> if (index + 5 >= input.length) append("\\u") then 2
    else append(input.substring(index + 2, index + 6).toInt(16).toChar()) then 6
    'U' -> if (index + 9 >= input.length) append("\\U") then 2
    else append(input.substring(index + 2, index + 10).toInt(16).toChar()) then 6
    else -> append("\\$it") then 2
}

internal fun stonecutterProperty(key: String): String =
    "dev.kikugie.stonecutter.$key"

internal val StonecutterFlag<*>.propertyKey: String
    inline get() = stonecutterProperty(key)
