package org.justgram.core

import android.content.Context
import org.justgram.badges.JustGramBadgesManager
import org.justgram.plugins.JustGramPluginManager

object JustGramCore {

    private var initialized = false

    @JvmStatic
    fun init(context: Context) {
        if (initialized) return
        initialized = true

        JustGramBadgesManager.init(context)
        JustGramPluginManager.init(context)
    }
}
