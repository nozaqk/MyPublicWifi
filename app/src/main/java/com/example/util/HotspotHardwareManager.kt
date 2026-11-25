package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat

sealed class HotspotStartResult {
  data class Success(val ssid: String, val password: String, val isHardwareAp: Boolean) : HotspotStartResult()
  data class PermissionRequired(val permissions: List<String>) : HotspotStartResult()
  data class WifiDisabled(val message: String) : HotspotStartResult()
  data class Failed(val reasonCode: Int, val message: String) : HotspotStartResult()
}

class HotspotHardwareManager(private val context: Context) {

  private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
  private val p2pManager = context.applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
  private var p2pChannel: WifiP2pManager.Channel? = null
  private var isP2pActive: Boolean = false

  private var reservation: WifiManager.LocalOnlyHotspotReservation? = null

  fun getRequiredPermissions(): List<String> {
    val perms = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
    return perms
  }

  fun hasRequiredPermissions(): Boolean {
    val perms = getRequiredPermissions()
    return perms.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
  }

  fun isWifiEnabled(): Boolean {
    return wifiManager?.isWifiEnabled == true
  }

  fun isHardwareHotspotActive(): Boolean {
    return reservation != null || isP2pActive
  }

  @SuppressLint("MissingPermission")
  fun startHardwareHotspot(
    useWifiDirect: Boolean = false,
    onResult: (HotspotStartResult) -> Unit
  ) {
    if (!hasRequiredPermissions()) {
      onResult(HotspotStartResult.PermissionRequired(getRequiredPermissions()))
      return
    }

    if (useWifiDirect && p2pManager != null) {
      startWifiDirectGroup(onResult)
      return
    }

    if (wifiManager == null) {
      onResult(HotspotStartResult.Failed(-1, "Wi-Fi hardware manager is not available on this device."))
      return
    }

    // Stop previous reservation if existing
    stopHardwareHotspot()

    try {
      wifiManager.startLocalOnlyHotspot(
        object : WifiManager.LocalOnlyHotspotCallback() {
          override fun onStarted(activeReservation: WifiManager.LocalOnlyHotspotReservation) {
            super.onStarted(activeReservation)
            reservation = activeReservation

            var broadcastSsid = "AndroidHotspot"
            var broadcastPassword = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
              val softAp = activeReservation.softApConfiguration
              if (softAp != null) {
                broadcastSsid = softAp.ssid ?: broadcastSsid
                broadcastPassword = softAp.passphrase ?: ""
              } else {
                @Suppress("DEPRECATION")
                val wifiConfig = activeReservation.wifiConfiguration
                if (wifiConfig != null) {
                  broadcastSsid = wifiConfig.SSID ?: broadcastSsid
                  broadcastPassword = wifiConfig.preSharedKey ?: ""
                }
              }
            } else {
              @Suppress("DEPRECATION")
              val wifiConfig = activeReservation.wifiConfiguration
              if (wifiConfig != null) {
                broadcastSsid = wifiConfig.SSID ?: broadcastSsid
                broadcastPassword = wifiConfig.preSharedKey ?: ""
              }
            }

            broadcastSsid = broadcastSsid.removePrefix("\"").removeSuffix("\"")
            broadcastPassword = broadcastPassword.removePrefix("\"").removeSuffix("\"")

            onResult(HotspotStartResult.Success(broadcastSsid, broadcastPassword, true))
          }

          override fun onStopped() {
            super.onStopped()
            reservation = null
          }

          override fun onFailed(reason: Int) {
            super.onFailed(reason)
            reservation = null
            val msg = when (reason) {
              ERROR_NO_CHANNEL -> "Wi-Fi channel currently unavailable."
              ERROR_GENERIC -> "Wi-Fi hardware reported a generic error or is in use by another app."
              ERROR_INCOMPATIBLE_MODE -> "Wi-Fi is currently in an incompatible mode. Turn on Wi-Fi or use System Hotspot."
              ERROR_TETHERING_DISALLOWED -> "Local-only hotspot is disallowed by device policy. Use System Mobile Hotspot instead."
              else -> "Failed to start hardware hotspot (Code: $reason)."
            }
            onResult(HotspotStartResult.Failed(reason, msg))
          }
        },
        Handler(Looper.getMainLooper())
      )
    } catch (e: SecurityException) {
      onResult(HotspotStartResult.PermissionRequired(getRequiredPermissions()))
    } catch (e: Exception) {
      onResult(HotspotStartResult.Failed(-2, e.message ?: "Failed to start Wi-Fi hotspot"))
    }
  }

  @SuppressLint("MissingPermission")
  fun startWifiDirectGroup(onResult: (HotspotStartResult) -> Unit) {
    if (p2pManager == null) {
      onResult(HotspotStartResult.Failed(-1, "Wi-Fi Direct not supported on this device."))
      return
    }

    try {
      p2pChannel = p2pManager.initialize(context, Looper.getMainLooper(), null)
      p2pManager.createGroup(p2pChannel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
          isP2pActive = true
          p2pManager.requestGroupInfo(p2pChannel) { group: WifiP2pGroup? ->
            val netName = group?.networkName ?: "DIRECT-MyPublicWiFi"
            val pass = group?.passphrase ?: ""
            onResult(HotspotStartResult.Success(netName, pass, true))
          }
        }

        override fun onFailure(reason: Int) {
          isP2pActive = false
          // Fall back to standard SoftAP
          startHardwareHotspot(useWifiDirect = false, onResult = onResult)
        }
      })
    } catch (e: Exception) {
      startHardwareHotspot(useWifiDirect = false, onResult = onResult)
    }
  }

  fun stopHardwareHotspot() {
    try {
      reservation?.close()
    } catch (_: Exception) {}
    reservation = null

    if (isP2pActive && p2pManager != null && p2pChannel != null) {
      try {
        @SuppressLint("MissingPermission")
        p2pManager.removeGroup(p2pChannel, null)
      } catch (_: Exception) {}
      isP2pActive = false
    }
  }

  fun openSystemHotspotSettings(context: Context) {
    val intents = listOf(
      Intent("android.settings.WIFI_AP_SETTINGS"),
      Intent("android.settings.TETHER_SETTINGS"),
      Intent(Settings.ACTION_WIRELESS_SETTINGS),
      Intent(Settings.ACTION_SETTINGS)
    )

    for (intent in intents) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      if (intent.resolveActivity(context.packageManager) != null) {
        try {
          context.startActivity(intent)
          return
        } catch (_: Exception) {}
      }
    }
  }
}
