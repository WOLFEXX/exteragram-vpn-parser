package com.exteragram.plugin.vpnparser.sources

import com.exteragram.plugin.vpnparser.model.VPNConfig
import com.exteragram.plugin.vpnparser.model.VPNProtocol
import com.exteragram.plugin.vpnparser.model.AuthConfig
import com.exteragram.plugin.vpnparser.model.AuthType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GitHubWireGuardSource : VPNSource {
    override val name = "GitHub-WireGuard"
    override val description = "WireGuard configs from GitHub repositories"
    override val protocols = listOf(VPNProtocol.WIREGUARD)
    
    private val repos = listOf(
        "v2fly/free-v2ray",
        "Alvin9999/new-pac",
        "freefq/free"
    )
    
    override suspend fun fetchConfigs(client: HttpClient): List<VPNConfig> = withContext(Dispatchers.IO) {
        val configs = mutableListOf<VPNConfig>()
        for (repo in repos) {
            configs.addAll(fetchFromRepo(client, repo))
        }
        return@withContext configs
    }
    
    private suspend fun fetchFromRepo(client: HttpClient, repo: String): List<VPNConfig> {
        val configs = mutableListOf<VPNConfig>()
        try {
            val apiUrl = "https://api.github.com/repos/$repo/contents/"
            val response = client.get<String> { url(apiUrl) }
            val items = Json.decodeFromString<List<JsonObject>>(response)
            
            for (item in items) {
                val name = item.getString("name")
                val downloadUrl = item.getString("download_url")
                if (name.endsWith(".conf") || name.contains("wireguard", ignoreCase = true)) {
                    try {
                        val confContent = client.get<String> { url(downloadUrl) }
                        val endpoint = extractEndpoint(confContent)
                        configs.add(VPNConfig(
                            name = "GH-$repo-$name",
                            protocol = VPNProtocol.WIREGUARD,
                            server = endpoint,
                            port = 51820,
                            auth = AuthConfig(AuthType.PSK, mapOf()),
                            rawConfig = confContent,
                            source = name
                        ))
                    } catch (e: Exception) {
                    }
                }
            }
        } catch (e: Exception) {
        }
        return configs
    }
    
    private fun extractEndpoint(content: String): String {
        val pattern = "Endpoint\\s*=\\s*([^:\\s]+):(\\d+)".toRegex()
        val match = pattern.find(content)
        return match?.groupValues?.get(1) ?: "0.0.0.0"
    }
}
