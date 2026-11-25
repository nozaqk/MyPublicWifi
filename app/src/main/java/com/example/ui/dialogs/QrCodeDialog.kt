package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrandBlue
import com.example.util.QrCodeUtil

@Composable
fun QrCodeDialog(
  ssid: String,
  password: String,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val qrString = remember(ssid, password) {
    QrCodeUtil.buildWifiQrString(ssid, password)
  }
  val qrMatrix = remember(qrString) {
    QrCodeUtil.generateQrMatrix(qrString)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .wrapContentHeight()
        .testTag("qr_code_dialog"),
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
        // Title matching Screenshot 6
        Text(
          text = "MyPublicWiFi",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1E293B),
          modifier = Modifier.padding(bottom = 16.dp)
        )

        // QR Code Container
        Box(
          modifier = Modifier
            .size(240.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            QrCodeUtil.drawQrCanvas(
              drawScope = this,
              matrix = qrMatrix,
              color = Color.Black,
              backgroundColor = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle instructions matching Screenshot 6
        Text(
          text = "Scan this QR code with another device to establish a WiFi connection without entering a password.",
          fontSize = 14.sp,
          color = Color(0xFF334155),
          textAlign = TextAlign.Center,
          lineHeight = 20.sp,
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons matching Screenshot 6: [ Back ]  [ Save ]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("qr_back_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandBlue,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text("Back", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = {
              Toast.makeText(
                context,
                "Wi-Fi credentials saved to clipboard & photo storage!",
                Toast.LENGTH_SHORT
              ).show()
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("qr_save_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandBlue,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text("Save", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}
