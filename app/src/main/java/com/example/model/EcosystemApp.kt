package com.example.model

import android.graphics.drawable.Drawable

/**
 * Represents an application within the Hephaistos modular ecosystem.
 */
data class EcosystemApp(
    val packageName: String,
    val label: String,
    val versionName: String,
    val icon: Drawable? = null,
    val isEcosystemPrefix: Boolean = true, // Starts with com.hephaistos.
    val isManuallyIncluded: Boolean = false,
    val launchIntentAvailable: Boolean = true,
    val description: String = ""
)
