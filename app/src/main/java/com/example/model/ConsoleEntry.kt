package com.example.model

enum class EntryStatus {
    RUNNING, SUCCESS, ERROR, INFO
}

data class ConsoleEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: EntryStatus = EntryStatus.INFO,
    val outputJson: String? = null,
    val plainOutput: String? = null,
    val targetModule: String? = null
)

enum class NavigationTab {
    LAUNCHER,
    CONSOLE,
    SETTINGS
}
