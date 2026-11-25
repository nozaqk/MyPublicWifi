package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.model.DeviceFilterRule
import com.example.model.PortForwardRule
import com.example.model.SettingsSection
import com.example.ui.theme.BrandBlue
import com.example.viewmodel.HotspotViewModel

@Composable
fun SettingsContentRouter(
  section: SettingsSection,
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  when (section) {
    SettingsSection.GENERAL -> GeneralSettingsScreen(viewModel, modifier)
    SettingsSection.SECURITY -> SecuritySettingsScreen(viewModel, modifier)
    SettingsSection.BANDWIDTH_MANAGER -> BandwidthManagerScreen(viewModel, modifier)
    SettingsSection.DHCP_SERVER -> DhcpSettingsScreen(viewModel, modifier)
    SettingsSection.PORT_FORWARDING -> PortForwardingScreen(viewModel, modifier)
    SettingsSection.DEVICE_FILTER -> DeviceFilterScreen(viewModel, modifier)
    SettingsSection.FIREWALL -> FirewallSettingsScreen(viewModel, modifier)
    SettingsSection.AUTH_DESIGN -> CaptivePortalSettingsScreen(viewModel, modifier)
    SettingsSection.FILE_SHARING -> FileSharingSettingsScreen(viewModel, modifier)
  }
}

@Composable
fun DhcpSettingsScreen(viewModel: HotspotViewModel, modifier: Modifier = Modifier) {
  val draft by viewModel.draftDhcpSettings.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("dhcp_settings_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text("DHCP Server", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

      Text("Configure local IP assignment pool for connected hotspot clients.", fontSize = 12.sp, color = Color(0xFF64748B))
      Spacer(modifier = Modifier.height(14.dp))

      SettingInputField("Starting IP Address", draft.startIp) {
        viewModel.draftDhcpSettings.value = draft.copy(startIp = it)
      }
      SettingInputField("Ending IP Address", draft.endIp) {
        viewModel.draftDhcpSettings.value = draft.copy(endIp = it)
      }
      SettingInputField("Subnet Mask", draft.subnetMask) {
        viewModel.draftDhcpSettings.value = draft.copy(subnetMask = it)
      }
      SettingInputField("Default Gateway", draft.gateway) {
        viewModel.draftDhcpSettings.value = draft.copy(gateway = it)
      }
      SettingInputField("Lease Time (hours)", draft.leaseTimeHours.toString()) {
        viewModel.draftDhcpSettings.value = draft.copy(leaseTimeHours = it.toIntOrNull() ?: 24)
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = { viewModel.discardDhcpSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Discard changes") }
      Button(
        onClick = { viewModel.saveDhcpSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Save changes") }
    }
  }
}

@Composable
fun PortForwardingScreen(viewModel: HotspotViewModel, modifier: Modifier = Modifier) {
  val rules by viewModel.portForwardRules.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  var serviceName by remember { mutableStateOf("") }
  var extPort by remember { mutableStateOf("") }
  var intIp by remember { mutableStateOf("192.168.137.45") }
  var intPort by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
      .testTag("port_forwarding_screen")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Port Forwarding", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(34.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Rule", fontSize = 12.sp)
      }
    }

    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    Text("Forward inbound traffic ports to specific client devices.", fontSize = 12.sp, color = Color(0xFF64748B))
    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      items(rules) { r ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(4.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(r.serviceName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text("Port ${r.externalPort} -> ${r.internalIp}:${r.internalPort} (${r.protocol})", fontSize = 11.sp, color = Color(0xFF64748B))
            }
            IconButton(onClick = { viewModel.removePortForwardRule(r.id) }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    if (showAddDialog) {
      AlertDialog(
        onDismissRequest = { showAddDialog = false },
        title = { Text("Add Port Forward Rule") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = serviceName, onValueChange = { serviceName = it }, label = { Text("Service Name") })
            OutlinedTextField(value = extPort, onValueChange = { extPort = it }, label = { Text("External Port") })
            OutlinedTextField(value = intIp, onValueChange = { intIp = it }, label = { Text("Internal Device IP") })
            OutlinedTextField(value = intPort, onValueChange = { intPort = it }, label = { Text("Internal Port") })
          }
        },
        confirmButton = {
          Button(onClick = {
            if (serviceName.isNotBlank() && extPort.toIntOrNull() != null) {
              viewModel.addPortForwardRule(
                PortForwardRule(
                  id = "pf_${System.currentTimeMillis()}",
                  serviceName = serviceName,
                  externalPort = extPort.toInt(),
                  internalIp = intIp,
                  internalPort = intPort.toIntOrNull() ?: extPort.toInt()
                )
              )
              showAddDialog = false
              serviceName = ""
              extPort = ""
            }
          }) { Text("Add") }
        },
        dismissButton = {
          TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
        }
      )
    }
  }
}

