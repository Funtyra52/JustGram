package org.justgram.ghost

import android.content.Context
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader

object JustGramGhostConfig {

    private const val PREFS_NAME = "justgram_ghost_config"

    private const val KEY_GHOST_MODE_MASTER = "ghost_mode_master"
    private const val KEY_HIDE_ONLINE = "ghost_hide_online"
    private const val KEY_HIDE_TYPING = "ghost_hide_typing"
    private const val KEY_HIDE_READ_MESSAGES = "ghost_hide_read_messages"
    private const val KEY_HIDE_READ_STORIES = "ghost_hide_read_stories"

    private val preferences: SharedPreferences
        get() = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var ghostModeMaster: Boolean
        get() = preferences.getBoolean(KEY_GHOST_MODE_MASTER, false)
        set(value) = preferences.edit().putBoolean(KEY_GHOST_MODE_MASTER, value).apply()

    var hideOnline: Boolean
        get() = preferences.getBoolean(KEY_HIDE_ONLINE, false)
        set(value) = preferences.edit().putBoolean(KEY_HIDE_ONLINE, value).apply()

    var hideTyping: Boolean
        get() = preferences.getBoolean(KEY_HIDE_TYPING, false)
        set(value) = preferences.edit().putBoolean(KEY_HIDE_TYPING, value).apply()

    var hideReadMessages: Boolean
        get() = preferences.getBoolean(KEY_HIDE_READ_MESSAGES, false)
        set(value) = preferences.edit().putBoolean(KEY_HIDE_READ_MESSAGES, value).apply()

    var hideReadStories: Boolean
        get() = preferences.getBoolean(KEY_HIDE_READ_STORIES, false)
        set(value) = preferences.edit().putBoolean(KEY_HIDE_READ_STORIES, value).apply()

    @JvmStatic
    fun isHideOnline(): Boolean {
        return ghostModeMaster || hideOnline
    }

    @JvmStatic
    fun isHideTyping(): Boolean {
        return ghostModeMaster || hideTyping
    }

    @JvmStatic
    fun isHideReadMessages(): Boolean {
        return ghostModeMaster || hideReadMessages
    }

    @JvmStatic
    fun isHideReadStories(): Boolean {
        return ghostModeMaster || hideReadStories
    }

    /**
     * Anti-delete policy confirmation:
     * JustGram strictly DOES NOT retain deleted messages on disk or in UI.
     */
    @JvmStatic
    fun isAntiDeleteAllowed(): Boolean = false
}
