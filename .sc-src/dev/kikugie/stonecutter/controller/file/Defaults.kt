package dev.kikugie.stonecutter.controller.file

import dev.kikugie.stitcher.antlr.scanner.HashStyleScanner
import dev.kikugie.stitcher.antlr.scanner.SlashStyleScanner
import dev.kikugie.stitcher.transformer.impl.SlashStarCommentStrategy
import dev.kikugie.stitcher.transformer.impl.StarRemoveCommentStrategy

internal fun FileHandlerContainer.configureDefaults() {
    configureJava()
    configureKotlin()
    configureShader()
    configureHash()
}

private fun FileHandlerContainer.configureJava(): Unit = configure("java", "scala", "sc", "groovy", "gradle", "json5") {
    useSlashScanner(nested = false)

    commenter.set(SlashStarCommentStrategy(true))
    uncommenter.set(StarRemoveCommentStrategy)
}

private fun FileHandlerContainer.configureKotlin(): Unit = configure("kt", "kts") {
    useSlashScanner(nested = true)

    commenter.set(SlashStarCommentStrategy(false))
    uncommenter.set(StarRemoveCommentStrategy)
}

private fun FileHandlerContainer.configureShader(): Unit = configure("fsh", "vsh") {
    useSlashScanner(nested = false)

    commenter.set(line("//"))
    uncommenter.set(StarRemoveCommentStrategy)
}

private fun HandlerBuilder.useSlashScanner(nested: Boolean): Unit = scanner {
    if (!nested) lexer.set(::SlashStyleScanner)
    else lexer.set { SlashStyleScanner(it).apply { nestMultiLineComments = true } }

    openers(SlashStyleScanner.SLASH_COMMENT_START, SlashStyleScanner.STAR_COMMENT_START)
    closers(SlashStyleScanner.SLASH_COMMENT_END, SlashStyleScanner.STAR_COMMENT_END)
}

private fun FileHandlerContainer.configureHash(): Unit = configure("cfg", "aw", "accesswidener", "ct", "classtweaker", "yml", "yaml") {
    scanner {
        lexer.set(::HashStyleScanner)
        openers(HashStyleScanner.HASH_COMMENT_START)
        closers(HashStyleScanner.HASH_COMMENT_END)
    }

    commenter.set(line("#"))
}