package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavDestination
import com.example.ui.theme.BrandBlue
import com.example.viewmodel.HotspotViewModel

@Composable
fun SecuritySettingsScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val draftSettings by viewModel.draftSecuritySettings.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("security_settings_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Title matching Screenshot 7
      Text(
        text = "Security",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Divider(
        color = Color(0xFFE2E8F0),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
      )

      // Security Checkboxes matching Screenshot 7 exactly
      SecurityCheckboxItem(
        label = "Enable Ad Blocker",
        checked = draftSettings.enableAdBlocker,
        onCheckedChange = {
          viewModel.draftSecuritySettings.value = draftSettings.copy(enableAdBlocker = it)
        }
      )

      SecurityCheckboxItem(
        label = "Block all social networks (e.g., Facebook)",
        checked = draftSettings.blockSocialNetworks,
        onCheckedChange = {
          viewModel.draftSecuritySettings.value = draftSettings.copy(blockSocialNetworks = it)
        }
      )

      SecurityCheckboxItem(
        label = "Block access to local host",
        checked = draftSettings.blockAccessToLocalHost,
        onCheckedChange = {
          viewModel.draftSecuritySettings.value = draftSettings.copy(blockAccessToLocalHost = it)
        }
      )

      SecurityCheckboxItem(
        label = "Block access to local network",
        checked = draftSettings.blockAccessToLocalNetwork,
        onCheckedChange = {
          viewModel.draftSecuritySettings.value = draftSettings.copy(blockAccessToLocalNetwork = it)
        }
      )

      SecurityCheckboxItem(
        label = "Block internet usage",
        checked = draftSettings.blockInternetUsage,
        onCheckedChange = {
          viewModel.draftSecuritySettings.value = draftSettings.copy(blockInternetUsage = it)
        }
      )

      SecurityCheckboxItem(
        label = "Block UPnP protocol",
        checked = draftSettings.blockUPnP,
        onCheckedChange = {
          viewModel.draftSecuritySettings.value = draftSettings.copy(blockUPnP = it)
        }
      )

      // Limit maximum number of clients row (matching Screenshot 7)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = draftSettings.limitMaxClients,
          onCheckedChange = {
            viewModel.draftSecuritySettings.value = draftSettings.copy(limitMaxClients = it)
          },
          colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
        )
        Text(
          text = "Limit maximum number of clients:",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.weight(1f)
        )

        Box(
          modifier = Modifier
            .width(70.dp)
            .height(34.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          BasicTextField(
            value = draftSettings.maxClientsCount.toString(),
            onValueChange = { str ->
              val count = str.toIntOrNull() ?: 0
              viewModel.draftSecuritySettings.value = draftSettings.copy(maxClientsCount = count)
            },
            textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(BrandBlue),
            singleLine = true
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Enable URL logging row + "View URL logs" button (matching Screenshot 7)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = draftSettings.enableUrlLogging,
          onCheckedChange = {
            viewModel.draftSecuritySettings.value = draftSettings.copy(enableUrlLogging = it)
          },
          colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
        )
        Text(
          text = "Enable URL logging",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.weight(1f)
        )

        Button(
          onClick = { viewModel.setNavDestination(NavDestination.URL_LOGS) },
          colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
          shape = RoundedCornerShape(4.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Text("View URL logs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Bottom Action Buttons matching Screenshot 7: [ Save changes ]  [ Discard changes ]
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Button(
        onClick = { viewModel.saveSecuritySettings() },
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("security_save_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = BrandBlue,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text("Save changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { viewModel.discardSecuritySettings() },
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("security_discard_button"),
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
fun SecurityCheckboxItem(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 3.dp),
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
