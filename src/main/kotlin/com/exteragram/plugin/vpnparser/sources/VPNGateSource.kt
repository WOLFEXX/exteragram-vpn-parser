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
import java.util.Base64

class VPNGateSource : VPNSource {
    override val name = "VPNGate"
    override val description = "OpenVPN configs from vpngate.net public API"
    override val enabled = true
    override val protocols = listOf(VPNProtocol.OPENVPN)
    
    override suspend fun fetchConfigs(client: HttpClient): List<VPNConfig> = withContext(Dispatchers.IO) {
        val url = "http://www.vpngate.net/api/iphone/"
        val response: HttpResponse = client.get { url(url) }
        val csv = response.bodyAsText()
        parseVPNGateCSV(csv)
    }
    
    private fun parseVPNGateCSV(csv: String): List<VPNConfig> {
        val configs = mutableListOf<VPNConfig>()
        val lines = csv.trim().split('\n')
        if (lines.size < 2) return configs
        
        for (line in lines.drop(1)) {
            if (line.startsWith("*") || line.trim().isEmpty()) continue
            val parts = line.split(',')
            if (parts.size < 15) continue
            
            try {
                val ip = parts[1].trim()
                val port = parts[2].trim().toIntOrNull() ?: 1194
                val country = parts[6].trim()
                val ovpnB64 = parts[14].trim()
                
                val ovpn = try {
                    String(Base64.getDecoder().decode(ovpnB64))
                } catch (e: Exception) {
                    continue
                }
                
                configs.add(VPNConfig(
                    name = "VPNGate-$country-$ip",
                    protocol = VPNProtocol.OPENVPN,
                    server = ip,
                    port = port,
                    country = country,
                    auth = AuthConfig(AuthType.CERTIFICATE, mapOf()),
                    rawConfig = ovpn,
                    source = name
                ))
            } catch (e: Exception) {
            }
        }
        return configs
    }
}
