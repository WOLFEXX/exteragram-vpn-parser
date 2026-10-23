package com.exteragram.plugin.vpnparser.sources

import com.exteragram.plugin.vpnparser.model.VPNConfig
import com.exteragram.plugin.vpnparser.model.VPNProtocol
import com.exteragram.plugin.vpnparser.model.AuthConfig
import com.exteragram.plugin.vpnparser.model.AuthType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.Base64

class V2RaySubscriptionSource(
    private val subscriptionUrls: List<String>
) : VPNSource {
    override val name = "V2Ray-Subscription"
    override val description = "VMess/VLess/Trojan/Shadowsocks from subscription links"
    override val enabled = true
    override val protocols = listOf(VPNProtocol.VMESS, VPNProtocol.VLESS, VPNProtocol.TROJAN, VPNProtocol.SHADOWSOCKS)
    
    override suspend fun fetchConfigs(client: HttpClient): List<VPNConfig> = withContext(Dispatchers.IO) {
        val configs = mutableListOf<VPNConfig>()
        for (url in subscriptionUrls) {
            configs.addAll(fetchFromUrl(client, url))
        }
        return@withContext configs
    }
    
    private suspend fun fetchFromUrl(client: HttpClient, url: String): List<VPNConfig> {
        val configs = mutableListOf<VPNConfig>()
        try {
            val response: HttpResponse = client.get { url(url) }
            var text = response.bodyAsText()
            // Try base64 decode
            try {
                text = String(Base64.getDecoder().decode(text.trim()))
            } catch (e: Exception) {
            }
            
            for (line in text.lines()) {
                val parsed = parseUri(line.trim())
                if (parsed != null) configs.add(parsed)
            }
        } catch (e: Exception) {
        }
        return configs
    }
    
    private fun parseUri(uri: String): VPNConfig? {
        return when {
            uri.startsWith("vmess://") -> parseVMess(uri)
            uri.startsWith("vless://") -> parseVLess(uri)
            uri.startsWith("trojan://") -> parseTrojan(uri)
            uri.startsWith("ss://") -> parseShadowsocks(uri)
            else -> null
        }
    }
    
    private fun parseVMess(uri: String): VPNConfig? {
        try {
            val b64 = uri.substring(8)
            val padded = b64.padEnd(b64.length + (4 - b64.length % 4) % 4, '=')
            val json = String(Base64.getDecoder().decode(padded))
            val obj = Json.decodeFromString<JsonObject>(json)
            return VPNConfig(
                name = obj["ps"]?.jsonPrimitive?.content ?: "VMess",
                protocol = VPNProtocol.VMESS,
                server = obj["add"]?.jsonPrimitive?.content ?: "",
                port = obj["port"]?.jsonPrimitive?.content?.toIntOrNull() ?: 443,
                auth = AuthConfig(AuthType.UUID, obj.toStringMap()),
                rawConfig = uri,
                source = name
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun parseVLess(uri: String): VPNConfig? {
        val pattern = "vless://([^@]+)@([^:]+):(\\d+)\\??([^#]*)#?(.*)".toRegex()
        val match = pattern.matchEntire(uri)
        if (match == null) return null
        val (uuid, host, port, params, name) = match.destructured
        return VPNConfig(
            name = if (name.isNotBlank()) name else "VLESS-$host",
            protocol = VPNProtocol.VLESS,
            server = host,
            port = port.toIntOrNull() ?: 443,
            auth = AuthConfig(AuthType.UUID, mapOf("uuid" to uuid, "params" to params)),
            rawConfig = uri,
            source = name
        )
    }
    
    private fun parseTrojan(uri: String): VPNConfig? {
        val pattern = "trojan://([^@]+)@([^:]+):(\\d+)\\??([^#]*)#?(.*)".toRegex()
        val match = pattern.matchEntire(uri)
        if (match == null) return null
        val (password, host, port, params, name) = match.destructured
        return VPNConfig(
            name = if (name.isNotBlank()) name else "Trojan-$host",
            protocol = VPNProtocol.TROJAN,
            server = host,
            port = port.toIntOrNull() ?: 443,
            auth = AuthConfig(AuthType.TOKEN, mapOf("password" to password, "params" to params)),
            rawConfig = uri,
            source = name
        )
    }
    
    private fun parseShadowsocks(uri: String): VPNConfig? {
        try {
            val withoutScheme = uri.substring(5)
            val parts = withoutScheme.split("#")
            val encoded = parts[0]
            val name = if (parts.size > 1) parts[1] else ""
            val padded = encoded.padEnd(encoded.length + (4 - encoded.length % 4) % 4, '=')
            val decoded = String(Base64.getDecoder().decode(padded))
            val authHost = decoded.split("@")
            val methodPass = authHost[0].split(":")
            val hostPort = authHost[1].split(":")
            return VPNConfig(
                name = name,
                protocol = VPNProtocol.SHADOWSOCKS,
                server = hostPort[0],
                port = hostPort[1].toIntOrNull() ?: 443,
                auth = AuthConfig(AuthType.USERPASS, mapOf("method" to methodPass[0], "password" to methodPass[1])),
                rawConfig = uri,
                source = name
            )
        } catch (e: Exception) {
            return null
        }
    }
}
