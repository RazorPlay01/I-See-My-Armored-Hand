package dev.kikugie.stonecutter.controller.task

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.util.overwriteText
import dev.kikugie.stonecutter.util.toPath
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.nio.file.Path

/**Implementation class for `stonecutterSwitchTo...` tasks.*/
@DisableCachingByDefault(because = "Switching the active version should reevaluate the sources")
public sealed class StonecutterSwitchTask : DefaultTask() {
    /**The modified file.*/
    @get:OutputFile
    public abstract val file: RegularFileProperty

    /**The applied version.*/
    @get:Input
    public abstract val version: Property<Identifier>
}

@DisableCachingByDefault(because = "Switching the active version should reevaluate the sources")
internal abstract class StonecutterExternalSwitchTask : StonecutterSwitchTask() {
    @TaskAction
    fun run() = file.toPath().overwriteText(version.get())
}

@DisableCachingByDefault(because = "Switching the active version should reevaluate the sources")
internal abstract class StonecutterScriptSwitchTask : StonecutterSwitchTask() {
    @get:Input
    abstract val writer: Property<(Path, Identifier) -> Unit>

    @TaskAction
    fun run() = writer.get()(file.toPath(), version.get())
}