package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SettingsSection
import com.example.ui.theme.BrandSidebarBlue
import com.example.ui.theme.BrandSidebarSelected

@Composable
fun SettingsSidebar(
  currentSection: SettingsSection,
  onSelectSection: (SettingsSection) -> Unit,
  onBackToMain: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .width(140.dp)
      .fillMaxHeight()
      .background(BrandSidebarBlue)
      .verticalScroll(rememberScrollState())
      .testTag("settings_sidebar")
  ) {
    // "Menu" item at top (matching Screenshots 3, 7, 8)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .clickable { onBackToMain() }
        .padding(horizontal = 10.dp)
        .testTag("settings_nav_menu"),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Menu,
        contentDescription = "Menu",
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Menu",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(2.dp))

    // List of settings sections matching screenshots
    SettingsSidebarItem(
      label = "General",
      icon = Icons.Default.Settings,
      isSelected = currentSection == SettingsSection.GENERAL,
      onClick = { onSelectSection(SettingsSection.GENERAL) },
      testTag = "settings_tab_general"
    )

    SettingsSidebarItem(
      label = "Security",
      icon = Icons.Default.Security,
      isSelected = currentSection == SettingsSection.SECURITY,
      onClick = { onSelectSection(SettingsSection.SECURITY) },
      testTag = "settings_tab_security"
    )

    SettingsSidebarItem(
      label = "DHCP Server",
      icon = Icons.Default.Hub,
      isSelected = currentSection == SettingsSection.DHCP_SERVER,
      onClick = { onSelectSection(SettingsSection.DHCP_SERVER) },
      testTag = "settings_tab_dhcp"
    )

    SettingsSidebarItem(
      label = "Bandwidth Manager",
      icon = Icons.Default.Speed,
      isSelected = currentSection == SettingsSection.BANDWIDTH_MANAGER,
      onClick = { onSelectSection(SettingsSection.BANDWIDTH_MANAGER) },
      testTag = "settings_tab_bandwidth"
    )

    SettingsSidebarItem(
      label = "Port Forwarding",
      icon = Icons.AutoMirrored.Filled.CompareArrows,
      isSelected = currentSection == SettingsSection.PORT_FORWARDING,
      onClick = { onSelectSection(SettingsSection.PORT_FORWARDING) },
      testTag = "settings_tab_port_forwarding"
    )

    SettingsSidebarItem(
      label = "Device Filter",
      icon = Icons.Default.Tune,
      isSelected = currentSection == SettingsSection.DEVICE_FILTER,
      onClick = { onSelectSection(SettingsSection.DEVICE_FILTER) },
      testTag = "settings_tab_device_filter"
    )

    SettingsSidebarItem(
      label = "Firewall",
      icon = Icons.Default.Shield,
      isSelected = currentSection == SettingsSection.FIREWALL,
      onClick = { onSelectSection(SettingsSection.FIREWALL) },
      testTag = "settings_tab_firewall"
    )

    SettingsSidebarItem(
      label = "Authentication / Design",
      icon = Icons.Default.Badge,
      isSelected = currentSection == SettingsSection.AUTH_DESIGN,
      onClick = { onSelectSection(SettingsSection.AUTH_DESIGN) },
      testTag = "settings_tab_auth_design"
    )

    SettingsSidebarItem(
      label = "File Sharing",
      icon = Icons.Default.Share,
      isSelected = currentSection == SettingsSection.FILE_SHARING,
      onClick = { onSelectSection(SettingsSection.FILE_SHARING) },
      testTag = "settings_tab_file_sharing"
    )
  }
}

@Composable
fun SettingsSidebarItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .background(if (isSelected) BrandSidebarSelected else Color.Transparent)
      .clickable { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.CenterStart
  ) {
    // White vertical active line on left (matching Screenshot 7: | Security)
    if (isSelected) {
      Box(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .width(3.5.dp)
          .height(30.dp)
          .background(Color.White, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        maxLines = 2,
        lineHeight = 13.sp
      )
    }
  }
}
