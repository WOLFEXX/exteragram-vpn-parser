package com.exteragram.plugin.vpnparser.commands

import com.exteragram.plugin.api.command.Command
import com.exteragram.plugin.api.command.CommandContext
import com.exteragram.plugin.api.command.CommandResult
import com.exteragram.plugin.vpnparser.services.VPNSourceRegistry

class VPNSourcesCommand(
    private val sourceRegistry: VPNSourceRegistry
) : Command {
    override val name: String = "vpnsources"
    override val aliases: List<String> = listOf("vpnsrc", "vpnlist")
    override val description: String = "Manage VPN config sources"
    override val usage: String = "vpnsources [list|enable <name>|disable <name>|add-subscription <url>...]"
    override val permission: String? = null
    
    override suspend fun execute(context: CommandContext, args: List<String>): CommandResult {
        if (args.isEmpty() || args[0] == "list") {
            return listSources(context)
        }
        
        return when (args[0].lowercase()) {
            "enable" -> if (args.size > 1) enableSource(args[1]) else usageResult()
            "disable" -> if (args.size > 1) disableSource(args[1]) else usageResult()
            "add-subscription", "addsub" -> if (args.size > 1) addSubscription(args.drop(1)) else usageResult()
            else -> usageResult()
        }
    }
    
    private fun listSources(context: CommandContext): CommandResult {
        val sources = sourceRegistry.getAllSources()
        val sb = StringBuilder()
        sb.append("VPN Sources (${sources.size})\n")
        sb.append("─".repeat(60)).append("\n")
        sources.forEach { source ->
            val status = if (sourceRegistry.isEnabled(source.name)) "✓" else "✗"
            sb.append("$status ${source.name}\n")
            sb.append("  ${source.description}\n")
            sb.append("  Protocols: ${source.protocols.joinToString(", ")}\n")
        }
        sb.append("─".repeat(60))
        return CommandResult.success(sb.toString())
    }
    
    private fun enableSource(name: String): CommandResult {
        sourceRegistry.enableSource(name)
        return CommandResult.success("Source '$name' enabled")
    }
    
    private fun disableSource(name: String): CommandResult {
        sourceRegistry.disableSource(name)
        return CommandResult.success("Source '$name' disabled")
    }
    
    private fun addSubscription(urls: List<String>): CommandResult {
        sourceRegistry.registerSubscriptionSource(urls)
        return CommandResult.success("Added subscription source with ${urls.size} URL(s)")
    }
    
    private fun usageResult(): CommandResult {
        return CommandResult.error("Usage: $usage")
    }
}
