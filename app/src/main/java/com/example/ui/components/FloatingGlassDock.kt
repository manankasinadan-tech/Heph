package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.NavigationTab
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassDark
import com.example.ui.theme.GlassLight

import androidx.compose.ui.graphics.luminance

@Composable
fun FloatingGlassDock(
    currentTab: NavigationTab,
    isScanning: Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dockBg = if (isDark) GlassDark else GlassLight
    val dockBorder = if (isDark) GlassBorderDark else GlassBorderLight

    val rotationAngle by animateFloatAsState(
        targetValue = if (isScanning) 360f else 0f,
        animationSpec = spring(),
        label = "refresh_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = dockBg,
            shadowElevation = 16.dp,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .border(1.dp, dockBorder, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home / Launcher Tab
                DockItem(
                    icon = Icons.Default.Home,
                    label = "Accueil",
                    isSelected = currentTab == NavigationTab.LAUNCHER,
                    testTag = "dock_home_button",
                    onClick = { onTabSelected(NavigationTab.LAUNCHER) }
                )

                // Refresh Button (Action)
                DockItem(
                    icon = Icons.Default.Refresh,
                    label = stringResource(R.string.dock_refresh),
                    isSelected = false,
                    iconRotation = rotationAngle,
                    testTag = "dock_refresh_button",
                    onClick = onRefresh
                )

                // Console / Terminal Tab
                DockItem(
                    icon = Icons.Default.Terminal,
                    label = stringResource(R.string.dock_console),
                    isSelected = currentTab == NavigationTab.CONSOLE,
                    testTag = "dock_console_button",
                    onClick = { onTabSelected(NavigationTab.CONSOLE) }
                )

                // Settings Tab
                DockItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.dock_settings),
                    isSelected = currentTab == NavigationTab.SETTINGS,
                    testTag = "dock_settings_button",
                    onClick = { onTabSelected(NavigationTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    iconRotation: Float = 0f,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = label }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(iconRotation)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )

        AnimatedVisibility(visible = isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        }
    }
}
