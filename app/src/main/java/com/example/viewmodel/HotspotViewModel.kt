package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.util.ConnectedDeviceScanner
import com.example.util.HotspotHardwareManager
import com.example.util.HotspotStartResult
import com.example.util.LocalHotspotWebServer
import com.example.util.NetworkConnectionOption
import com.example.util.RealNetworkManager
import com.example.util.RealSpeedTester
import com.example.util.RealTrafficMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HotspotViewModel(application: Application) : AndroidViewModel(application) {

  // Navigation & Theme
  private val _currentNavDestination = MutableStateFlow(NavDestination.HOTSPOT)
  val currentNavDestination: StateFlow<NavDestination> = _currentNavDestination.asStateFlow()

  private val _currentSettingsSection = MutableStateFlow(SettingsSection.GENERAL)
  val currentSettingsSection: StateFlow<SettingsSection> = _currentSettingsSection.asStateFlow()

  private val _darkTheme = MutableStateFlow(false)
  val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

  // Real Network Manager & Connections
  val realNetworkManager = RealNetworkManager(application)
  val localIpAddress: String = ConnectedDeviceScanner.getLocalIpAddress()

  private val _availableInternetConnections = MutableStateFlow<List<NetworkConnectionOption>>(emptyList())
  val availableInternetConnections: StateFlow<List<NetworkConnectionOption>> = _availableInternetConnections.asStateFlow()

  // Hotspot Main State
  private val _hotspotMode = MutableStateFlow(HotspotMode.WLAN_HOTSPOT)
  val hotspotMode: StateFlow<HotspotMode> = _hotspotMode.asStateFlow()

  private val _networkAccess = MutableStateFlow("Router Mode (NAT)")
  val networkAccess: StateFlow<String> = _networkAccess.asStateFlow()

  private val _internetConnection = MutableStateFlow("Automatic ($localIpAddress)")
  val internetConnection: StateFlow<String> = _internetConnection.asStateFlow()

  private val _ssid = MutableStateFlow("MyPublicWiFi")
  val ssid: StateFlow<String> = _ssid.asStateFlow()

  private val _password = MutableStateFlow("12345678")
  val password: StateFlow<String> = _password.asStateFlow()

  private val _isPasswordVisible = MutableStateFlow(false)
  val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

  private val _isHotspotActive = MutableStateFlow(false)
  val isHotspotActive: StateFlow<Boolean> = _isHotspotActive.asStateFlow()

  private val _uptimeSeconds = MutableStateFlow(0L)
  val uptimeSeconds: StateFlow<Long> = _uptimeSeconds.asStateFlow()

  private val _activeIpDetails = MutableStateFlow("MyPublicWiFi ($localIpAddress)")
  val activeIpDetails: StateFlow<String> = _activeIpDetails.asStateFlow()

  // Hardware Manager for real over-the-air Wi-Fi broadcast
  val hardwareManager = HotspotHardwareManager(application)

  private val _isHardwareBroadcasting = MutableStateFlow(false)
  val isHardwareBroadcasting: StateFlow<Boolean> = _isHardwareBroadcasting.asStateFlow()

  private val _hardwareStatusMessage = MutableStateFlow<String?>(null)
  val hardwareStatusMessage: StateFlow<String?> = _hardwareStatusMessage.asStateFlow()

  private val _showPermissionDialog = MutableStateFlow(false)
  val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

  private val _showHotspotHelpDialog = MutableStateFlow(false)
  val showHotspotHelpDialog: StateFlow<Boolean> = _showHotspotHelpDialog.asStateFlow()

  private val _hardwareErrorMessage = MutableStateFlow<String?>(null)
  val hardwareErrorMessage: StateFlow<String?> = _hardwareErrorMessage.asStateFlow()

  // Dialogs
  private val _showQrDialog = MutableStateFlow(false)
  val showQrDialog: StateFlow<Boolean> = _showQrDialog.asStateFlow()

  private val _showEmojiPicker = MutableStateFlow(false)
  val showEmojiPicker: StateFlow<Boolean> = _showEmojiPicker.asStateFlow()

  private val _showAboutDialog = MutableStateFlow(false)
  val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

  // Real connected devices list only (no simulated devices)
  private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
  val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()

  // Real URL Logs
  private val _urlLogs = MutableStateFlow<List<UrlLogEntry>>(emptyList())
  val urlLogs: StateFlow<List<UrlLogEntry>> = _urlLogs.asStateFlow()

  private val _urlSearchQuery = MutableStateFlow("")
  val urlSearchQuery: StateFlow<String> = _urlSearchQuery.asStateFlow()

  private val _selectedLogId = MutableStateFlow<String?>(null)
  val selectedLogId: StateFlow<String?> = _selectedLogId.asStateFlow()

  // Settings State & Drafts
  private val _bandwidthSettings = MutableStateFlow(BandwidthSettings())
  val bandwidthSettings: StateFlow<BandwidthSettings> = _bandwidthSettings.asStateFlow()
  val draftBandwidthSettings = MutableStateFlow(BandwidthSettings())

  private val _bandwidthTab = MutableStateFlow("Default Bandwidth")
  val bandwidthTab: StateFlow<String> = _bandwidthTab.asStateFlow()

  private val _securitySettings = MutableStateFlow(SecuritySettings())
  val securitySettings: StateFlow<SecuritySettings> = _securitySettings.asStateFlow()
  val draftSecuritySettings = MutableStateFlow(SecuritySettings())

  private val _generalSettings = MutableStateFlow(GeneralSettings())
  val generalSettings: StateFlow<GeneralSettings> = _generalSettings.asStateFlow()
  val draftGeneralSettings = MutableStateFlow(GeneralSettings())

  private val _dhcpSettings = MutableStateFlow(DhcpSettings())
  val dhcpSettings: StateFlow<DhcpSettings> = _dhcpSettings.asStateFlow()
  val draftDhcpSettings = MutableStateFlow(DhcpSettings())

  private val _portForwardRules = MutableStateFlow<List<PortForwardRule>>(emptyList())
  val portForwardRules: StateFlow<List<PortForwardRule>> = _portForwardRules.asStateFlow()

  private val _deviceFilterRules = MutableStateFlow<List<DeviceFilterRule>>(emptyList())
  val deviceFilterRules: StateFlow<List<DeviceFilterRule>> = _deviceFilterRules.asStateFlow()

  private val _firewallSettings = MutableStateFlow(FirewallSettings())
  val firewallSettings: StateFlow<FirewallSettings> = _firewallSettings.asStateFlow()
  val draftFirewallSettings = MutableStateFlow(FirewallSettings())

  private val _captivePortalSettings = MutableStateFlow(CaptivePortalSettings())
  val captivePortalSettings: StateFlow<CaptivePortalSettings> = _captivePortalSettings.asStateFlow()
  val draftCaptivePortalSettings = MutableStateFlow(CaptivePortalSettings())

  private val _fileSharingSettings = MutableStateFlow(FileSharingSettings())
  val fileSharingSettings: StateFlow<FileSharingSettings> = _fileSharingSettings.asStateFlow()
  val draftFileSharingSettings = MutableStateFlow(FileSharingSettings())

  // Real Speed Test
  private val _speedTestState = MutableStateFlow(SpeedTestState())
  val speedTestState: StateFlow<SpeedTestState> = _speedTestState.asStateFlow()

  // Real Hardware Traffic Monitor (TrafficStats)
  private val trafficMonitor = RealTrafficMonitor()
  private val _trafficHistory = MutableStateFlow<List<TrafficHistoryPoint>>(emptyList())
  val trafficHistory: StateFlow<List<TrafficHistoryPoint>> = _trafficHistory.asStateFlow()

  // Real Embedded Local Web Server (Captive Portal & File Sharing)
  private val localWebServer = LocalHotspotWebServer(application) { logEntry ->
    _urlLogs.update { list ->
      listOf(logEntry) + list.take(199)
    }
  }

  val localWebServerPort: Int
    get() = localWebServer.serverPort

  private var activeHotspotJob: Job? = null
  private var speedTestJob: Job? = null

  init {
    refreshAvailableConnections()

    // Initialize blank traffic baseline
    val initialPoints = mutableListOf<TrafficHistoryPoint>()
    val now = System.currentTimeMillis() / 1000
    for (i in 20 downTo 0) {
      initialPoints.add(
        TrafficHistoryPoint(
          timestampSec = now - i,
          downloadMbit = 0f,
          uploadMbit = 0f
        )
      )
    }
    _trafficHistory.value = initialPoints
  }

  fun refreshAvailableConnections() {
    val connections = realNetworkManager.getAvailableInternetConnections()
    _availableInternetConnections.value = connections
    if (connections.isNotEmpty()) {
      _internetConnection.value = connections.first().displayName
    }
  }

  fun setNavDestination(destination: NavDestination) {
    _currentNavDestination.value = destination
  }

  fun setSettingsSection(section: SettingsSection) {
    _currentSettingsSection.value = section
    // Refresh drafts
    draftGeneralSettings.value = _generalSettings.value
    draftSecuritySettings.value = _securitySettings.value
    draftBandwidthSettings.value = _bandwidthSettings.value
    draftDhcpSettings.value = _dhcpSettings.value
    draftFirewallSettings.value = _firewallSettings.value
    draftCaptivePortalSettings.value = _captivePortalSettings.value
    draftFileSharingSettings.value = _fileSharingSettings.value
  }

  fun toggleDarkTheme() {
    _darkTheme.value = !_darkTheme.value
  }

  fun setHotspotMode(mode: HotspotMode) {
    _hotspotMode.value = mode
    when (mode) {
      HotspotMode.WLAN_HOTSPOT -> {
        _networkAccess.value = "Router Mode (NAT)"
      }
      HotspotMode.WLAN_REPEATER -> {
        _networkAccess.value = "Bridge Mode (Repeater)"
        if (!realNetworkManager.isWifiConnected()) {
          Toast.makeText(
            getApplication(),
            "Notice: WLAN Repeater mode requires an active Wi-Fi connection to repeat.",
            Toast.LENGTH_LONG
          ).show()
        }
      }
      HotspotMode.MULTIFUNCTIONAL -> {
        _networkAccess.value = "Multifunctional Mode"
      }
    }
  }

  fun setNetworkAccess(mode: String) {
    _networkAccess.value = mode
  }

  fun setInternetConnection(conn: String) {
    _internetConnection.value = conn
  }

  fun setSsid(name: String) {
    _ssid.value = name
    _activeIpDetails.value = "$name ($localIpAddress)"
  }

  fun setPassword(pass: String) {
    _password.value = pass
  }

  fun togglePasswordVisibility() {
    _isPasswordVisible.value = !_isPasswordVisible.value
  }

  fun setShowQrDialog(show: Boolean) {
    _showQrDialog.value = show
  }

  fun setShowEmojiPicker(show: Boolean) {
    _showEmojiPicker.value = show
  }

  fun setShowAboutDialog(show: Boolean) {
    _showAboutDialog.value = show
  }

  fun appendEmojiToSsid(emoji: String) {
    _ssid.value = _ssid.value + emoji
    _activeIpDetails.value = "${_ssid.value} ($localIpAddress)"
  }

  fun toggleHotspot() {
    if (_isHotspotActive.value) {
      stopHotspot()
    } else {
      startHotspot()
    }
  }

  fun startHotspot() {
    if (_password.value.length < 8) {
      Toast.makeText(
        getApplication(),
        "Network Key must be at least 8 characters",
        Toast.LENGTH_SHORT
      ).show()
      return
    }

    if (!hardwareManager.hasRequiredPermissions()) {
      _showPermissionDialog.value = true
      return
    }

    // Keep user's exact SSID, key, and real local IP - never random!
    _isHotspotActive.value = true
    _uptimeSeconds.value = 0L
    _activeIpDetails.value = "${_ssid.value} ($localIpAddress)"

    // Query real connected hardware devices from ARP table - strictly NO fake mock devices
    _connectedDevices.value = ConnectedDeviceScanner.scanConnectedDevices(getApplication())

    // Start real hardware TrafficStats sampling
    trafficMonitor.startSession()
    startRealTrafficMonitoring()

    // Start Real Embedded Local Web Server (Captive Portal & File Sharing)
    localWebServer.captivePortalSettings = _captivePortalSettings.value
    localWebServer.fileSharingSettings = _fileSharingSettings.value
    localWebServer.isLoggingEnabled = _securitySettings.value.enableUrlLogging
    localWebServer.start()

    val isWifiDirect = _networkAccess.value.contains("WiFi Direct", ignoreCase = true)
    hardwareManager.startHardwareHotspot(useWifiDirect = isWifiDirect) { result ->
      when (result) {
        is HotspotStartResult.Success -> {
          _isHardwareBroadcasting.value = true
          _hardwareStatusMessage.value = "Broadcasting Wi-Fi as \"${_ssid.value}\". Look for this network on other devices or scan the QR Code."
          _hardwareErrorMessage.value = null

          Toast.makeText(
            getApplication(),
            "Broadcasting Wi-Fi as: ${_ssid.value}",
            Toast.LENGTH_LONG
          ).show()
        }

        is HotspotStartResult.PermissionRequired -> {
          _showPermissionDialog.value = true
        }

        is HotspotStartResult.WifiDisabled -> {
          _hardwareErrorMessage.value = "Wi-Fi antenna is currently turned off. Please turn on Wi-Fi in your device settings."
          _showHotspotHelpDialog.value = true
        }

        is HotspotStartResult.Failed -> {
          _isHardwareBroadcasting.value = false
          _hardwareErrorMessage.value = result.message
          _hardwareStatusMessage.value = "To broadcast over the air to other phones, turn on Mobile Hotspot in Android Settings."
          _showHotspotHelpDialog.value = true
        }
      }
    }
  }

  fun stopHotspot() {
    _isHotspotActive.value = false
    _isHardwareBroadcasting.value = false
    _hardwareStatusMessage.value = null
    hardwareManager.stopHardwareHotspot()
    localWebServer.stop()
    activeHotspotJob?.cancel()
    _connectedDevices.value = emptyList()

    Toast.makeText(
      getApplication(),
      "Hotspot stopped",
      Toast.LENGTH_SHORT
    ).show()
  }

  fun dismissPermissionDialog() {
    _showPermissionDialog.value = false
  }

  fun dismissHelpDialog() {
    _showHotspotHelpDialog.value = false
  }

  fun setShowHotspotHelpDialog(show: Boolean) {
    _showHotspotHelpDialog.value = show
  }

  private fun startRealTrafficMonitoring() {
    activeHotspotJob?.cancel()
    activeHotspotJob = viewModelScope.launch {
      while (isActive && _isHotspotActive.value) {
        delay(1000)
        _uptimeSeconds.value += 1

        // 1. REAL TrafficStats throughput from physical network interface
        val snapshot = withContext(Dispatchers.Default) {
          trafficMonitor.sample()
        }

        val nowSec = System.currentTimeMillis() / 1000
        val history = _trafficHistory.value.toMutableList()
        if (history.size >= 30) history.removeAt(0)
        history.add(
          TrafficHistoryPoint(
            timestampSec = nowSec,
            downloadMbit = snapshot.downloadSpeedMbit,
            uploadMbit = snapshot.uploadSpeedMbit
          )
        )
        _trafficHistory.value = history

        // 2. Scan for actual connected hardware clients via ARP every 2 seconds
        if (_uptimeSeconds.value % 2L == 0L) {
          withContext(Dispatchers.IO) {
            // Light probe on subnet to refresh ARP table
            ConnectedDeviceScanner.probeSubnet(localIpAddress)
            val realDevices = ConnectedDeviceScanner.scanConnectedDevices(getApplication())

            _connectedDevices.update { existing ->
              if (realDevices.isEmpty()) {
                emptyList()
              } else {
                realDevices.map { scanned ->
                  val prev = existing.find { it.ip == scanned.ip || it.mac == scanned.mac }
                  if (prev != null) {
                    scanned.copy(
                      isBlocked = prev.isBlocked,
                      downloadSpeedMbit = snapshot.downloadSpeedMbit / realDevices.size.toFloat(),
                      uploadSpeedMbit = snapshot.uploadSpeedMbit / realDevices.size.toFloat(),
                      totalDownloadedMb = prev.totalDownloadedMb + (snapshot.downloadSpeedMbit * 0.125f),
                      totalUploadedMb = prev.totalUploadedMb + (snapshot.uploadSpeedMbit * 0.125f)
                    )
                  } else scanned
                }
              }
            }
          }
        }
      }
    }
  }

  // Device Management
  fun toggleBlockDevice(deviceId: String) {
    _connectedDevices.update { list ->
      list.map { dev ->
        if (dev.id == deviceId) {
          val newBlocked = !dev.isBlocked
          val statusText = if (newBlocked) "Blocked" else "Unblocked"
          Toast.makeText(getApplication(), "${dev.name} is now $statusText", Toast.LENGTH_SHORT).show()
          dev.copy(isBlocked = newBlocked)
        } else dev
      }
    }
  }

  fun removeDevice(deviceId: String) {
    _connectedDevices.update { list -> list.filterNot { it.id == deviceId } }
  }

  fun refreshConnectedDevices() {
    viewModelScope.launch(Dispatchers.IO) {
      ConnectedDeviceScanner.probeSubnet(localIpAddress)
      val realDevices = ConnectedDeviceScanner.scanConnectedDevices(getApplication())
      _connectedDevices.value = realDevices
      withContext(Dispatchers.Main) {
        if (realDevices.isEmpty()) {
          Toast.makeText(getApplication(), "No active connected devices detected", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(getApplication(), "${realDevices.size} active device(s) connected", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  // Bandwidth Manager
  fun setBandwidthTab(tab: String) {
    _bandwidthTab.value = tab
  }

  fun saveBandwidthSettings() {
    _bandwidthSettings.value = draftBandwidthSettings.value
    Toast.makeText(getApplication(), "Bandwidth settings saved", Toast.LENGTH_SHORT).show()
  }

  fun discardBandwidthSettings() {
    draftBandwidthSettings.value = _bandwidthSettings.value
    Toast.makeText(getApplication(), "Bandwidth changes discarded", Toast.LENGTH_SHORT).show()
  }

  // Real Speed Test Runner
  fun runSpeedTest() {
    if (_speedTestState.value.isRunning) return

    speedTestJob?.cancel()
    speedTestJob = viewModelScope.launch {
      RealSpeedTester.executeSpeedTest { updateState ->
        _speedTestState.value = updateState
      }
    }
  }

  // Security Settings
  fun saveSecuritySettings() {
    _securitySettings.value = draftSecuritySettings.value
    localWebServer.isLoggingEnabled = draftSecuritySettings.value.enableUrlLogging
    Toast.makeText(getApplication(), "Security settings saved", Toast.LENGTH_SHORT).show()
  }

  fun discardSecuritySettings() {
    draftSecuritySettings.value = _securitySettings.value
    Toast.makeText(getApplication(), "Security changes discarded", Toast.LENGTH_SHORT).show()
  }

  // General Settings
  fun saveGeneralSettings() {
    _generalSettings.value = draftGeneralSettings.value
    Toast.makeText(getApplication(), "General settings saved", Toast.LENGTH_SHORT).show()
  }

  fun discardGeneralSettings() {
    draftGeneralSettings.value = _generalSettings.value
    Toast.makeText(getApplication(), "General changes discarded", Toast.LENGTH_SHORT).show()
  }

  // DHCP Settings
  fun saveDhcpSettings() {
    _dhcpSettings.value = draftDhcpSettings.value
    Toast.makeText(getApplication(), "DHCP settings saved", Toast.LENGTH_SHORT).show()
  }

  fun discardDhcpSettings() {
    draftDhcpSettings.value = _dhcpSettings.value
    Toast.makeText(getApplication(), "DHCP changes discarded", Toast.LENGTH_SHORT).show()
  }

  // Firewall Settings
  fun saveFirewallSettings() {
    _firewallSettings.value = draftFirewallSettings.value
    Toast.makeText(getApplication(), "Firewall settings saved", Toast.LENGTH_SHORT).show()
  }

  fun discardFirewallSettings() {
    draftFirewallSettings.value = _firewallSettings.value
    Toast.makeText(getApplication(), "Firewall changes discarded", Toast.LENGTH_SHORT).show()
  }

  // Captive Portal
  fun saveCaptivePortalSettings() {
    _captivePortalSettings.value = draftCaptivePortalSettings.value
    localWebServer.captivePortalSettings = draftCaptivePortalSettings.value
    Toast.makeText(getApplication(), "Captive Portal settings saved & applied to local server", Toast.LENGTH_SHORT).show()
  }

  fun discardCaptivePortalSettings() {
    draftCaptivePortalSettings.value = _captivePortalSettings.value
    Toast.makeText(getApplication(), "Captive Portal changes discarded", Toast.LENGTH_SHORT).show()
  }

  // File Sharing
  fun saveFileSharingSettings() {
    _fileSharingSettings.value = draftFileSharingSettings.value
    localWebServer.fileSharingSettings = draftFileSharingSettings.value
    Toast.makeText(getApplication(), "File Sharing settings saved & applied to local server", Toast.LENGTH_SHORT).show()
  }

  fun discardFileSharingSettings() {
    draftFileSharingSettings.value = _fileSharingSettings.value
    Toast.makeText(getApplication(), "File Sharing changes discarded", Toast.LENGTH_SHORT).show()
  }

  // URL Logs Actions
  fun setUrlSearchQuery(query: String) {
    _urlSearchQuery.value = query
  }

  fun selectLogEntry(id: String) {
    _selectedLogId.value = id
  }

  fun clearUrlLogs() {
    _urlLogs.value = emptyList()
    Toast.makeText(getApplication(), "URL logs cleared", Toast.LENGTH_SHORT).show()
  }

  // Port Forwarding & Device Filters
  fun addPortForwardRule(rule: PortForwardRule) {
    _portForwardRules.update { it + rule }
  }

  fun removePortForwardRule(id: String) {
    _portForwardRules.update { it.filterNot { r -> r.id == id } }
  }

  fun addDeviceFilterRule(rule: DeviceFilterRule) {
    _deviceFilterRules.update { it + rule }
  }

  fun removeDeviceFilterRule(id: String) {
    _deviceFilterRules.update { it.filterNot { r -> r.id == id } }
  }

  // Android System Intents
  fun openSystemHotspotSettings(context: Context) {
    hardwareManager.openSystemHotspotSettings(context)
  }

  fun shareWifiCredentials(context: Context) {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(
        Intent.EXTRA_TEXT,
        "Wi-Fi Network: ${_ssid.value}\nPassword: ${_password.value}\nConnect with MyPublicWiFi"
      )
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Wi-Fi Credentials")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(shareIntent)
  }

  fun openOfficialWebsite(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mypublicwifi.com"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
      context.startActivity(intent)
    } catch (_: Exception) {
      Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onCleared() {
    super.onCleared()
    stopHotspot()
  }
}
