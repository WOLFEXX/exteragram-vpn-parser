package com.exteragram.plugin.vpnparser

import com.exteragram.plugin.api.ExteragramPlugin
import com.exteragram.plugin.api.PluginContext
import com.exteragram.plugin.api.PluginInfo
import com.exteragram.plugin.vpnparser.commands.VPNParseCommand
import com.exteragram.plugin.vpnparser.commands.VPNSourcesCommand
import com.exteragram.plugin.vpnparser.services.VPNParserService
import com.exteragram.plugin.vpnparser.services.VPNSourceRegistry

class VPNParserPlugin : ExteragramPlugin {
    private lateinit var vpnParserService: VPNParserService
    private lateinit var sourceRegistry: VPNSourceRegistry
    
    override fun getInfo(): PluginInfo = PluginInfo(
        id = "vpn-parser",
        name = "VPN Parser",
        version = "1.0.0",
        description = "Parse VPN configs from open sources (VPNGate, GitHub, V2Ray subscriptions)",
        author = "Exteragram",
        category = PluginInfo.Category.UTILS
    )
    
    override fun onEnable(context: PluginContext) {
        sourceRegistry = VPNSourceRegistry()
        vpnParserService = VPNParserService(context.httpClient, sourceRegistry)
        
        context.commandManager.registerCommand(VPNParseCommand(vpnParserService))
        context.commandManager.registerCommand(VPNSourcesCommand(sourceRegistry))
    }
    
    override fun onDisable(context: PluginContext) {
        vpnParserService.close()
    }
}
