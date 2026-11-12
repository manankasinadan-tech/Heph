package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.example.model.EcosystemApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PackageManagerScanner(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    companion object {
        const val ECOSYSTEM_PREFIX = "com.hephaistos."
    }

    suspend fun scanEcosystemApps(manualPackages: Set<String>): List<EcosystemApp> =
        withContext(Dispatchers.IO) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos: List<ResolveInfo> = try {
                pm.queryIntentActivities(mainIntent, 0)
            } catch (e: Exception) {
                emptyList()
            }

            val appMap = mutableMapOf<String, EcosystemApp>()

            for (ri in resolveInfos) {
                val pkgName = ri.activityInfo.packageName
                if (pkgName == context.packageName) continue // Skip Hephaistos launcher itself

                val isEcosystem = pkgName.startsWith(ECOSYSTEM_PREFIX)
                val isManual = manualPackages.contains(pkgName)

                if (isEcosystem || isManual) {
                    val label = try {
                        ri.loadLabel(pm).toString()
                    } catch (e: Exception) {
                        pkgName
                    }
                    val icon = try {
                        ri.loadIcon(pm)
                    } catch (e: Exception) {
                        null
                    }
                    val versionName = try {
                        pm.getPackageInfo(pkgName, 0).versionName ?: "1.0"
                    } catch (e: Exception) {
                        "1.0"
                    }

                    appMap[pkgName] = EcosystemApp(
                        packageName = pkgName,
                        label = label,
                        versionName = versionName,
                        icon = icon,
                        isEcosystemPrefix = isEcosystem,
                        isManuallyIncluded = isManual,
                        launchIntentAvailable = true,
                        description = if (isEcosystem) "Module Hephaistos natif" else "Application intégrée manuellement"
                    )
                }
            }

            appMap.values.sortedBy { it.label }
        }

    suspend fun scanAllInstalledApps(manualPackages: Set<String>): List<EcosystemApp> =
        withContext(Dispatchers.IO) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos: List<ResolveInfo> = try {
                pm.queryIntentActivities(mainIntent, 0)
            } catch (e: Exception) {
                emptyList()
            }

            val result = mutableListOf<EcosystemApp>()
            val seen = mutableSetOf<String>()

            for (ri in resolveInfos) {
                val pkgName = ri.activityInfo.packageName
                if (pkgName == context.packageName || seen.contains(pkgName)) continue
                seen.add(pkgName)

                val label = try {
                    ri.loadLabel(pm).toString()
                } catch (e: Exception) {
                    pkgName
                }
                val icon = try {
                    ri.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }
                val versionName = try {
                    pm.getPackageInfo(pkgName, 0).versionName ?: "1.0"
                } catch (e: Exception) {
                    "1.0"
                }
                val isEcosystem = pkgName.startsWith(ECOSYSTEM_PREFIX)
                val isManual = manualPackages.contains(pkgName)

                result.add(
                    EcosystemApp(
                        packageName = pkgName,
                        label = label,
                        versionName = versionName,
                        icon = icon,
                        isEcosystemPrefix = isEcosystem,
                        isManuallyIncluded = isManual,
                        launchIntentAvailable = true,
                        description = pkgName
                    )
                )
            }

            result.sortedBy { it.label }
        }
}
