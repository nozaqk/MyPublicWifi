package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CheckmarkGreen
import com.example.viewmodel.HotspotViewModel

@Composable
fun StatisticsScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val trafficHistory by viewModel.trafficHistory.collectAsState()
  val connectedDevices by viewModel.connectedDevices.collectAsState()
  val isHotspotActive by viewModel.isHotspotActive.collectAsState()
  val uptimeSeconds by viewModel.uptimeSeconds.collectAsState()

  val currentDownload = trafficHistory.lastOrNull()?.downloadMbit ?: 0f
  val currentUpload = trafficHistory.lastOrNull()?.uploadMbit ?: 0f
  val peakDownload = trafficHistory.maxOfOrNull { it.downloadMbit } ?: 0f
  val peakUpload = trafficHistory.maxOfOrNull { it.uploadMbit } ?: 0f

  val totalTransferredMb = connectedDevices.sumOf { (it.totalDownloadedMb + it.totalUploadedMb).toDouble() }.toFloat()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("statistics_screen")
  ) {
    Text(
      text = "Traffic & Bandwidth Analytics",
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )

    HorizontalDivider(
      color = Color(0xFFE2E8F0),
      thickness = 1.dp,
      modifier = Modifier.padding(vertical = 10.dp)
    )

    // Current Speeds Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = CheckmarkGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Download", fontSize = 12.sp, color = Color(0xFF64748B))
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = String.format("%.2f Mbit/s", currentDownload),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(text = "Peak: ${String.format("%.1f", peakDownload)} Mbit/s", fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Upload", fontSize = 12.sp, color = Color(0xFF64748B))
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = String.format("%.2f Mbit/s", currentUpload),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(text = "Peak: ${String.format("%.1f", peakUpload)} Mbit/s", fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Real-time Throughput Graph
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(6.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Real-Time Throughput (Mbit/s)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(CheckmarkGreen, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Down", fontSize = 10.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).background(BrandBlue, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Up", fontSize = 10.sp, color = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val maxSpeed = 6.0f // Scale to max 6 Mbit/s

            // Draw grid lines
            for (i in 1..3) {
              val y = h * (i / 4f)
              drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
              )
            }

            if (trafficHistory.size >= 2) {
              val stepX = w / (trafficHistory.size - 1)

              val downloadPath = Path()
              val uploadPath = Path()

              trafficHistory.forEachIndexed { index, point ->
                val x = index * stepX
                val downY = h - (point.downloadMbit.coerceIn(0f, maxSpeed) / maxSpeed) * h
                val upY = h - (point.uploadMbit.coerceIn(0f, maxSpeed) / maxSpeed) * h

                if (index == 0) {
                  downloadPath.moveTo(x, downY)
                  uploadPath.moveTo(x, upY)
                } else {
                  downloadPath.lineTo(x, downY)
                  uploadPath.lineTo(x, upY)
                }
              }

              // Draw upload line
              drawPath(
                path = uploadPath,
                color = BrandBlue,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
              )

              // Draw download line
              drawPath(
                path = downloadPath,
                color = CheckmarkGreen,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Session Data Consumption
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(6.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Session Overview", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Active Hotspot State:", fontSize = 12.sp, color = Color(0xFF64748B))
          Text(if (isHotspotActive) "Running" else "Stopped", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isHotspotActive) CheckmarkGreen else Color(0xFFE53935))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Session Duration:", fontSize = 12.sp, color = Color(0xFF64748B))
          Text("${uptimeSeconds / 60}m ${uptimeSeconds % 60}s", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Total Data Transferred:", fontSize = 12.sp, color = Color(0xFF64748B))
          Text(String.format("%.1f MB", totalTransferredMb), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
        }
      }
    }
  }
}
