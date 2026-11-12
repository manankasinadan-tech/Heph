package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EcosystemDispatcher
import com.example.data.PackageManagerScanner
import com.example.data.PreferencesManager
import com.example.model.ConsoleEntry
import com.example.model.EcosystemApp
import com.example.model.EntryStatus
import com.example.model.NavigationTab
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

data class HephaistosUiState(
    val currentTab: NavigationTab = NavigationTab.LAUNCHER,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isGridLayout: Boolean = true,
    val ecosystemApps: List<EcosystemApp> = emptyList(),
    val allInstalledApps: List<EcosystemApp> = emptyList(),
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val consoleEntries: List<ConsoleEntry> = emptyList(),
    val commandInput: String = "",
    val manifestContent: String = "",
    val specContent: String = "",
    val showManualPicker: Boolean = false,
    val showUnifiedUIDemo: Boolean = false,
    val activeNotificationMessage: String? = null
)

class HephaistosViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val scanner = PackageManagerScanner(application)
    val dispatcher = EcosystemDispatcher(application)

    private val _uiState = MutableStateFlow(
        HephaistosUiState(
            themeMode = prefs.getThemeMode(),
            isGridLayout = prefs.isGridLayout()
        )
    )
    val uiState: StateFlow<HephaistosUiState> = _uiState.asStateFlow()

    init {
        loadAssetDocs()
        refreshApps()
        initWelcomeConsole()

        viewModelScope.launch {
            dispatcher.incomingResults.collect { entry ->
                _uiState.update { state ->
                    state.copy(consoleEntries = state.consoleEntries + entry)
                }
            }
        }
    }

    private fun loadAssetDocs() {
        viewModelScope.launch {
            val manifest = try {
                getApplication<Application>().assets.open("manifest.json").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                "{}"
            }
            val spec = try {
                getApplication<Application>().assets.open("ECOSYSTEM_SPEC.md").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                "# Spécifications non trouvées"
            }
            _uiState.update { it.copy(manifestContent = manifest, specContent = spec) }
        }
    }

    private fun initWelcomeConsole() {
        val welcome = ConsoleEntry(
            command = "init",
            status = EntryStatus.INFO,
            plainOutput = """
                ╔═══════════════════════════════════════════════════════╗
                ║          HEPHAISTOS GSI ORCHESTRATION TERMINAL        ║
                ║            Hôte Modulaire de Portage Système          ║
                ╚═══════════════════════════════════════════════════════╝
                • Tapez 'help' pour afficher la liste des commandes.
                • Tapez 'list' pour afficher les modules connectés.
                • Tapez 'spec' ou 'doc' pour consulter les normes d'API.
            """.trimIndent()
        )
        _uiState.update { it.copy(consoleEntries = listOf(welcome)) }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setGridLayout(isGrid: Boolean) {
        prefs.setGridLayout(isGrid)
        _uiState.update { it.copy(isGridLayout = isGrid) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCommandInput(cmd: String) {
        _uiState.update { it.copy(commandInput = cmd) }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            val manual = prefs.getManualPackages()
            val ecosystem = scanner.scanEcosystemApps(manual)
            val allApps = scanner.scanAllInstalledApps(manual)
            _uiState.update {
                it.copy(
                    ecosystemApps = ecosystem,
                    allInstalledApps = allApps,
                    isScanning = false
                )
            }
        }
    }

    fun toggleManualPackage(packageName: String) {
        viewModelScope.launch {
            prefs.toggleManualPackage(packageName)
            refreshApps()
        }
    }

    fun setManualPickerVisible(show: Boolean) {
        if (show) {
            refreshApps()
        }
        _uiState.update { it.copy(showManualPicker = show) }
    }

    fun setUnifiedUIDemoVisible(show: Boolean) {
        _uiState.update { it.copy(showUnifiedUIDemo = show) }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Impossible de lancer $packageName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearConsole() {
        _uiState.update { it.copy(consoleEntries = emptyList()) }
    }

    fun executeCommand(rawCommand: String) {
        val trimmed = rawCommand.trim()
        if (trimmed.isEmpty()) return

        _uiState.update { it.copy(commandInput = "") }
        val parts = trimmed.split("\\s+".toRegex())
        val verb = parts[0].lowercase()

        when (verb) {
            "help" -> {
                val entry = ConsoleEntry(
                    command = trimmed,
                    status = EntryStatus.INFO,
                    plainOutput = """
                        Commandes Hephaistos disponibles :
                          help                      - Affiche ce menu d'aide
                          list                      - Liste les modules de l'écosystème installés
                          ping <module|all>         - Teste la disponibilité d'un module
                          scan                      - Rafraîchit la détection des paquets
                          spec                      - Affiche les spécifications d'écosystème
                          doc [manifest]            - Affiche le gabarit manifest.json
                          theme <system|dark|light> - Modifie le thème de l'application
                          run <pkg> <cmd> [json]    - Envoie une commande directe à un module
                          clear                     - Efface l'historique de la console
                          about                     - Informations sur l'hôte Hephaistos
                    """.trimIndent()
                )
                appendConsoleEntry(entry)
            }
            "list" -> {
                val apps = _uiState.value.ecosystemApps
                val text = if (apps.isEmpty()) {
                    "Aucun module com.hephaistos.* détecté pour l'instant.\nUtilisez 'scan' ou ajoutez des paquets dans Paramètres."
                } else {
                    buildString {
                        appendLine("Modules d'environnement détectés (${apps.size}) :")
                        apps.forEachIndexed { i, app ->
                            appendLine("  [${i + 1}] ${app.label} (${app.packageName}) v${app.versionName}")
                        }
                    }
                }
                appendConsoleEntry(ConsoleEntry(command = trimmed, status = EntryStatus.SUCCESS, plainOutput = text))
            }
            "scan" -> {
                refreshApps()
                appendConsoleEntry(
                    ConsoleEntry(
                        command = trimmed,
                        status = EntryStatus.INFO,
                        plainOutput = "Balayage des applications de l'écosystème en cours..."
                    )
                )
            }
            "ping" -> {
                val target = if (parts.size > 1) parts[1] else "all"
                if (target == "all") {
                    val apps = _uiState.value.ecosystemApps
                    if (apps.isEmpty()) {
                        appendConsoleEntry(
                            ConsoleEntry(
                                command = trimmed,
                                status = EntryStatus.INFO,
                                plainOutput = "Aucun module externe connecté à pinger."
                            )
                        )
                    } else {
                        apps.forEach { app ->
                            val sent = dispatcher.dispatchModuleCommand(app.packageName, "ping", "{}")
                            appendConsoleEntry(sent)
                        }
                    }
                } else {
                    val targetPkg = if (target.startsWith("com.hephaistos.")) target else "com.hephaistos.$target"
                    val sent = dispatcher.dispatchModuleCommand(targetPkg, "ping", "{}")
                    appendConsoleEntry(sent)
                }
            }
            "run" -> {
                if (parts.size < 3) {
                    appendConsoleEntry(
                        ConsoleEntry(
                            command = trimmed,
                            status = EntryStatus.ERROR,
                            plainOutput = "Usage: run <package_name> <command> [json_payload]\nExemple: run com.hephaistos.unpacker unpack {\"path\":\"/sdcard/system.img\"}"
                        )
                    )
                } else {
                    val pkg = parts[1]
                    val cmd = parts[2]
                    val json = if (parts.size > 3) parts.drop(3).joinToString(" ") else "{}"
                    val sent = dispatcher.dispatchModuleCommand(pkg, cmd, json)
                    appendConsoleEntry(sent)
                }
            }
            "spec" -> {
                appendConsoleEntry(
                    ConsoleEntry(
                        command = trimmed,
                        status = EntryStatus.SUCCESS,
                        plainOutput = _uiState.value.specContent.take(1500) + "\n\n... [Voir Paramètres pour le texte intégral]"
                    )
                )
            }
            "doc" -> {
                appendConsoleEntry(
                    ConsoleEntry(
                        command = trimmed,
                        status = EntryStatus.SUCCESS,
                        outputJson = _uiState.value.manifestContent
                    )
                )
            }
            "theme" -> {
                val mode = if (parts.size > 1) parts[1].lowercase() else "system"
                when (mode) {
                    "dark" -> setThemeMode(ThemeMode.DARK)
                    "light" -> setThemeMode(ThemeMode.LIGHT)
                    else -> setThemeMode(ThemeMode.SYSTEM)
                }
                appendConsoleEntry(
                    ConsoleEntry(
                        command = trimmed,
                        status = EntryStatus.SUCCESS,
                        plainOutput = "Thème configuré sur: $mode"
                    )
                )
            }
            "clear" -> {
                clearConsole()
            }
            "about" -> {
                val aboutJson = JSONObject().apply {
                    put("application", "Hephaistos")
                    put("role", "Launcher & Orchestration Host")
                    put("package", "com.hephaistos.launcher")
                    put("version", "1.0.0")
                    put("target", "Android GSI & System Partition Tooling")
                    put("ipcProtocol", "com.hephaistos.action.EXECUTE / RESULT")
                }.toString(2)
                appendConsoleEntry(
                    ConsoleEntry(
                        command = trimmed,
                        status = EntryStatus.INFO,
                        outputJson = aboutJson
                    )
                )
            }
            else -> {
                appendConsoleEntry(
                    ConsoleEntry(
                        command = trimmed,
                        status = EntryStatus.ERROR,
                        plainOutput = "Commande inconnue : '$verb'. Tapez 'help' pour voir la liste des commandes disponibles."
                    )
                )
            }
        }
    }

    private fun appendConsoleEntry(entry: ConsoleEntry) {
        _uiState.update { it.copy(consoleEntries = it.consoleEntries + entry) }
    }

    fun shareDoc(context: Context, filename: String, content: String) {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_TITLE, filename)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Exporter $filename")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur d'export: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        dispatcher.cleanup()
    }
}