@Composable
fun DeviceFilterScreen(viewModel: HotspotViewModel, modifier: Modifier = Modifier) {
  val rules by viewModel.deviceFilterRules.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var devName by remember { mutableStateOf("") }
  var macAddress by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(14.dp)
      .testTag("device_filter_screen")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Device Filter", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(34.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Block MAC", fontSize = 12.sp)
      }
    }

    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

    Text("Filter clients by MAC hardware address (Whitelist / Blacklist mode).", fontSize = 12.sp, color = Color(0xFF64748B))
    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      items(rules) { r ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(4.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(r.deviceName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text("MAC: ${r.mac}  [${r.mode}]", fontSize = 11.sp, color = Color(0xFFE53935))
            }
            IconButton(onClick = { viewModel.removeDeviceFilterRule(r.id) }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    if (showAddDialog) {
      AlertDialog(
        onDismissRequest = { showAddDialog = false },
        title = { Text("Add Filter MAC Rule") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = devName, onValueChange = { devName = it }, label = { Text("Device Label") })
            OutlinedTextField(value = macAddress, onValueChange = { macAddress = it }, label = { Text("MAC Address (XX-XX-XX-XX-XX-XX)") })
          }
        },
        confirmButton = {
          Button(onClick = {
            if (macAddress.isNotBlank()) {
              viewModel.addDeviceFilterRule(
                DeviceFilterRule(
                  id = "df_${System.currentTimeMillis()}",
                  deviceName = devName.ifBlank { "Filtered Device" },
                  mac = macAddress,
                  mode = "Blocked"
                )
              )
              showAddDialog = false
              devName = ""
              macAddress = ""
            }
          }) { Text("Block") }
        },
        dismissButton = {
          TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
        }
      )
    }
  }
}

@Composable
fun FirewallSettingsScreen(viewModel: HotspotViewModel, modifier: Modifier = Modifier) {
  val draft by viewModel.draftFirewallSettings.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("firewall_settings_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text("Firewall", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

      Text("Hardware packet filter & port security controls.", fontSize = 12.sp, color = Color(0xFF64748B))
      Spacer(modifier = Modifier.height(14.dp))

      SettingInputField("Blocked Outbound Ports", draft.blockPorts) {
        viewModel.draftFirewallSettings.value = draft.copy(blockPorts = it)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftFirewallSettings.value = draft.copy(allowDnsOnly = !draft.allowDnsOnly)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.allowDnsOnly, onCheckedChange = {
          viewModel.draftFirewallSettings.value = draft.copy(allowDnsOnly = it)
        })
        Text("Allow DNS & HTTP/HTTPS traffic only (Strict Mode)", fontSize = 13.sp)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftFirewallSettings.value = draft.copy(preventPortScanning = !draft.preventPortScanning)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.preventPortScanning, onCheckedChange = {
          viewModel.draftFirewallSettings.value = draft.copy(preventPortScanning = it)
        })
        Text("Prevent internal port scanning between clients", fontSize = 13.sp)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftFirewallSettings.value = draft.copy(blockPingRequests = !draft.blockPingRequests)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.blockPingRequests, onCheckedChange = {
          viewModel.draftFirewallSettings.value = draft.copy(blockPingRequests = it)
        })
        Text("Block ICMP ping requests to the router host", fontSize = 13.sp)
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = { viewModel.discardFirewallSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Discard changes") }
      Button(
        onClick = { viewModel.saveFirewallSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Save changes") }
    }
  }
}

