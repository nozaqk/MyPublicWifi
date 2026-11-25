package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.NavDestination
import com.example.ui.components.SettingsSidebar
import com.example.ui.components.SidebarNavigation
import com.example.ui.dialogs.AboutDialog
import com.example.ui.dialogs.EmojiPickerModal
import com.example.ui.dialogs.QrCodeDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyPublicWiFiTheme
import com.example.viewmodel.HotspotViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: HotspotViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val darkTheme by viewModel.darkTheme.collectAsState()

      MyPublicWiFiTheme(darkTheme = darkTheme) {
        MainAppContent(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: HotspotViewModel) {
  val currentNavDestination by viewModel.currentNavDestination.collectAsState()
  val currentSettingsSection by viewModel.currentSettingsSection.collectAsState()
  val connectedDevices by viewModel.connectedDevices.collectAsState()
  val showQrDialog by viewModel.showQrDialog.collectAsState()
  val showEmojiPicker by viewModel.showEmojiPicker.collectAsState()
  val showAboutDialog by viewModel.showAboutDialog.collectAsState()
  val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
  val showHotspotHelpDialog by viewModel.showHotspotHelpDialog.collectAsState()
  val ssid by viewModel.ssid.collectAsState()
  val password by viewModel.password.collectAsState()
  val broadcastSsid by viewModel.broadcastSsid.collectAsState()
  val broadcastPassword by viewModel.broadcastPassword.collectAsState()

  val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    viewModel.dismissPermissionDialog()
    val allGranted = perms.values.all { it }
    if (allGranted) {
      viewModel.startHotspot()
    } else {
      viewModel.setShowHotspotHelpDialog(true)
    }
  }

  // Back handling: If on a sub-screen or in Settings, back returns to Hotspot screen
  BackHandler(enabled = currentNavDestination != NavDestination.HOTSPOT) {
    viewModel.setNavDestination(NavDestination.HOTSPOT)
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("main_scaffold"),
    contentWindowInsets = WindowInsets.safeDrawing
  ) { paddingValues ->
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Sidebar Navigation
      if (currentNavDestination == NavDestination.SETTINGS) {
        // Detailed Settings Sidebar matching Screenshots 3, 7, 8
        SettingsSidebar(
          currentSection = currentSettingsSection,
          onSelectSection = { section -> viewModel.setSettingsSection(section) },
          onBackToMain = { viewModel.setNavDestination(NavDestination.HOTSPOT) }
        )
      } else {
        // Main Iconic Blue Sidebar matching Screenshots 1, 2, 4
        SidebarNavigation(
          currentDestination = currentNavDestination,
          onNavigate = { destination -> viewModel.setNavDestination(destination) },
          onToggleMenu = { viewModel.setNavDestination(NavDestination.SETTINGS) },
          onToggleTheme = { viewModel.toggleDarkTheme() },
          onShowAbout = { viewModel.setShowAboutDialog(true) },
          connectedDevicesCount = connectedDevices.size
        )
      }

      // Main Content Screen
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        when (currentNavDestination) {
          NavDestination.HOTSPOT -> {
            HotspotHomeScreen(
              viewModel = viewModel,
              modifier = Modifier.fillMaxSize()
            )
          }

          NavDestination.DEVICES -> {
            ConnectedDevicesScreen(
              viewModel = viewModel,
              modifier = Modifier.fillMaxSize()
            )
          }

          NavDestination.SETTINGS -> {
            SettingsContentRouter(
              section = currentSettingsSection,
              viewModel = viewModel,
              modifier = Modifier.fillMaxSize()
            )
          }

          NavDestination.STATISTICS -> {
            StatisticsScreen(
              viewModel = viewModel,
              modifier = Modifier.fillMaxSize()
            )
          }

          NavDestination.URL_LOGS -> {
            UrlLogsScreen(
              viewModel = viewModel,
              modifier = Modifier.fillMaxSize()
            )
          }
        }
      }
    }

    // QR Code Dialog (Screenshot 6)
    if (showQrDialog) {
      QrCodeDialog(
        ssid = broadcastSsid ?: ssid,
        password = broadcastPassword ?: password,
        onDismiss = { viewModel.setShowQrDialog(false) }
      )
    }

    // Emoji Picker Modal (Screenshot 5)
    if (showEmojiPicker) {
      EmojiPickerModal(
        onEmojiSelected = { emoji -> viewModel.appendEmojiToSsid(emoji) },
        onDismiss = { viewModel.setShowEmojiPicker(false) }
      )
    }

    // About Dialog
    if (showAboutDialog) {
      AboutDialog(
        onDismiss = { viewModel.setShowAboutDialog(false) }
      )
    }

    // Wi-Fi Permission Dialog
    if (showPermissionDialog) {
      com.example.ui.dialogs.PermissionDialog(
        onRequestPermissions = {
          permissionLauncher.launch(viewModel.hardwareManager.getRequiredPermissions().toTypedArray())
        },
        onDismiss = { viewModel.dismissPermissionDialog() }
      )
    }

    // Connection Guide / Help Dialog
    if (showHotspotHelpDialog) {
      com.example.ui.dialogs.HotspotHelpDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.dismissHelpDialog() }
      )
    }
  }
}
