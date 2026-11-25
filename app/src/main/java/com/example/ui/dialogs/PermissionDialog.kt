package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BrandBlue

@Composable
fun PermissionDialog(
  onRequestPermissions: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("permission_dialog"),
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = BrandBlue,
          modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Wi-Fi Permission Required",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "To broadcast an over-the-air Wi-Fi Hotspot that other devices can discover and connect to, Android requires Nearby Wi-Fi Devices and Location permissions.",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          textAlign = TextAlign.Center,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onRequestPermissions,
          modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .testTag("grant_permission_button"),
          colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
          shape = RoundedCornerShape(4.dp)
        ) {
          Text("Grant Permission", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDismiss) {
          Text("Cancel", color = Color(0xFF64748B))
        }
      }
    }
  }
}
