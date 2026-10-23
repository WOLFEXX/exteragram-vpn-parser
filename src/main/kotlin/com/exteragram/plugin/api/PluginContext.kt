package com.exteragram.plugin.api

import io.ktor.client.HttpClient

/**
 * Context provided to plugins when they are enabled/disabled.
 * Contains access to Exteragram services.
 */
interface PluginContext {
    val httpClient: HttpClient
    val commandManager: CommandManager
    val configManager: ConfigManager
    val logger: PluginLogger
}

interface CommandManager {
    fun registerCommand(command: Command)
    fun unregisterCommand(name: String)
}

interface ConfigManager {
    fun getConfig(key: String): String?
    fun setConfig(key: String, value: String)
}

interface PluginLogger {
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun debug(message: String)
}
