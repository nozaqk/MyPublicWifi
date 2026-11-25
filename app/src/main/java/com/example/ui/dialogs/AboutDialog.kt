package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BrandBlue

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("about_dialog"),
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
          imageVector = Icons.Default.WifiTethering,
          contentDescription = null,
          tint = BrandBlue,
          modifier = Modifier.size(54.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "MyPublicWiFi",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1E293B)
        )

        Text(
          text = "Version 32.2 (Android Mobile Edition)",
          fontSize = 12.sp,
          color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "MyPublicWiFi turns your smartphone into a versatile Wi-Fi Access Point, Repeater, and multifunctional network controller with Bandwidth Management, Security Filters, URL Logging, and Captive Portal.",
          fontSize = 13.sp,
          color = Color(0xFF334155),
          textAlign = TextAlign.Center,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypublicwifi.com"))
            context.startActivity(intent)
          },
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
        ) {
          Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Visit www.mypublicwifi.com", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(42.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
          shape = RoundedCornerShape(4.dp)
        ) {
          Text("OK", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
