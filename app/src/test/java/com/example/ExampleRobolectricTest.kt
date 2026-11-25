package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.HotspotMode
import com.example.util.QrCodeUtil
import com.example.viewmodel.HotspotViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches MyPublicWiFi`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyPublicWiFi", appName)
  }

  @Test
  fun `qr code string generator formats wifi credentials correctly`() {
    val qr = QrCodeUtil.buildWifiQrString("MyPublicWiFi", "12345678")
    assertEquals("WIFI:T:WPA;S:MyPublicWiFi;P:12345678;;", qr)

    val matrix = QrCodeUtil.generateQrMatrix(qr)
    assertTrue(matrix.isNotEmpty())
    assertTrue(matrix.size >= 21)
    // Finder pattern corners should be true
    assertTrue(matrix[0][0])
  }

  @Test
  fun `hotspot view model toggle hotspot state and devices`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val shadowApp = org.robolectric.Shadows.shadowOf(app)
    shadowApp.grantPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      shadowApp.grantPermissions(android.Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    val viewModel = HotspotViewModel(app)

    assertEquals(HotspotMode.WLAN_HOTSPOT, viewModel.hotspotMode.value)
    assertFalse(viewModel.isHotspotActive.value)
    assertEquals(0, viewModel.connectedDevices.value.size)

    // Start Hotspot
    viewModel.startHotspot()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertTrue(viewModel.isHotspotActive.value)

    // Stop Hotspot
    viewModel.stopHotspot()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertFalse(viewModel.isHotspotActive.value)
    assertEquals(0, viewModel.connectedDevices.value.size)
  }

  @Test
  fun `user configured SSID and password are not overwritten on startHotspot`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val shadowApp = org.robolectric.Shadows.shadowOf(app)
    shadowApp.grantPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      shadowApp.grantPermissions(android.Manifest.permission.NEARBY_WIFI_DEVICES)
    }

    val viewModel = HotspotViewModel(app)
    viewModel.setSsid("CustomOfficeWiFi")
    viewModel.setPassword("SecretPass99")

    viewModel.startHotspot()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // Assert user's SSID and Password NEVER changed to random values
    assertEquals("CustomOfficeWiFi", viewModel.ssid.value)
    assertEquals("SecretPass99", viewModel.password.value)
    assertTrue(viewModel.isHotspotActive.value)
  }

  @Test
  fun `hotspot view model prompts permission when not granted`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val shadowApp = org.robolectric.Shadows.shadowOf(app)
    shadowApp.denyPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)

    val viewModel = HotspotViewModel(app)
    viewModel.startHotspot()
    assertTrue(viewModel.showPermissionDialog.value)
    assertFalse(viewModel.isHotspotActive.value)
  }

  @Test
  fun `emoji append updates ssid`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = HotspotViewModel(app)
    viewModel.setSsid("MyHotspot")
    viewModel.appendEmojiToSsid("☺")
    assertEquals("MyHotspot☺", viewModel.ssid.value)
  }

  @Test
  fun `real network manager retrieves available connections`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val netManager = com.example.util.RealNetworkManager(app)
    val connections = netManager.getAvailableInternetConnections()
    assertTrue(connections.isNotEmpty())
    assertTrue(connections.any { it.id.startsWith("auto") })
  }

  @Test
  fun `traffic monitor samples valid hardware snapshot`() {
    val monitor = com.example.util.RealTrafficMonitor()
    monitor.startSession()
    val snap = monitor.sample()
    assertNotNull(snap)
    assertTrue(snap.downloadSpeedMbit >= 0f)
    assertTrue(snap.uploadSpeedMbit >= 0f)
  }

  @Test
  fun `local hotspot web server starts and handles url logging`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val logged = mutableListOf<com.example.model.UrlLogEntry>()
    val server = com.example.util.LocalHotspotWebServer(app) { log ->
      logged.add(log)
    }
    server.start(18080)
    assertTrue(server.isRunning)
    server.stop()
    assertFalse(server.isRunning)
  }
}
