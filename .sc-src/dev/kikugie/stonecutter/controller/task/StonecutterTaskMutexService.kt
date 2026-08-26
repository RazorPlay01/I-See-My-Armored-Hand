package dev.kikugie.stonecutter.controller.task

import dev.kikugie.stonecutter.StonecutterInternalAPI
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

@StonecutterInternalAPI
public interface StonecutterTaskMutexService : BuildService<BuildServiceParameters.None> {
    public companion object {
        @JvmStatic public fun create(gradle: Gradle, task: String): Provider<StonecutterTaskMutexService> {
            require(task.isNotBlank()) { "Task name must be non-empty" }
            val name = task.take(task.length - 1) + task.last().uppercaseChar()
            return gradle.sharedServices.registerIfAbsent(name, StonecutterTaskMutexService::class.java) {
                maxParallelUsages.set(1)
            }
        }
    }
}