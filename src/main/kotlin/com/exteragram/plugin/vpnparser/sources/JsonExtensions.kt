package com.exteragram.plugin.vpnparser.sources

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

fun JsonObject.toStringMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (key in this.keys) {
        val value = this[key]
        map[key] = when (value) {
            is JsonObject -> value.toString()
            else -> value?.jsonPrimitive?.content ?: value.toString()
        }
    }
    return map
}
