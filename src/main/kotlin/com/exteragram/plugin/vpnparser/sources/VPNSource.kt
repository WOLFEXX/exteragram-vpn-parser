package com.exteragram.plugin.vpnparser.sources

import com.exteragram.plugin.vpnparser.model.VPNConfig
import com.exteragram.plugin.vpnparser.model.VPNProtocol
import io.ktor.client.HttpClient

interface VPNSource {
    val name: String
    val description: String
    val enabled: Boolean
    val protocols: List<VPNProtocol>
    
    suspend fun fetchConfigs(client: HttpClient): List<VPNConfig>
    
    fun isEnabled(): Boolean = enabled
}
