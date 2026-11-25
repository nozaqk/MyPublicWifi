package com.example.model

enum class NavDestination {
  HOTSPOT,
  DEVICES,
  SETTINGS,
  STATISTICS,
  URL_LOGS
}

enum class SettingsSection(val label: String) {
  GENERAL("General"),
  SECURITY("Security"),
  DHCP_SERVER("DHCP Server"),
  BANDWIDTH_MANAGER("Bandwidth Manager"),
  PORT_FORWARDING("Port Forwarding"),
  DEVICE_FILTER("Device Filter"),
  FIREWALL("Firewall"),
  AUTH_DESIGN("Authentication / Design"),
  FILE_SHARING("File Sharing")
}

enum class HotspotMode(val title: String) {
  WLAN_HOTSPOT("WLAN Hotspot"),
  WLAN_REPEATER("WLAN Repeater"),
  MULTIFUNCTIONAL("Multifunctional Hotspot")
}

data class ConnectedDevice(
  val id: String,
  val name: String,
  val ip: String,
  val mac: String,
  val accessTime: String,
  val user: String = "Guest",
  val downloadSpeedMbit: Float = 0.0f,
  val uploadSpeedMbit: Float = 0.0f,
  val totalDownloadedMb: Float = 14.2f,
  val totalUploadedMb: Float = 3.8f,
  val isBlocked: Boolean = false,
  val customSpeedLimitMbit: Float? = null
)

data class UrlLogEntry(
  val id: String,
  val accessTime: String,
  val user: String,
  val deviceName: String,
  val mac: String,
  val url: String
)

data class BandwidthSettings(
  val enabled: Boolean = true,
  val mode: String = "Each client is assigned a statically defined bandwidth",
  val downloadLimitEnabled: Boolean = true,
  val downloadSpeed: Float = 3.0f,
  val downloadUnit: String = "Mbit/s",
  val uploadLimitEnabled: Boolean = true,
  val uploadSpeed: Float = 1.0f,
  val uploadUnit: String = "Mbit/s"
)

data class SecuritySettings(
  val enableAdBlocker: Boolean = false,
  val blockSocialNetworks: Boolean = false,
  val blockAccessToLocalHost: Boolean = false,
  val blockAccessToLocalNetwork: Boolean = false,
  val blockInternetUsage: Boolean = false,
  val blockUPnP: Boolean = false,
  val limitMaxClients: Boolean = false,
  val maxClientsCount: Int = 254,
  val enableUrlLogging: Boolean = true
)

data class GeneralSettings(
  val startAtStartup: Boolean = true,
  val startMinimized: Boolean = true,
  val autoStartHotspotOnOpen: Boolean = true,
  val disablePowerManagement: Boolean = true,
  val preferredWifiTech: String = "Wi-Fi Direct"
)

data class DhcpSettings(
  val startIp: String = "192.168.137.2",
  val endIp: String = "192.168.137.254",
  val subnetMask: String = "255.255.255.0",
  val gateway: String = "192.168.137.1",
  val leaseTimeHours: Int = 24
)

data class PortForwardRule(
  val id: String,
  val serviceName: String,
  val protocol: String = "TCP",
  val externalPort: Int,
  val internalIp: String,
  val internalPort: Int,
  val enabled: Boolean = true
)

data class DeviceFilterRule(
  val id: String,
  val deviceName: String,
  val mac: String,
  val mode: String = "Blocked" // "Allowed", "Blocked"
)

data class FirewallSettings(
  val blockPorts: String = "21, 23, 25, 445",
  val allowDnsOnly: Boolean = false,
  val preventPortScanning: Boolean = true,
  val blockPingRequests: Boolean = false
)

data class CaptivePortalSettings(
  val enabled: Boolean = false,
  val portalTitle: String = "Welcome to MyPublicWiFi",
  val requireTerms: Boolean = true,
  val welcomeMessage: String = "Please click accept to enjoy free high-speed wireless internet.",
  val voucherAuthEnabled: Boolean = false
)

data class FileSharingSettings(
  val enabled: Boolean = false,
  val shareName: String = "Public Shared Folder",
  val allowUploads: Boolean = true,
  val requirePassword: Boolean = false,
  val password: String = "shared123"
)

data class SpeedTestState(
  val isRunning: Boolean = false,
  val progress: Float = 0f,
  val phase: String = "Ready",
  val pingMs: Int = 18,
  val jitterMs: Int = 3,
  val downloadSpeedMbit: Float = 42.6f,
  val uploadSpeedMbit: Float = 14.8f
)

data class TrafficHistoryPoint(
  val timestampSec: Long,
  val downloadMbit: Float,
  val uploadMbit: Float
)
