package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import java.net.Inet4Address
import java.net.NetworkInterface

data class NetworkConnectionOption(
  val id: String,
  val displayName: String,
  val interfaceName: String?,
  val ipAddress: String,
  val isConnected: Boolean
)

class RealNetworkManager(private val context: Context) {

  private val connectivityManager =
    context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  private val wifiManager =
    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
  private val telephonyManager =
    context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

  fun hasWifiDirectSupport(): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
  }

  fun isWifiConnected(): Boolean {
    val activeNet = connectivityManager?.activeNetwork ?: return false
    val caps = connectivityManager.getNetworkCapabilities(activeNet) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  }

  fun isMobileDataConnected(): Boolean {
    val activeNet = connectivityManager?.activeNetwork ?: return false
    val caps = connectivityManager.getNetworkCapabilities(activeNet) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
  }

  fun getAvailableInternetConnections(): List<NetworkConnectionOption> {
    val options = mutableListOf<NetworkConnectionOption>()
    val localIp = ConnectedDeviceScanner.getLocalIpAddress()

    // 1. Automatic Option (Primary active network)
    val autoDesc = if (isWifiConnected()) {
      val wifiSsid = getWifiSsid()
      "Automatic ($wifiSsid - $localIp)"
    } else if (isMobileDataConnected()) {
      val op = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() } ?: "Cellular"
      "Automatic ($op - $localIp)"
    } else {
      "Automatic ($localIp)"
    }
    options.add(
      NetworkConnectionOption(
        id = "auto",
        displayName = autoDesc,
        interfaceName = null,
        ipAddress = localIp,
        isConnected = true
      )
    )

    // 2. Scan physical network interfaces
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces()
      while (interfaces.hasMoreElements()) {
        val intf = interfaces.nextElement()
        if (intf.isLoopback || !intf.isUp) continue

        val name = intf.name.lowercase()
        val addresses = intf.inetAddresses
        var ipv4: String? = null
        while (addresses.hasMoreElements()) {
          val addr = addresses.nextElement()
          if (!addr.isLoopbackAddress && addr is Inet4Address) {
            val host = addr.hostAddress
            if (!host.isNullOrBlank() && !host.startsWith("127.")) {
              ipv4 = host
              break
            }
          }
        }

        if (ipv4 != null) {
          when {
            name.startsWith("wlan") || name.startsWith("p2p") || name.startsWith("ap") -> {
              val wifiSsid = getWifiSsid()
              val label = if (wifiSsid.isNotBlank() && wifiSsid != "<unknown ssid>") {
                "WLAN: $wifiSsid ($ipv4)"
              } else {
                "WLAN ($ipv4)"
              }
              options.add(
                NetworkConnectionOption(
                  id = "wlan_${intf.name}",
                  displayName = label,
                  interfaceName = intf.name,
                  ipAddress = ipv4,
                  isConnected = true
                )
              )
            }
            name.startsWith("rmnet") || name.startsWith("ccmni") || name.startsWith("radio") -> {
              val carrier = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() } ?: "Mobile Data"
              options.add(
                NetworkConnectionOption(
                  id = "mobile_${intf.name}",
                  displayName = "$carrier ($ipv4)",
                  interfaceName = intf.name,
                  ipAddress = ipv4,
                  isConnected = true
                )
              )
            }
            name.startsWith("eth") || name.startsWith("rndis") || name.startsWith("usb") -> {
              options.add(
                NetworkConnectionOption(
                  id = "eth_${intf.name}",
                  displayName = "Ethernet / USB ($ipv4)",
                  interfaceName = intf.name,
                  ipAddress = ipv4,
                  isConnected = true
                )
              )
            }
          }
        }
      }
    } catch (_: Exception) {}

    // Fallbacks if no specific interface was parsed
    if (options.none { it.id.startsWith("wlan") }) {
      options.add(
        NetworkConnectionOption(
          id = "wlan_default",
          displayName = "WLAN ($localIp)",
          interfaceName = "wlan0",
          ipAddress = localIp,
          isConnected = isWifiConnected()
        )
      )
    }

    if (options.none { it.id.startsWith("mobile") }) {
      options.add(
        NetworkConnectionOption(
          id = "mobile_default",
          displayName = "Mobile Data (LTE/5G)",
          interfaceName = "rmnet0",
          ipAddress = localIp,
          isConnected = isMobileDataConnected()
        )
      )
    }

    return options
  }

  @Suppress("DEPRECATION")
  private fun getWifiSsid(): String {
    return try {
      val info = wifiManager?.connectionInfo
      val raw = info?.ssid ?: ""
      raw.removePrefix("\"").removeSuffix("\"").takeIf { it != "<unknown ssid>" } ?: "Active Wi-Fi"
    } catch (_: Exception) {
      "Active Wi-Fi"
    }
  }
}
