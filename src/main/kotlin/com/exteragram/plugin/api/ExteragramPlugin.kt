package com.exteragram.plugin.api

/**
 * Main plugin interface. All Exteragram plugins must implement this.
 */
interface ExteragramPlugin {
    fun getInfo(): PluginInfo
    fun onEnable(context: PluginContext)
    fun onDisable(context: PluginContext)
}
