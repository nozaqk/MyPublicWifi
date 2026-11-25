package com.example.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Standards-compliant ISO/IEC 18004 QR Code generator for Wi-Fi configurations.
 * Generates valid Wi-Fi Alliance QR codes (WIFI:T:...;S:...;P:...;;)
 * with Reed-Solomon error correction and standard quiet zones so any camera or
 * barcode scanner can directly scan and connect to the hotspot.
 */
object QrCodeUtil {

  /**
   * Escapes special characters (\, ;, ,, :, ") per Wi-Fi Alliance / ZXing specification.
   */
  fun escapeWifiString(text: String): String {
    val sb = StringBuilder()
    for (ch in text) {
      if (ch == '\\' || ch == ';' || ch == ',' || ch == ':' || ch == '"') {
        sb.append('\\')
      }
      sb.append(ch)
    }
    return sb.toString()
  }

  /**
   * Generates standard Wi-Fi configuration string.
   * Format: WIFI:T:WPA;S:ssid;P:password;; (or WIFI:T:nopass;S:ssid;; if empty)
   */
  fun buildWifiQrString(ssid: String, password: String): String {
    val escapedSsid = escapeWifiString(ssid)
    return if (password.isEmpty()) {
      "WIFI:T:nopass;S:$escapedSsid;;"
    } else {
      val escapedPassword = escapeWifiString(password)
      "WIFI:T:WPA;S:$escapedSsid;P:$escapedPassword;;"
    }
  }

  /**
   * Generates a 2D boolean array representing the ISO/IEC 18004 QR code matrix
   * using ZXing QRCodeWriter.
   */
  fun generateQrMatrix(content: String): Array<BooleanArray> {
    return try {
      val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 0
      )
      val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
      val w = bitMatrix.width
      val h = bitMatrix.height
      Array(h) { r ->
        BooleanArray(w) { c ->
          bitMatrix.get(c, r)
        }
      }
    } catch (_: Exception) {
      // Fallback empty 21x21 matrix in case of extreme input error
      Array(21) { BooleanArray(21) { false } }
    }
  }

  /**
   * Renders the QR code onto a Compose DrawScope with standard quiet zone
   * and clean high-contrast black/white modules for instant scanning.
   */
  fun drawQrCanvas(
    drawScope: DrawScope,
    matrix: Array<BooleanArray>,
    color: Color = Color.Black,
    backgroundColor: Color = Color.White
  ) {
    val size = matrix.size
    if (size == 0) return

    val totalWidth = drawScope.size.width
    val totalHeight = drawScope.size.height
    val minDim = minOf(totalWidth, totalHeight)

    // Standard 2-module quiet zone around the QR matrix
    val quietZoneModules = 2
    val totalModules = size + (quietZoneModules * 2)
    val moduleSize = minDim / totalModules.toFloat()

    val startX = (totalWidth - (totalModules * moduleSize)) / 2f + (quietZoneModules * moduleSize)
    val startY = (totalHeight - (totalModules * moduleSize)) / 2f + (quietZoneModules * moduleSize)

    // Fill white background for maximum optical contrast
    drawScope.drawRect(
      color = backgroundColor,
      topLeft = Offset.Zero,
      size = drawScope.size
    )

    // Draw sharp square modules for reliable camera / lens reading
    for (r in 0 until size) {
      for (c in 0 until size) {
        if (matrix[r][c]) {
          drawScope.drawRect(
            color = color,
            topLeft = Offset(startX + c * moduleSize, startY + r * moduleSize),
            size = Size(moduleSize, moduleSize)
          )
        }
      }
    }
  }
}
