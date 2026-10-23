package com.exteragram.plugin.vpnparser.sources

import kotlinx.serialization.json.JsonObject

fun JsonObject.toStringMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (key in this.keys) {
        val value = this[key]
        map[key] = value?.jsonPrimitive?.content ?: value.toString()
    }
    return map
}