@Composable
fun CaptivePortalSettingsScreen(viewModel: HotspotViewModel, modifier: Modifier = Modifier) {
  val draft by viewModel.draftCaptivePortalSettings.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("captive_portal_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text("Authentication / Design", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftCaptivePortalSettings.value = draft.copy(enabled = !draft.enabled)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.enabled, onCheckedChange = {
          viewModel.draftCaptivePortalSettings.value = draft.copy(enabled = it)
        })
        Text("Enable Captive Portal Web Login", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(10.dp))

      SettingInputField("Captive Portal Title", draft.portalTitle) {
        viewModel.draftCaptivePortalSettings.value = draft.copy(portalTitle = it)
      }

      SettingInputField("Welcome Announcement Text", draft.welcomeMessage) {
        viewModel.draftCaptivePortalSettings.value = draft.copy(welcomeMessage = it)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftCaptivePortalSettings.value = draft.copy(requireTerms = !draft.requireTerms)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.requireTerms, onCheckedChange = {
          viewModel.draftCaptivePortalSettings.value = draft.copy(requireTerms = it)
        })
        Text("Require guests to accept Terms of Use before internet access", fontSize = 13.sp)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftCaptivePortalSettings.value = draft.copy(voucherAuthEnabled = !draft.voucherAuthEnabled)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.voucherAuthEnabled, onCheckedChange = {
          viewModel.draftCaptivePortalSettings.value = draft.copy(voucherAuthEnabled = it)
        })
        Text("Require pre-generated Prepaid / Voucher code", fontSize = 13.sp)
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = { viewModel.discardCaptivePortalSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Discard changes") }
      Button(
        onClick = { viewModel.saveCaptivePortalSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Save changes") }
    }
  }
}

@Composable
fun FileSharingSettingsScreen(viewModel: HotspotViewModel, modifier: Modifier = Modifier) {
  val draft by viewModel.draftFileSharingSettings.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(14.dp)
      .testTag("file_sharing_screen"),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text("File Sharing", fontSize = 17.sp, fontWeight = FontWeight.Bold)
      HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftFileSharingSettings.value = draft.copy(enabled = !draft.enabled)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.enabled, onCheckedChange = {
          viewModel.draftFileSharingSettings.value = draft.copy(enabled = it)
        })
        Text("Enable Local Hotspot Web File Server", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
      }

      Text("Allows connected clients to download shared files or upload pictures/documents over the local WiFi connection.", fontSize = 12.sp, color = Color(0xFF64748B))
      Spacer(modifier = Modifier.height(10.dp))

      SettingInputField("Shared Folder Name", draft.shareName) {
        viewModel.draftFileSharingSettings.value = draft.copy(shareName = it)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftFileSharingSettings.value = draft.copy(allowUploads = !draft.allowUploads)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.allowUploads, onCheckedChange = {
          viewModel.draftFileSharingSettings.value = draft.copy(allowUploads = it)
        })
        Text("Allow connected clients to upload files to host device", fontSize = 13.sp)
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable {
          viewModel.draftFileSharingSettings.value = draft.copy(requirePassword = !draft.requirePassword)
        }.padding(vertical = 4.dp)
      ) {
        Checkbox(checked = draft.requirePassword, onCheckedChange = {
          viewModel.draftFileSharingSettings.value = draft.copy(requirePassword = it)
        })
        Text("Protect File Server with Password", fontSize = 13.sp)
      }

      if (draft.requirePassword) {
        SettingInputField("Access Password", draft.password) {
          viewModel.draftFileSharingSettings.value = draft.copy(password = it)
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = { viewModel.discardFileSharingSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Discard changes") }
      Button(
        onClick = { viewModel.saveFileSharingSettings() },
        modifier = Modifier.weight(1f).height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
      ) { Text("Save changes") }
    }
  }
}

@Composable
fun SettingInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
    Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
        .background(MaterialTheme.colorScheme.surface)
        .padding(horizontal = 10.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(BrandBlue),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
