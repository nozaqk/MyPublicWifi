package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BandwidthSettings
import com.example.ui.theme.BrandBlue
import com.example.viewmodel.HotspotViewModel

@Composable
fun BandwidthManagerScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val bandwidthTab by viewModel.bandwidthTab.collectAsState()
  val draftSettings by viewModel.draftBandwidthSettings.collectAsState()
  val connectedDevices by viewModel.connectedDevices.collectAsState()
  val speedTestState by viewModel.speedTestState.collectAsState()

  var showModeDropdown by remember { mutableStateOf(false) }
  var showDownloadUnitDropdown by remember { mutableStateOf(false) }
  var showUploadUnitDropdown by remember { mutableStateOf(false) }

  val unitOptions = listOf("Kbit/s", "Mbit/s", "Gbit/s")
  val modeOptions = listOf(
    "Each client is assigned a statically defined bandwidth",
    "Dynamic bandwidth allocation based on active clients",
    "Fair-share queueing"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("bandwidth_manager_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Screen Title matching Screenshot 3
      Text(
        text = "Bandwidth Manager",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      HorizontalDivider(
        color = Color(0xFFE2E8F0),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 10.dp)
      )

      // "Enable bandwidth manager" checkbox
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            viewModel.draftBandwidthSettings.value = draftSettings.copy(enabled = !draftSettings.enabled)
          }
          .padding(vertical = 4.dp)
          .testTag("enable_bandwidth_checkbox")
      ) {
        Checkbox(
          checked = draftSettings.enabled,
          onCheckedChange = { isChecked ->
            viewModel.draftBandwidthSettings.value = draftSettings.copy(enabled = isChecked)
          },
          colors = CheckboxDefaults.colors(
            checkedColor = BrandBlue,
            checkmarkColor = Color.White
          )
        )
        Text(
          text = "Enable bandwidth manager",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Subtabs: Total Bandwidth | Default Bandwidth | Individual Bandwidth | Test Bandwidth
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val tabs = listOf("Total\nBandwidth", "Default\nBandwidth", "Individual\nBandwidth", "Test\nBandwidth")
        val tabKeys = listOf("Total Bandwidth", "Default Bandwidth", "Individual Bandwidth", "Test Bandwidth")

        tabs.forEachIndexed { index, label ->
          val isSelected = bandwidthTab == tabKeys[index]
          Box(
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
              .background(if (isSelected) BrandBlue else Color.Transparent)
              .clickable { viewModel.setBandwidthTab(tabKeys[index]) }
              .testTag("bandwidth_tab_${index}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else BrandBlue,
              lineHeight = 13.sp
            )
          }
        }
      }

      HorizontalDivider(color = BrandBlue, thickness = 2.dp)

      Spacer(modifier = Modifier.height(14.dp))

      // Tab Content
      when (bandwidthTab) {
        "Default Bandwidth" -> {
          // Matching Screenshot 3 exactly
          Text(
            text = "Default bandwidth:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
          )

          // Mode Dropdown
          Box(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { showModeDropdown = true }
                .padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = draftSettings.mode,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
              expanded = showModeDropdown,
              onDismissRequest = { showModeDropdown = false }
            ) {
              modeOptions.forEach { opt ->
                DropdownMenuItem(
                  text = { Text(opt, fontSize = 13.sp) },
                  onClick = {
                    viewModel.draftBandwidthSettings.value = draftSettings.copy(mode = opt)
                    showModeDropdown = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Static default bandwidth group box (matching Screenshot 3)
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Static default bandwidth",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Download Speed row
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Checkbox(
                  checked = draftSettings.downloadLimitEnabled,
                  onCheckedChange = { isChecked ->
                    viewModel.draftBandwidthSettings.value =
                      draftSettings.copy(downloadLimitEnabled = isChecked)
                  },
                  colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                )
                Text(
                  text = "Download speed:",
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f)
                )

                // Numeric input
                Box(
                  modifier = Modifier
                    .width(60.dp)
                    .height(36.dp)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp),
                  contentAlignment = Alignment.CenterStart
                ) {
                  BasicTextField(
                    value = draftSettings.downloadSpeed.toInt().toString(),
                    onValueChange = { str ->
                      val num = str.toFloatOrNull() ?: 0f
                      viewModel.draftBandwidthSettings.value = draftSettings.copy(downloadSpeed = num)
                    },
                    textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(BrandBlue),
                    singleLine = true
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Unit dropdown
                Box {
                  Row(
                    modifier = Modifier
                      .width(80.dp)
                      .height(36.dp)
                      .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                      .clickable { showDownloadUnitDropdown = true }
                      .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = draftSettings.downloadUnit,
                      fontSize = 12.sp,
                      modifier = Modifier.weight(1f)
                    )
                    Icon(
                      imageVector = Icons.Default.ArrowDropDown,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  DropdownMenu(
                    expanded = showDownloadUnitDropdown,
                    onDismissRequest = { showDownloadUnitDropdown = false }
                  ) {
                    unitOptions.forEach { u ->
                      DropdownMenuItem(
                        text = { Text(u) },
                        onClick = {
                          viewModel.draftBandwidthSettings.value = draftSettings.copy(downloadUnit = u)
                          showDownloadUnitDropdown = false
                        }
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Upload Speed row
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Checkbox(
                  checked = draftSettings.uploadLimitEnabled,
                  onCheckedChange = { isChecked ->
                    viewModel.draftBandwidthSettings.value =
                      draftSettings.copy(uploadLimitEnabled = isChecked)
                  },
                  colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
                )
                Text(
                  text = "Upload speed:",
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f)
                )

                // Numeric input
                Box(
                  modifier = Modifier
                    .width(60.dp)
                    .height(36.dp)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp),
                  contentAlignment = Alignment.CenterStart
                ) {
                  BasicTextField(
                    value = draftSettings.uploadSpeed.toInt().toString(),
                    onValueChange = { str ->
                      val num = str.toFloatOrNull() ?: 0f
                      viewModel.draftBandwidthSettings.value = draftSettings.copy(uploadSpeed = num)
                    },
                    textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(BrandBlue),
                    singleLine = true
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Unit dropdown
                Box {
                  Row(
                    modifier = Modifier
                      .width(80.dp)
                      .height(36.dp)
                      .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                      .clickable { showUploadUnitDropdown = true }
                      .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = draftSettings.uploadUnit,
                      fontSize = 12.sp,
                      modifier = Modifier.weight(1f)
                    )
                    Icon(
                      imageVector = Icons.Default.ArrowDropDown,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  DropdownMenu(
                    expanded = showUploadUnitDropdown,
                    onDismissRequest = { showUploadUnitDropdown = false }
                  ) {
                    unitOptions.forEach { u ->
                      DropdownMenuItem(
                        text = { Text(u) },
                        onClick = {
                          viewModel.draftBandwidthSettings.value = draftSettings.copy(uploadUnit = u)
                          showUploadUnitDropdown = false
                        }
                      )
                    }
                  }
                }
              }
            }
          }
        }

        "Test Bandwidth" -> {
          // Interactive speed test
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Network Bandwidth & Speed Test",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                SpeedMetricBox(label = "Ping", value = "${speedTestState.pingMs} ms")
                SpeedMetricBox(label = "Jitter", value = "${speedTestState.jitterMs} ms")
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                SpeedMetricBox(
                  label = "Download",
                  value = String.format("%.1f Mbit/s", speedTestState.downloadSpeedMbit),
                  highlight = true
                )
                SpeedMetricBox(
                  label = "Upload",
                  value = String.format("%.1f Mbit/s", speedTestState.uploadSpeedMbit),
                  highlight = true
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              LinearProgressIndicator(
                progress = { speedTestState.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = BrandBlue
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = speedTestState.phase,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = { viewModel.runSpeedTest() },
                enabled = !speedTestState.isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(4.dp)
              ) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Speed Test")
              }
            }
          }
        }

        "Total Bandwidth" -> {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Total Network Bandwidth Pool",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Set aggregate throughput limits for the entire hotspot to avoid congesting your cellular connection.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(text = "Total Max Download: 100 Mbit/s", fontSize = 13.sp)
              Text(text = "Total Max Upload: 30 Mbit/s", fontSize = 13.sp)
            }
          }
        }

        "Individual Bandwidth" -> {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Client-Specific Bandwidth Throttling",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))

              if (connectedDevices.isEmpty()) {
                Text(
                  text = "No clients currently connected to allocate custom bandwidth.",
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              } else {
                connectedDevices.forEach { dev ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(text = dev.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                      Text(text = dev.ip, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Text(text = "Limit: 3 Mbit/s", fontSize = 12.sp, color = BrandBlue)
                  }
                  HorizontalDivider(color = Color(0xFFE2E8F0))
                }
              }
            }
          }
        }
      }
    }

    // Bottom Action Buttons matching Screenshot 3: [ Discard changes ]  [ Save changes ]
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Button(
        onClick = { viewModel.discardBandwidthSettings() },
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("bandwidth_discard_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = BrandBlue,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text("Discard changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = { viewModel.saveBandwidthSettings() },
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("bandwidth_save_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = BrandBlue,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text("Save changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun SpeedMetricBox(label: String, value: String, highlight: Boolean = false) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(130.dp)
      .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
      .padding(8.dp)
  ) {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
    Text(
      text = value,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = if (highlight) BrandBlue else Color(0xFF1E293B)
    )
  }
}
