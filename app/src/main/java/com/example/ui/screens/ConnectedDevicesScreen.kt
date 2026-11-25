package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectedDevice
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CheckmarkGreen
import com.example.ui.theme.DangerRed
import com.example.viewmodel.HotspotViewModel

@Composable
fun ConnectedDevicesScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val connectedDevices by viewModel.connectedDevices.collectAsState()
  val isHotspotActive by viewModel.isHotspotActive.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
      .testTag("connected_devices_screen")
  ) {
    // Title & Add device action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Connected Devices",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "${connectedDevices.size} active clients",
          fontSize = 12.sp,
          color = Color(0xFF64748B)
        )
      }

      OutlinedButton(
        onClick = { viewModel.refreshConnectedDevices() },
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
        modifier = Modifier.height(36.dp)
      ) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Scan Network", fontSize = 12.sp)
      }
    }

    HorizontalDivider(
      color = Color(0xFFE2E8F0),
      thickness = 1.dp,
      modifier = Modifier.padding(vertical = 10.dp)
    )

    if (connectedDevices.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.DevicesOther,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (isHotspotActive) "No devices currently connected" else "Hotspot is inactive",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isHotspotActive) "Real devices connecting to this hotspot will automatically appear here." else "Start the hotspot to allow other devices to connect.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(
          items = connectedDevices,
          key = { index, device -> "${device.id}_${device.mac}_$index" }
        ) { _, device ->
          DeviceCard(
            device = device,
            onToggleBlock = { viewModel.toggleBlockDevice(device.id) },
            onRemove = { viewModel.removeDevice(device.id) }
          )
        }
      }
    }
  }
}

@Composable
fun DeviceCard(
  device: ConnectedDevice,
  onToggleBlock: () -> Unit,
  onRemove: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(6.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (device.isBlocked) DangerRed.copy(alpha = 0.5f) else Color(0xFFCBD5E1)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(
              if (device.isBlocked) DangerRed.copy(alpha = 0.1f) else BrandBlue.copy(alpha = 0.1f),
              RoundedCornerShape(6.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (device.name.contains("Laptop", true)) Icons.Default.Laptop else Icons.Default.Smartphone,
            contentDescription = null,
            tint = if (device.isBlocked) DangerRed else BrandBlue,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = device.name,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (device.isBlocked) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "BLOCKED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DangerRed
              )
            }
          }
          Text(
            text = "IP: ${device.ip}  •  MAC: ${device.mac}",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )
        }

        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Disconnect",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Speed & Data stats
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = CheckmarkGreen,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = String.format("%.1f Mbit/s", device.downloadSpeedMbit),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = String.format("%.1f Mbit/s", device.uploadSpeedMbit),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Text(
          text = "Total: ${String.format("%.1f", device.totalDownloadedMb)} MB",
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Actions row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onToggleBlock,
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (device.isBlocked) CheckmarkGreen else DangerRed
          ),
          shape = RoundedCornerShape(4.dp),
          modifier = Modifier.height(30.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
          Text(
            text = if (device.isBlocked) "Unblock Client" else "Block Client",
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
