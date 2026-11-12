package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.ThemeMode

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hephaistos_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_MANUAL_PACKAGES = "manual_packages"
        private const val KEY_IS_GRID = "is_grid_layout"
    }

    fun getThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun getManualPackages(): Set<String> {
        return prefs.getStringSet(KEY_MANUAL_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setManualPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_MANUAL_PACKAGES, packages).apply()
    }

    fun toggleManualPackage(packageName: String): Boolean {
        val current = getManualPackages().toMutableSet()
        val willInclude = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        setManualPackages(current)
        return willInclude
    }

    fun isGridLayout(): Boolean {
        return prefs.getBoolean(KEY_IS_GRID, true)
    }

    fun setGridLayout(isGrid: Boolean) {
        prefs.edit().putBoolean(KEY_IS_GRID, isGrid).apply()
    }
}
