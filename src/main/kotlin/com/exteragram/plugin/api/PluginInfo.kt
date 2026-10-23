package com.exteragram.plugin.api

data class PluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val category: Category = Category.UTILS
) {
    enum class Category {
        UTILS,
        FUN,
        ADMIN,
        ECONOMY,
        MODERATION,
        OTHER
    }
}
