package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.WarningOrange
import com.example.viewmodel.HotspotViewModel

@Composable
fun HotspotHelpDialog(
  viewModel: HotspotViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val ssid = viewModel.broadcastSsid.value ?: viewModel.ssid.value
  val password = viewModel.broadcastPassword.value ?: viewModel.password.value
  val isHardwareBroadcasting = viewModel.isHardwareBroadcasting.value
  val errorMessage = viewModel.hardwareErrorMessage.value

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .wrapContentHeight()
        .testTag("hotspot_help_dialog"),
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Connect Other Devices",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage != null) {
          Surface(
            color = WarningOrange.copy(alpha = 0.12f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = WarningOrange,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFC2410C),
                lineHeight = 16.sp
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
          text = "Why is the network not showing on other devices?",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "On Android phones, there are two ways to broadcast Wi-Fi over the air so other devices can discover and connect to it:",
          fontSize = 12.sp,
          color = Color(0xFF475569),
          lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Step 1
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Method 1: Android System Mobile Hotspot (Recommended)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "1. Tap the button below to open your phone's native Hotspot settings.\n2. Turn Hotspot ON.\n3. Set Network Name to \"$ssid\" and Password to \"$password\".\n4. Your other phone or laptop will immediately see \"$ssid\" and connect!",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 2
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Method 2: Scan QR Code with Camera",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BrandBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap the \"QR\" button on the home screen. On your other phone, open the Camera app and point it at the QR code to join automatically without typing any password.",
              fontSize = 11.sp,
              color = Color(0xFF334155),
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            viewModel.hardwareManager.openSystemHotspotSettings(context)
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("open_hotspot_settings_dialog_button"),
          colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
          shape = RoundedCornerShape(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SettingsCell,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Open Android Hotspot Settings",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
        ) {
          Text("Close", fontSize = 13.sp)
        }
      }
    }
  }
}
