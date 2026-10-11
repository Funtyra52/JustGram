package org.justgram.plugins

import android.view.View
import org.telegram.messenger.FileLog
import java.lang.reflect.Method
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap

object PluginUIHookManager {

    private const val TAG = "PluginUIHook"

    // Reflection cache to eliminate expensive Class.getMethod calls on Main thread
    private val methodCache = ConcurrentHashMap<String, Method?>()

    // View state hash cache for dirty checking (prevents redundant relayouts and microfreezes)
    private val viewStateHash = WeakHashMap<View, Int>()

    // Global scroll flag: when true, skip non-critical onBind / onDraw hooks to maintain 60/120 FPS
    @Volatile
    var isFastScrolling: Boolean = false

    @JvmStatic
    fun invokeUIHook(
        hookId: String,
        targetView: View?,
        extraData: Any?,
        isEssential: Boolean = false
    ) {
        if (!JustGramPluginManager.isPluginsActive()) {
            return
        }

        // Drop redundant UI hooks during fast scrolling to prevent FPS drops
        if (isFastScrolling && !isEssential) {
            return
        }

        // Dirty-check: if targetView and data haven't changed, skip to avoid layout thrashing
        if (targetView != null && !isEssential) {
            val stateHash = (hookId.hashCode() * 31) xor (extraData?.hashCode() ?: 0)
            val prevHash = viewStateHash[targetView]
            if (prevHash != null && prevHash == stateHash) {
                return
            }
            viewStateHash[targetView] = stateHash
        }

        val activePlugins = JustGramPluginManager.getEnabledPlugins()
        if (activePlugins.isEmpty()) return

        for (meta in activePlugins) {
            val plugin = meta.pluginInstance ?: continue
            try {
                plugin.onUIHook(hookId, targetView, extraData)
            } catch (t: Throwable) {
                FileLog.e(TAG, "Plugin ${meta.id} threw exception on UI hook $hookId", t)
                // If a plugin repeatedly fails in UI hooks, safely quarantine it
                meta.lastError = t.message
            }
        }
    }

    @JvmStatic
    fun getCachedMethod(clazz: Class<*>, methodName: String, vararg paramTypes: Class<*>): Method? {
        val key = "${clazz.name}#$methodName"
        return methodCache.computeIfAbsent(key) {
            try {
                val m = clazz.getDeclaredMethod(methodName, *paramTypes)
                m.isAccessible = true
                m
            } catch (t: Throwable) {
                null
            }
        }
    }

    @JvmStatic
    fun clearCache() {
        methodCache.clear()
        viewStateHash.clear()
    }
}
