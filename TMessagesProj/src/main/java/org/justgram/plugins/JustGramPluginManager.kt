package org.justgram.plugins

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object JustGramPluginManager {

    private const val TAG = "JustGramPluginManager"
    private const val PREFS_NAME = "justgram_plugins_prefs"

    private const val KEY_STARTUP_CRASH = "startup_crash_detected"
    private const val KEY_STARTUP_IN_PROGRESS = "startup_in_progress"
    private const val KEY_SAFE_MODE = "safe_mode_enabled"

    private val pluginsMap = ConcurrentHashMap<String, PluginMetadata>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val prefs: SharedPreferences
        get() = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isSafeMode: Boolean
        get() = prefs.getBoolean(KEY_SAFE_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_SAFE_MODE, value).apply()

    fun isPluginsActive(): Boolean {
        return !isSafeMode
    }

    fun init(context: Context) {
        checkStartupCrashState()

        if (isSafeMode) {
            FileLog.d("$TAG: JustGram running in Safe Mode - plugins disabled")
            return
        }

        loadPlugins(context)
    }

    private fun checkStartupCrashState() {
        val wasInProgress = prefs.getBoolean(KEY_STARTUP_IN_PROGRESS, false)
        if (wasInProgress) {
            // Previous start crashed before completing startup window!
            prefs.edit().putBoolean(KEY_STARTUP_CRASH, true).apply()
            FileLog.e(TAG, "Detected crash during previous startup! Setting startup_crash_detected=true")
        }

        // Mark startup in progress
        prefs.edit().putBoolean(KEY_STARTUP_IN_PROGRESS, true).apply()

        // After 6 seconds of successful running, mark startup as stable
        mainHandler.postDelayed({
            prefs.edit().putBoolean(KEY_STARTUP_IN_PROGRESS, false).apply()
        }, 6000)
    }

    fun shouldShowSafeModeDialog(): Boolean {
        return prefs.getBoolean(KEY_STARTUP_CRASH, false)
    }

    fun checkSafeModeDialog(activity: Activity) {
        if (!shouldShowSafeModeDialog() || activity.isFinishing) return

        AndroidUtilities.runOnUIThread {
            try {
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Безопасный режим (Safe Mode)")
                builder.setMessage("Обнаружен сбой при запуске. Запустить JustGram в безопасном режиме (без плагинов)?")

                builder.setPositiveButton("Безопасный режим") { _, _ ->
                    isSafeMode = true
                    prefs.edit().putBoolean(KEY_STARTUP_CRASH, false).apply()
                    disableAllPlugins()
                }

                builder.setNegativeButton("Обычный запуск") { _, _ ->
                    isSafeMode = false
                    prefs.edit().putBoolean(KEY_STARTUP_CRASH, false).apply()
                    loadPlugins(activity.applicationContext)
                }

                builder.setCancelable(false)
                builder.show()
            } catch (t: Throwable) {
                FileLog.e(TAG, t)
            }
        }
    }

    private fun loadPlugins(context: Context) {
        // Safe loading of plugins with isolated try-catch per plugin
        try {
            val pluginsDir = File(context.filesDir, "plugins")
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs()
            }

            // Register standard core UI plugin as reference
            registerCorePlugin(context)

        } catch (t: Throwable) {
            FileLog.e(TAG, "Fatal error initializing plugin subsystem", t)
        }
    }

    private fun registerCorePlugin(context: Context) {
        val corePlugin = object : IPlugin {
            override fun getId(): String = "org.justgram.core_enhancer"
            override fun getName(): String = "JustGram UI Enhancer"
            override fun getVersion(): String = "0.1.0"
            override fun getDescription(): String = "Оптимизация отрисовки и кэширование элементов JustGram"
            override fun getAuthor(): String = "JustGram Team"

            override fun onInit(context: Context) {}
            override fun onEnable() {}
            override fun onDisable() {}
            override fun onUIHook(hookId: String, targetView: android.view.View?, extraData: Any?) {}
        }

        registerPlugin(context, corePlugin)
    }

    fun registerPlugin(context: Context, plugin: IPlugin) {
        try {
            val meta = PluginMetadata(
                id = plugin.getId(),
                name = plugin.getName(),
                version = plugin.getVersion(),
                author = plugin.getAuthor(),
                description = plugin.getDescription(),
                isEnabled = true,
                isQuarantined = false,
                pluginInstance = plugin
            )

            try {
                plugin.onInit(context)
                plugin.onEnable()
                pluginsMap[plugin.getId()] = meta
                FileLog.d("$TAG: Plugin initialized successfully: ${plugin.getId()}")
            } catch (t: Throwable) {
                FileLog.e(TAG, "Error initializing plugin ${plugin.getId()}", t)
                meta.isQuarantined = true
                meta.isEnabled = false
                meta.lastError = t.message
                pluginsMap[plugin.getId()] = meta
            }
        } catch (t: Throwable) {
            FileLog.e(TAG, t)
        }
    }

    fun getEnabledPlugins(): List<PluginMetadata> {
        if (isSafeMode) return emptyList()
        return pluginsMap.values.filter { it.isEnabled && !it.isQuarantined }
    }

    fun getAllPlugins(): List<PluginMetadata> {
        return pluginsMap.values.toList()
    }

    fun togglePlugin(pluginId: String, enable: Boolean) {
        val meta = pluginsMap[pluginId] ?: return
        if (enable && meta.isQuarantined) {
            // Reset quarantine on manual retry
            meta.isQuarantined = false
            meta.lastError = null
        }
        meta.isEnabled = enable
        try {
            if (enable) {
                meta.pluginInstance?.onEnable()
            } else {
                meta.pluginInstance?.onDisable()
            }
        } catch (t: Throwable) {
            FileLog.e(TAG, t)
            meta.isQuarantined = true
            meta.lastError = t.message
        }
    }

    private fun disableAllPlugins() {
        for (meta in pluginsMap.values) {
            try {
                meta.isEnabled = false
                meta.pluginInstance?.onDisable()
            } catch (ignore: Throwable) {}
        }
    }
}
