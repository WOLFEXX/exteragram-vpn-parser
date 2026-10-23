package com.exteragram.plugin.vpnparser.model

import kotlinx.serialization.Serializable

@Serializable
data class VPNConfig(
    val name: String,
    val protocol: VPNProtocol,
    val server: String,
    val port: Int,
    val country: String? = null,
    val auth: AuthConfig? = null,
    val rawConfig: String? = null,
    val source: String = "unknown",
    val lastChecked: Long? = null,
    val latencyMs: Long? = null,
    val isWorking: Boolean? = null
) {
    val endpoint: String
        get() = "$server:$port"
    
    val uriFormat: String
        get() = when (protocol) {
            VPNProtocol.WIREGUARD -> "wireguard://$endpoint?name=$name"
            VPNProtocol.OPENVPN -> "openvpn://$endpoint?name=$name"
            VPNProtocol.VMESS -> "vmess://$endpoint?name=$name"
            VPNProtocol.VLESS -> "vless://$endpoint?name=$name"
            VPNProtocol.TROJAN -> "trojan://$endpoint?name=$name"
            VPNProtocol.SHADOWSOCKS -> "shadowsocks://$endpoint?name=$name"
            VPNProtocol.IKEV2 -> "ikev2://$endpoint?name=$name"
            VPNProtocol.CISCO_ANYCONNECT -> "anyconnect://$endpoint?name=$name"
        }
}

@Serializable
enum class VPNProtocol {
    WIREGUARD,
    OPENVPN,
    VMESS,
    VLESS,
    TROJAN,
    SHADOWSOCKS,
    IKEV2,
    CISCO_ANYCONNECT,
    UNKNOWN
}

@Serializable
data class AuthConfig(
    val type: AuthType,
    val data: Map<String, String>
)

@Serializable
enum class AuthType {
    NONE,
    PSK,
    CERTIFICATE,
    USERPASS,
    UUID,
    TOKEN
}

@Serializable
data class VPNConfigList(
    val configs: List<VPNConfig>,
    val source: String,
    val count: Int = configs.size,
    val fetchedAt: Long = System.currentTimeMillis()
)
