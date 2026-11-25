package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlue
import com.example.viewmodel.HotspotViewModel

@Composable
fun GeneralSettingsScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val draftSettings by viewModel.draftGeneralSettings.collectAsState()
  var showTechMenu by remember { mutableStateOf(false) }

  val techOptions = listOf(
    "Wi-Fi Direct",
    "Standard SoftAP (2.4 GHz)",
    "Standard SoftAP (5 GHz Dual-Band)",
    "Bluetooth Tethering",
    "USB Tethering"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("general_settings_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Title matching Screenshot 8
      Text(
        text = "General Settings",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Divider(
        color = Color(0xFFE2E8F0),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
      )

      // Checkboxes matching Screenshot 8
      GeneralCheckboxItem(
        label = "Start MyPublicWiFi at system startup",
        checked = draftSettings.startAtStartup,
        onCheckedChange = {
          viewModel.draftGeneralSettings.value = draftSettings.copy(startAtStartup = it)
        }
      )

      GeneralCheckboxItem(
        label = "Start MyPublicWiFi minimized",
        checked = draftSettings.startMinimized,
        onCheckedChange = {
          viewModel.draftGeneralSettings.value = draftSettings.copy(startMinimized = it)
        }
      )

      GeneralCheckboxItem(
        label = "Automatically create and start Hotspot when opening the App",
        checked = draftSettings.autoStartHotspotOnOpen,
        onCheckedChange = {
          viewModel.draftGeneralSettings.value = draftSettings.copy(autoStartHotspotOnOpen = it)
        }
      )

      GeneralCheckboxItem(
        label = "Disable Power Management for the WiFi Adapter",
        checked = draftSettings.disablePowerManagement,
        onCheckedChange = {
          viewModel.draftGeneralSettings.value = draftSettings.copy(disablePowerManagement = it)
        }
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Preferred WiFi technology row matching Screenshot 8
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Preferred WiFi technology:",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.width(170.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
              .background(MaterialTheme.colorScheme.surface)
              .clickable { showTechMenu = true }
              .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = draftSettings.preferredWifiTech,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
          }

          DropdownMenu(
            expanded = showTechMenu,
            onDismissRequest = { showTechMenu = false }
          ) {
            techOptions.forEach { tech ->
              DropdownMenuItem(
                text = { Text(tech) },
                onClick = {
                  viewModel.draftGeneralSettings.value = draftSettings.copy(preferredWifiTech = tech)
                  showTechMenu = false
                }
              )
            }
          }
        }
      }
    }

    // Bottom Action Buttons matching Screenshot 8: [ Save changes ]  [ Discard changes ]
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Button(
        onClick = { viewModel.saveGeneralSettings() },
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("general_save_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = BrandBlue,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text("Save changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { viewModel.discardGeneralSettings() },
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("general_discard_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = BrandBlue,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text("Discard changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun GeneralCheckboxItem(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = BrandBlue,
        checkmarkColor = Color.White
      )
    )
    Text(
      text = label,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}
