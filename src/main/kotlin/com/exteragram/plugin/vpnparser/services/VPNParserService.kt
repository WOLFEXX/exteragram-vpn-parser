package com.exteragram.plugin.vpnparser.services

import com.exteragram.plugin.vpnparser.model.VPNConfig
import com.exteragram.plugin.vpnparser.model.VPNConfigList
import com.exteragram.plugin.vpnparser.model.VPNProtocol
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import java.util.concurrent.atomic.AtomicInteger

class VPNParserService(
    private val httpClient: HttpClient,
    private val sourceRegistry: VPNSourceRegistry
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val validatedCount = AtomicInteger(0)
    private val failedCount = AtomicInteger(0)
    
    suspend fun fetchAllConfigs(
        merge: Boolean = true,
        deduplicate: Boolean = true
    ): List<VPNConfig> {
        val allConfigs = mutableListOf<VPNConfig>()
        val sources = sourceRegistry.getEnabledSources()
        
        val jobs = sources.map { source ->
            scope.async {
                try {
                    val configs = source.fetchConfigs(httpClient)
                    configs.map { it.copy(source = source.name) }
                } catch (e: Exception) {
                    emptyList<VPNConfig>()
                }
            }
        }
        
        jobs.awaitAll().forEach { allConfigs.addAll(it) }
        
        var result: List<VPNConfig> = allConfigs
        if (deduplicate) {
            result = deduplicateConfigs(result)
        }
        
        return result
    }
    
    suspend fun fetchFromSource(sourceName: String): List<VPNConfig> {
        val source = sourceRegistry.getSource(sourceName)
            ?: throw IllegalArgumentException("Source not found: $sourceName")
        return try {
            source.fetchConfigs(httpClient).map { it.copy(source = source.name) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun fetchByProtocol(protocol: VPNProtocol): List<VPNConfig> {
        val all = fetchAllConfigs()
        return all.filter { it.protocol == protocol }
    }
    
    suspend fun fetchByCountry(country: String): List<VPNConfig> {
        val all = fetchAllConfigs()
        return all.filter { it.country?.equals(country, ignoreCase = true) == true }
    }
    
    private fun deduplicateConfigs(configs: List<VPNConfig>): List<VPNConfig> {
        return configs.distinctBy { "${it.server}:${it.port}:${it.protocol}" }
    }
    
    fun getStats(): VPNParserStats = VPNParserStats(
        validatedCount = validatedCount.get(),
        failedCount = failedCount.get()
    )
    
    fun close() {
        scope.coroutineContext.cancel()
    }
}

data class VPNParserStats(
    val validatedCount: Int,
    val failedCount: Int
)
