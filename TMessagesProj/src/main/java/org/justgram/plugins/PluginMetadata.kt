package org.justgram.plugins

import androidx.annotation.Keep

@Keep
data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    var isEnabled: Boolean = true,
    var isQuarantined: Boolean = false,
    var lastError: String? = null,
    val pluginInstance: IPlugin? = null
)
