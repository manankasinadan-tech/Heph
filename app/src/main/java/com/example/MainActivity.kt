package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.NavigationTab
import com.example.ui.HephaistosViewModel
import com.example.ui.components.FloatingGlassDock
import com.example.ui.console.ConsoleScreen
import com.example.ui.launcher.LauncherScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.HephaistosTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HephaistosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            HephaistosTheme(themeMode = uiState.themeMode) {
                // BackHandler to return to Launcher Home if on Console or Settings
                BackHandler(enabled = uiState.currentTab != NavigationTab.LAUNCHER) {
                    viewModel.selectTab(NavigationTab.LAUNCHER)
                }

                Scaffold(
                    contentWindowInsets = WindowInsets(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Main View Router
                        Crossfade(
                            targetState = uiState.currentTab,
                            label = "tab_crossfade",
                            modifier = Modifier.fillMaxSize()
                        ) { tab ->
                            when (tab) {
                                NavigationTab.LAUNCHER -> {
                                    LauncherScreen(
                                        uiState = uiState,
                                        onLaunchApp = { pkg -> viewModel.launchApp(context, pkg) },
                                        onOpenConsoleForModule = { pkg ->
                                            viewModel.setCommandInput("run $pkg status")
                                            viewModel.selectTab(NavigationTab.CONSOLE)
                                        },
                                        onToggleLayout = { isGrid -> viewModel.setGridLayout(isGrid) },
                                        onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
                                        onOpenManualPicker = { viewModel.setManualPickerVisible(true) },
                                        onNavigateTab = { targetTab -> viewModel.selectTab(targetTab) }
                                    )
                                }
                                NavigationTab.CONSOLE -> {
                                    ConsoleScreen(
                                        uiState = uiState,
                                        onExecuteCommand = { cmd -> viewModel.executeCommand(cmd) },
                                        onClearConsole = { viewModel.clearConsole() },
                                        onCommandInputChange = { cmd -> viewModel.setCommandInput(cmd) }
                                    )
                                }
                                NavigationTab.SETTINGS -> {
                                    SettingsScreen(
                                        uiState = uiState,
                                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                        onToggleManualPackage = { pkg -> viewModel.toggleManualPackage(pkg) },
                                        onOpenManualPicker = { show -> viewModel.setManualPickerVisible(show) },
                                        onOpenUIDemo = { show -> viewModel.setUnifiedUIDemoVisible(show) },
                                        onShareDoc = { filename, content -> viewModel.shareDoc(context, filename, content) }
                                    )
                                }
                            }
                        }

                        // macOS-Style Floating Material 3 Dock
                        FloatingGlassDock(
                            currentTab = uiState.currentTab,
                            isScanning = uiState.isScanning,
                            onTabSelected = { selectedTab -> viewModel.selectTab(selectedTab) },
                            onRefresh = { viewModel.refreshApps() },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val command = intent.getStringExtra("COMMAND")
        if (!command.isNullOrBlank()) {
            viewModel.selectTab(NavigationTab.CONSOLE)
            viewModel.executeCommand(command)
        }
    }
}
