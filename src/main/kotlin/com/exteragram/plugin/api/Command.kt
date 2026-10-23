package com.exteragram.plugin.api

/**
 * Base interface for all commands.
 */
interface Command {
    val name: String
    val aliases: List<String>
    val description: String
    val usage: String
    val permission: String?

    suspend fun execute(context: CommandContext, args: List<String>): CommandResult
}

interface CommandContext {
    val sender: CommandSender
    val channel: CommandChannel
    val plugin: ExteragramPlugin

    fun sendMessage(message: String)
    fun reply(message: String)
}

interface CommandSender {
    val id: String
    val name: String
    val isAdmin: Boolean
    val permissions: Set<String>
}

interface CommandChannel {
    val id: String
    val name: String
    val type: ChannelType
}

enum class ChannelType {
    PRIVATE,
    GROUP,
    CHANNEL
}

sealed class CommandResult {
    data class Success(val message: String) : CommandResult()
    data class Error(val message: String) : CommandResult()

    companion object {
        fun success(message: String) = Success(message)
        fun error(message: String) = Error(message)
    }
}
