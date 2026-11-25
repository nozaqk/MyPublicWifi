package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavDestination
import com.example.ui.theme.BrandSidebarBlue
import com.example.ui.theme.BrandSidebarSelected

@Composable
fun SidebarNavigation(
  currentDestination: NavDestination,
  onNavigate: (NavDestination) -> Unit,
  onToggleMenu: () -> Unit,
  onToggleTheme: () -> Unit,
  onShowAbout: () -> Unit,
  connectedDevicesCount: Int,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .width(56.dp)
      .fillMaxHeight()
      .background(BrandSidebarBlue)
      .testTag("sidebar_navigation"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Nav Items
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Menu / Hamburger icon
      SidebarIconButton(
        icon = Icons.Default.Menu,
        contentDescription = "Menu",
        isSelected = false,
        onClick = onToggleMenu,
        testTag = "nav_menu"
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Hotspot / Wi-Fi broadcast
      SidebarIconButton(
        icon = Icons.Default.Wifi,
        contentDescription = "WLAN Hotspot",
        isSelected = currentDestination == NavDestination.HOTSPOT,
        onClick = { onNavigate(NavDestination.HOTSPOT) },
        testTag = "nav_hotspot"
      )

      // Connected Devices / Smartphone
      SidebarIconButton(
        icon = Icons.Default.Smartphone,
        contentDescription = "Connected Devices",
        isSelected = currentDestination == NavDestination.DEVICES,
        onClick = { onNavigate(NavDestination.DEVICES) },
        badgeText = if (connectedDevicesCount > 0) connectedDevicesCount.toString() else null,
        testTag = "nav_devices"
      )

      // Settings / Gear
      SidebarIconButton(
        icon = Icons.Default.Settings,
        contentDescription = "Settings",
        isSelected = currentDestination == NavDestination.SETTINGS,
        onClick = { onNavigate(NavDestination.SETTINGS) },
        testTag = "nav_settings"
      )

      // Traffic Analytics / Chart
      SidebarIconButton(
        icon = Icons.AutoMirrored.Filled.ShowChart,
        contentDescription = "Traffic Analytics",
        isSelected = currentDestination == NavDestination.STATISTICS,
        onClick = { onNavigate(NavDestination.STATISTICS) },
        testTag = "nav_statistics"
      )

      // URL Logs / WWW
      SidebarIconButton(
        icon = Icons.Default.Public,
        contentDescription = "URL Logs",
        isSelected = currentDestination == NavDestination.URL_LOGS,
        onClick = { onNavigate(NavDestination.URL_LOGS) },
        testTag = "nav_url_logs"
      )

      // Sun / Theme toggle icon
      SidebarIconButton(
        icon = Icons.Default.WbSunny,
        contentDescription = "Toggle Light/Dark Theme",
        isSelected = false,
        onClick = onToggleTheme,
        testTag = "nav_theme_toggle"
      )
    }

    // Bottom Info item
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      SidebarIconButton(
        icon = Icons.Default.Info,
        contentDescription = "About MyPublicWiFi",
        isSelected = false,
        onClick = onShowAbout,
        testTag = "nav_about"
      )
    }
  }
}

@Composable
fun SidebarIconButton(
  icon: ImageVector,
  contentDescription: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String,
  badgeText: String? = null
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .background(if (isSelected) BrandSidebarSelected else Color.Transparent)
      .clickable { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    // White vertical indicator bar on the left when selected (matching Screenshot 1: | 📶 )
    if (isSelected) {
      Box(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .width(4.dp)
          .height(30.dp)
          .background(Color.White, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
      )
    }

    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = Color.White,
        modifier = Modifier.size(24.dp)
      )

      if (badgeText != null) {
        Box(
          modifier = Modifier
            .offset(x = 10.dp, y = (-10).dp)
            .size(16.dp)
            .background(Color(0xFFE53935), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = badgeText,
            color = Color.White,
            fontSize = 9.sp
          )
        }
      }
    }
  }
}
