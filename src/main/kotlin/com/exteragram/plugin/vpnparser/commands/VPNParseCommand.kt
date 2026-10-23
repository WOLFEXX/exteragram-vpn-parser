package com.exteragram.plugin.vpnparser.commands

import com.exteragram.plugin.api.command.Command
import com.exteragram.plugin.api.command.CommandContext
import com.exteragram.plugin.api.command.CommandResult
import com.exteragram.plugin.vpnparser.model.VPNProtocol
import com.exteragram.plugin.vpnparser.services.VPNParserService

class VPNParseCommand(
    private val vpnParserService: VPNParserService
) : Command {
    override val name: String = "vpnparse"
    override val aliases: List<String> = listOf("vpn", "vpns")
    override val description: String = "Parse VPN configs from open sources"
    override val usage: String = "vpnparse [--source <name>] [--protocol <wireguard|openvpn|vmess|vless|trojan|shadowsocks>] [--country <code>] [--limit <n>]"
    override val permission: String? = null
    
    override suspend fun execute(context: CommandContext, args: List<String>): CommandResult {
        val parsed = parseArgs(args)
        val source = parsed["source"] as String?
        val protocolStr = parsed["protocol"] as String?
        val country = parsed["country"] as String?
        val limit = (parsed["limit"] as String?)?.toIntOrNull() ?: Int.MAX_VALUE
        
        context.sendMessage("Fetching VPN configs...")
        
        var configs = if (source != null) {
            vpnParserService.fetchFromSource(source)
        } else if (protocolStr != null) {
            val protocol = parseProtocol(protocolStr)
            if (protocol == null) {
                return CommandResult.error("Unknown protocol: $protocolStr")
            }
            vpnParserService.fetchByProtocol(protocol)
        } else if (country != null) {
            vpnParserService.fetchByCountry(country)
        } else {
            vpnParserService.fetchAllConfigs()
        }
        
        if (configs.isEmpty()) {
            return CommandResult.success("No VPN configs found")
        }
        
        configs = configs.take(limit)
        
        val sb = StringBuilder()
        sb.append("Found ${configs.size} VPN configs\n")
        sb.append("─".repeat(60)).append("\n")
        
        configs.forEach { config ->
            val status = when (config.isWorking) {
                true -> "✓"
                false -> "✗"
                else -> "?"
            }
            val latency = config.latencyMs?.let { "(${it}ms)" } ?: ""
            val countryInfo = config.country?.let { " [$it]" } ?: ""
            sb.append("$status ${config.protocol}://${config.endpoint}$countryInfo $latency [${config.source}]\n")
            sb.append("  ${config.name}\n")
        }
        
        if (configs.size >= limit) {
            sb.append("... and more (limit: $limit)\n")
        }
        sb.append("─".repeat(60))
        
        return CommandResult.success(sb.toString())
    }
    
    private fun parseProtocol(str: String): VPNProtocol? = when (str.lowercase()) {
        "wireguard", "wg" -> VPNProtocol.WIREGUARD
        "openvpn", "ovpn" -> VPNProtocol.OPENVPN
        "vmess" -> VPNProtocol.VMESS
        "vless" -> VPNProtocol.VLESS
        "trojan" -> VPNProtocol.TROJAN
        "shadowsocks", "ss" -> VPNProtocol.SHADOWSOCKS
        "ikev2" -> VPNProtocol.IKEV2
        "anyconnect" -> VPNProtocol.CISCO_ANYCONNECT
        else -> null
    }
    
    private fun parseArgs(args: List<String>): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--source", "-s" -> if (i + 1 < args.size) {
                    map["source"] = args[i + 1]; i++
                }
                "--protocol", "-p" -> if (i + 1 < args.size) {
                    map["protocol"] = args[i + 1]; i++
                }
                "--country", "-c" -> if (i + 1 < args.size) {
                    map["country"] = args[i + 1]; i++
                }
                "--limit", "-l" -> if (i + 1 < args.size) {
                    map["limit"] = args[i + 1]; i++
                }
            }
            i++
        }
        return map
    }
}
