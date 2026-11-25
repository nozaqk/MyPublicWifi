package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HotspotMode
import com.example.model.NavDestination
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.CheckmarkGreen
import com.example.viewmodel.HotspotViewModel

@Composable
fun HotspotHomeScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val hotspotMode by viewModel.hotspotMode.collectAsState()
  val networkAccess by viewModel.networkAccess.collectAsState()
  val internetConnection by viewModel.internetConnection.collectAsState()
  val ssid by viewModel.ssid.collectAsState()
  val password by viewModel.password.collectAsState()
  val broadcastSsid by viewModel.broadcastSsid.collectAsState()
  val broadcastPassword by viewModel.broadcastPassword.collectAsState()
  // While broadcasting, show the credentials Android actually uses; otherwise the typed values
  val shownSsid = broadcastSsid ?: ssid
  val shownPassword = broadcastPassword ?: password
  val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
  val isHotspotActive by viewModel.isHotspotActive.collectAsState()
  val connectedDevices by viewModel.connectedDevices.collectAsState()
  val activeIpDetails by viewModel.activeIpDetails.collectAsState()
  val uptimeSeconds by viewModel.uptimeSeconds.collectAsState()

  var showNetworkAccessMenu by remember { mutableStateOf(false) }
  var showInternetConnectionMenu by remember { mutableStateOf(false) }

  val networkAccessOptions = listOf(
    "Router Mode (NAT)",
    "Bridge Mode",
    "WiFi Direct Group",
    "Internet Sharing (ICS)"
  )

  val availableConnections by viewModel.availableInternetConnections.collectAsState()
  val localIp = viewModel.localIpAddress
  val internetConnectionOptions = remember(availableConnections, localIp) {
    if (availableConnections.isNotEmpty()) {
      availableConnections.map { it.displayName }
    } else {
      listOf(
        "Automatic ($localIp)",
        "WLAN ($localIp)",
        "Mobile Data (LTE/5G)"
      )
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("hotspot_home_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // App Header with antenna icon
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.WifiTethering,
          contentDescription = null,
          tint = BrandBlue,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "MyPublicWiFi",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isHotspotActive) {
          val minutes = uptimeSeconds / 60
          val seconds = uptimeSeconds % 60
          Surface(
            color = CheckmarkGreen.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(CheckmarkGreen, RoundedCornerShape(3.dp))
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 11.sp,
                color = CheckmarkGreen,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Top Mode Buttons: [WLAN Hotspot] [WLAN Repeater] [Multifunctional Hotspot]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        HotspotModeButton(
          title = "WLAN\nHotspot",
          isSelected = hotspotMode == HotspotMode.WLAN_HOTSPOT,
          onClick = { viewModel.setHotspotMode(HotspotMode.WLAN_HOTSPOT) },
          modifier = Modifier.weight(1f),
          testTag = "mode_wlan_hotspot"
        )
        HotspotModeButton(
          title = "WLAN\nRepeater",
          isSelected = hotspotMode == HotspotMode.WLAN_REPEATER,
          onClick = { viewModel.setHotspotMode(HotspotMode.WLAN_REPEATER) },
          modifier = Modifier.weight(1f),
          testTag = "mode_wlan_repeater"
        )
        HotspotModeButton(
          title = "Multifunctional\nHotspot",
          isSelected = hotspotMode == HotspotMode.MULTIFUNCTIONAL,
          onClick = { viewModel.setHotspotMode(HotspotMode.MULTIFUNCTIONAL) },
          modifier = Modifier.weight(1f),
          testTag = "mode_multifunctional"
        )
      }

      // In WLAN Repeater mode, only Internet Connection is shown (matching Screenshot 2)
      if (hotspotMode != HotspotMode.WLAN_REPEATER) {
        // Network Access Label
        FieldLabel(text = "Network Access")
        Box(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.surface)
              .clickable { showNetworkAccessMenu = true }
              .padding(horizontal = 10.dp)
              .testTag("network_access_dropdown"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = networkAccess,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Valid",
              tint = CheckmarkGreen,
              modifier = Modifier.size(18.dp)
            )
          }

          DropdownMenu(
            expanded = showNetworkAccessMenu,
            onDismissRequest = { showNetworkAccessMenu = false }
          ) {
            networkAccessOptions.forEach { opt ->
              DropdownMenuItem(
                text = { Text(opt) },
                onClick = {
                  viewModel.setNetworkAccess(opt)
                  showNetworkAccessMenu = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }

      // Internet Connection Label
      FieldLabel(text = "Internet Connection")
      Box(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { showInternetConnectionMenu = true }
            .padding(horizontal = 10.dp)
            .testTag("internet_connection_dropdown"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (hotspotMode == HotspotMode.WLAN_REPEATER) Icons.Default.SignalCellularAlt else Icons.Default.Language,
            contentDescription = null,
            tint = CheckmarkGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = internetConnection,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Valid",
            tint = CheckmarkGreen,
            modifier = Modifier.size(18.dp)
          )
        }

        DropdownMenu(
          expanded = showInternetConnectionMenu,
          onDismissRequest = { showInternetConnectionMenu = false }
        ) {
          internetConnectionOptions.forEach { opt ->
            DropdownMenuItem(
              text = { Text(opt) },
              onClick = {
                viewModel.setInternetConnection(opt)
                showInternetConnectionMenu = false
              }
            )
          }
        }
      }

      // If not repeater mode, show SSID and Key fields
      if (hotspotMode != HotspotMode.WLAN_REPEATER) {
        Spacer(modifier = Modifier.height(10.dp))

        // Network Name (SSID)
        FieldLabel(text = "Network Name (SSID)")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          BasicTextField(
            value = ssid,
            onValueChange = { viewModel.setSsid(it) },
            textStyle = TextStyle(
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(BrandBlue),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("ssid_input")
          )

          // Emoji Button matching screenshot 1 and 5
          IconButton(
            onClick = { viewModel.setShowEmojiPicker(true) },
            modifier = Modifier
              .size(28.dp)
              .testTag("emoji_picker_button")
          ) {
            Icon(
              imageVector = Icons.Default.SentimentSatisfiedAlt,
              contentDescription = "Pick Emoji",
              tint = Color(0xFF475569)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Valid",
            tint = CheckmarkGreen,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Network Key
        FieldLabel(text = "Network Key (at least 8 characters)")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          BasicTextField(
            value = password,
            onValueChange = { viewModel.setPassword(it) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = TextStyle(
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(BrandBlue),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("password_input")
          )

          IconButton(
            onClick = { viewModel.togglePasswordVisibility() },
            modifier = Modifier
              .size(28.dp)
              .testTag("password_visibility_toggle")
          ) {
            Icon(
              imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = "Toggle Visibility",
              tint = Color(0xFF475569)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Valid",
            tint = if (password.length >= 8) CheckmarkGreen else Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Primary Action Buttons
      if (!isHotspotActive) {
        // "Start Hotspot" button (matching Screenshot 1)
        Button(
          onClick = { viewModel.startHotspot() },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("start_hotspot_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = "Start Hotspot",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
        }
      } else {
        // "Stop Hotspot" + "QR" button (matching Screenshot 2)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { viewModel.stopHotspot() },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("stop_hotspot_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandBlue,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "Stop Hotspot",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = { viewModel.setShowQrDialog(true) },
            modifier = Modifier
              .width(90.dp)
              .height(48.dp)
              .testTag("show_qr_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandBlue,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = "QR Code",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("QR", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // If Hotspot is active, show the live broadcast banner with connection info
      if (isHotspotActive) {
        Surface(
          color = Color(0xFFF0FDF4),
          border = androidx.compose.foundation.BorderStroke(1.dp, CheckmarkGreen.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WifiTethering,
                contentDescription = null,
                tint = CheckmarkGreen,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Broadcasting Wi-Fi to other devices",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF166534)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "1. On other device, search Wi-Fi for: $shownSsid\n2. Enter password: $shownPassword (or scan QR code)",
              fontSize = 11.sp,
              color = Color(0xFF1E293B),
              lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.clickable {
                try {
                  val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("http://$localIp:${viewModel.localWebServerPort}/")
                  )
                  intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                  context.startActivity(intent)
                } catch (_: Exception) {}
              },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Web Portal & Shared Files: http://$localIp:${viewModel.localWebServerPort}/",
                fontSize = 11.sp,
                color = BrandBlue,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
      }

      // Android System Hotspot Settings & Connection Help Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = { viewModel.openSystemHotspotSettings(context) },
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("open_system_tethering_button"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BrandBlue
          ),
          shape = RoundedCornerShape(4.dp),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SettingsCell,
            contentDescription = null,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Android Hotspot Settings", fontSize = 11.sp, maxLines = 1)
        }

        OutlinedButton(
          onClick = { viewModel.setShowHotspotHelpDialog(true) },
          modifier = Modifier
            .height(38.dp)
            .testTag("not_showing_help_button"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFC2410C)
          ),
          shape = RoundedCornerShape(4.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Not showing?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Bottom Status Bar (matching Screenshot 1 and Screenshot 2)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
    ) {
      HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
      Spacer(modifier = Modifier.height(8.dp))

      // SSID / IP line
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Wifi,
          contentDescription = null,
          tint = Color(0xFF1E293B),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isHotspotActive) "$shownSsid ($localIp)" else ssid,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Security / Password line
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = Color(0xFF1E293B),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isPasswordVisible) shownPassword else "****************",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.weight(1f)
        )
        IconButton(
          onClick = { viewModel.togglePasswordVisibility() },
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Connected devices line
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.setNavDestination(NavDestination.DEVICES) }
          .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Smartphone,
          contentDescription = null,
          tint = Color(0xFF1E293B),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Connected devices:   ${connectedDevices.size}",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Website link
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.openOfficialWebsite(context) }
          .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = Color(0xFF1E293B),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "https://www.mypublicwifi.com",
          fontSize = 12.sp,
          color = BrandBlue
        )
      }
    }
  }
}

@Composable
fun HotspotModeButton(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = ""
) {
  Box(
    modifier = modifier
      .height(46.dp)
      .clip(RoundedCornerShape(4.dp))
      .border(
        width = 1.5.dp,
        color = BrandBlue,
        shape = RoundedCornerShape(4.dp)
      )
      .background(if (isSelected) BrandBlue else Color.White)
      .clickable { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (isSelected) Color.White else BrandBlue,
      lineHeight = 13.sp
    )
  }
}

@Composable
fun FieldLabel(text: String) {
  Text(
    text = text,
    fontSize = 13.sp,
    fontWeight = FontWeight.Normal,
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.padding(bottom = 4.dp)
  )
}
