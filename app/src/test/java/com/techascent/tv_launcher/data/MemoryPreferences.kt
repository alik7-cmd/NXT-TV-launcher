package com.techascent.tv_launcher.data

import android.content.SharedPreferences
import java.lang.reflect.Proxy

/** In-memory SharedPreferences contract for testing repository encoding and change notifications. */
internal class MemoryPreferences {
    private val values = mutableMapOf<String, Any?>()
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    val preferences: SharedPreferences = Proxy.newProxyInstance(
        SharedPreferences::class.java.classLoader,
        arrayOf(SharedPreferences::class.java),
    ) { _, method, args ->
        when (method.name) {
            "getString", "getStringSet", "getBoolean" -> values[args[0]] ?: args[1]
            "registerOnSharedPreferenceChangeListener" -> {
                listeners += args[0] as SharedPreferences.OnSharedPreferenceChangeListener
                null
            }
            "unregisterOnSharedPreferenceChangeListener" -> {
                listeners -= args[0] as SharedPreferences.OnSharedPreferenceChangeListener
                null
            }
            "edit" -> editor()
            else -> error("Unexpected preferences call: ${method.name}")
        }
    } as SharedPreferences

    private fun editor(): SharedPreferences.Editor {
        val changes = mutableMapOf<String, Any?>()
        return Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "putString", "putStringSet", "putBoolean" -> {
                    changes[args[0] as String] = (args[1] as? Set<*>)?.toSet() ?: args[1]
                    proxy
                }
                "apply", "commit" -> {
                    val changed = changes.keys.filter { values[it] != changes[it] }
                    values.putAll(changes)
                    changed.forEach { key -> listeners.toList().forEach { it.onSharedPreferenceChanged(preferences, key) } }
                    if (method.name == "commit") true else null
                }
                else -> error("Unexpected editor call: ${method.name}")
            }
        } as SharedPreferences.Editor
    }
}
