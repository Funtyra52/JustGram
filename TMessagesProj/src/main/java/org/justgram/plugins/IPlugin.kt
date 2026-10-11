package org.justgram.plugins

import android.content.Context
import android.view.View

interface IPlugin {
    fun getId(): String
    fun getName(): String
    fun getVersion(): String
    fun getDescription(): String
    fun getAuthor(): String

    fun onInit(context: Context)
    fun onEnable()
    fun onDisable()

    /**
     * Called for UI hooks (e.g. "chat_menu", "profile_cell", "dialog_cell").
     * Must be lightweight to prevent microfreezes and FPS drops.
     */
    fun onUIHook(hookId: String, targetView: View?, extraData: Any?)
}
