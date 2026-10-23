package com.exteragram.plugin.vpnparser.sources

import com.exteragram.plugin.vpnparser.model.VPNConfig
import io.ktor.client.HttpClient

interface VPNSource {
    val name: String
    val description: String
    val enabled: Boolean = true
    val protocols: List<VPNProtocol>
    
    suspend fun fetchConfigs(client: HttpClient): List<VPNConfig>
    
    fun isEnabled(): Boolean = enabled
}

enum class VPNProtocol {
    WIREGUARD,
    OPENVPN,
    VMESS,
    VLESS,
    TROJAN,
    SHADOWSOCKS,
    IKEV2,
    CISCO_ANYCONNECT
}
