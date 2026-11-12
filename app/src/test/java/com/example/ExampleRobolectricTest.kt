package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import com.example.model.NavigationTab
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hephaistos", appName)
  }

  @Test
  fun `preferences manager handles theme and manual packages`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferencesManager(context)

    prefs.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, prefs.getThemeMode())

    val testPkg = "com.hephaistos.custom_engine"
    val included = prefs.toggleManualPackage(testPkg)
    assertTrue(included)
    assertTrue(prefs.getManualPackages().contains(testPkg))
  }
}
