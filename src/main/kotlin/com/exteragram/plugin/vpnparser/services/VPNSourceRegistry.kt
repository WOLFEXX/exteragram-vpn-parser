package com.exteragram.plugin.vpnparser.services

import com.exteragram.plugin.vpnparser.sources.GitHubWireGuardSource
import com.exteragram.plugin.vpnparser.sources.VPNSource
import com.exteragram.plugin.vpnparser.sources.VPNGateSource
import com.exteragram.plugin.vpnparser.sources.V2RaySubscriptionSource

class VPNSourceRegistry {
    private val sources = mutableMapOf<String, VPNSource>()
    private val enabledSources = mutableSetOf<String>()
    
    init {
        registerSource(VPNGateSource())
        registerSource(GitHubWireGuardSource())
    }
    
    fun registerSource(source: VPNSource) {
        sources[source.name] = source
        enabledSources.add(source.name)
    }
    
    fun registerSubscriptionSource(urls: List<String>) {
        val source = V2RaySubscriptionSource(urls)
        registerSource(source)
    }
    
    fun getSource(name: String): VPNSource? = sources[name]
    
    fun getEnabledSources(): List<VPNSource> = enabledSources.mapNotNull { sources[it] }
    
    fun getAllSources(): List<VPNSource> = sources.values.toList()
    
    fun enableSource(name: String) {
        if (sources.containsKey(name)) {
            enabledSources.add(name)
        }
    }
    
    fun disableSource(name: String) {
        enabledSources.remove(name)
    }
    
    fun isEnabled(name: String): Boolean = enabledSources.contains(name)
}
