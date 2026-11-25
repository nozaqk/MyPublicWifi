package com.example.util

import android.content.Context
import com.example.model.ConnectedDevice
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ConnectedDeviceScanner {

  /**
   * Scans for actual connected client devices using the system ARP table (/proc/net/arp).
   * Deduplicates by IP/MAC and filters out invalid addresses.
   */
  fun scanConnectedDevices(context: Context): List<ConnectedDevice> {
    val devices = mutableListOf<ConnectedDevice>()
    val arpFilter = listOf("00:00:00:00:00:00", "0:0:0:0:0:0")
    val seenIps = mutableSetOf<String>()
    val seenMacs = mutableSetOf<String>()

    try {
      val arpTable = File("/proc/net/arp")
      if (arpTable.exists() && arpTable.canRead()) {
        BufferedReader(FileReader(arpTable)).use { reader ->
          var line: String? = reader.readLine() // skip header line
          val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

          while (reader.readLine().also { line = it } != null) {
            val tokens = line?.trim()?.split("\\s+".toRegex()) ?: continue
            // format: IP, HW_type, Flags, HW_address, Mask, Device
            if (tokens.size >= 4) {
              val ip = tokens[0]
              val flags = tokens[2]
              val mac = tokens[3].uppercase(Locale.ROOT)

              // Flags 0x2 indicates a resolved active client; ignore 0x0 (incomplete)
              if (flags != "0x0" &&
                  !arpFilter.contains(mac) &&
                  !ip.equals("IP", ignoreCase = true) &&
                  !seenIps.contains(ip) &&
                  !seenMacs.contains(mac)
              ) {
                seenIps.add(ip)
                seenMacs.add(mac)

                val devName = resolveDeviceName(ip)
                val sanitizedId = "dev_${ip.replace('.', '_')}_${mac.replace(":", "").replace("-", "")}"

                devices.add(
                  ConnectedDevice(
                    id = sanitizedId,
                    name = devName,
                    ip = ip,
                    mac = mac,
                    accessTime = dateFormat.format(Date()),
                    user = "Guest",
                    downloadSpeedMbit = 0f,
                    uploadSpeedMbit = 0f,
                    totalDownloadedMb = 0f,
                    totalUploadedMb = 0f,
                    isBlocked = false
                  )
                )
              }
            }
          }
        }
      }
    } catch (_: Exception) {}

    return devices
  }

  /**
   * Lightly probes the local subnet to trigger ARP resolution for freshly connected clients.
   */
  fun probeSubnet(baseIp: String) {
    val parts = baseIp.split(".")
    if (parts.size == 4) {
      val prefix = "${parts[0]}.${parts[1]}.${parts[2]}"
      for (i in 2..20) {
        val targetIp = "$prefix.$i"
        try {
          Socket().use { s ->
            s.connect(InetSocketAddress(targetIp, 80), 20)
          }
        } catch (_: Exception) {}
      }
    }
  }

  /**
   * Resolves the device's actual network IP address dynamically.
   */
  fun getLocalIpAddress(): String {
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces()
      while (interfaces.hasMoreElements()) {
        val intf = interfaces.nextElement()
        if (intf.isLoopback || !intf.isUp) continue

        val addresses = intf.inetAddresses
        while (addresses.hasMoreElements()) {
          val addr = addresses.nextElement()
          if (!addr.isLoopbackAddress && addr is Inet4Address) {
            val host = addr.hostAddress
            if (!host.isNullOrBlank() && !host.startsWith("127.")) {
              return host
            }
          }
        }
      }
    } catch (_: Exception) {}
    return "192.168.43.1"
  }

  private fun resolveDeviceName(ip: String): String {
    return try {
      val inet = InetAddress.getByName(ip)
      val host = inet.canonicalHostName ?: inet.hostName
      if (!host.isNullOrBlank() && host != ip) {
        host
      } else {
        "Connected Device (${ip.substringAfterLast('.')})"
      }
    } catch (_: Exception) {
      "Connected Device (${ip.substringAfterLast('.')})"
    }
  }
}
