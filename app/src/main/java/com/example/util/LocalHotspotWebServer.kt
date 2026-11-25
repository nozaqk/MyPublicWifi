package com.example.util

import android.content.Context
import com.example.model.CaptivePortalSettings
import com.example.model.FileSharingSettings
import com.example.model.UrlLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalHotspotWebServer(
  private val context: Context,
  private val onUrlLogged: (UrlLogEntry) -> Unit
) {

  private var serverSocket: ServerSocket? = null
  private var serverJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.IO)

  var captivePortalSettings: CaptivePortalSettings = CaptivePortalSettings()
  var fileSharingSettings: FileSharingSettings = FileSharingSettings()
  var isLoggingEnabled: Boolean = true

  var serverPort: Int = 8080
    private set

  val isRunning: Boolean
    get() = serverSocket?.isClosed == false

  fun start(preferredPort: Int = 8080) {
    if (isRunning) return
    stop()

    try {
      serverPort = preferredPort
      val socket = try {
        ServerSocket(serverPort)
      } catch (_: Exception) {
        serverPort = if (preferredPort != 8888) 8888 else 8089
        ServerSocket(serverPort)
      }
      serverSocket = socket

      serverJob = scope.launch {
        while (isActive && serverSocket?.isClosed == false) {
          try {
            val client = serverSocket?.accept() ?: break
            launch {
              handleClient(client)
            }
          } catch (_: Exception) {
            break
          }
        }
      }
    } catch (_: Exception) {
      stop()
    }
  }

  fun stop() {
    try {
      serverSocket?.close()
    } catch (_: Exception) {}
    serverSocket = null
    serverJob?.cancel()
  }

  private fun handleClient(socket: Socket) {
    try {
      socket.soTimeout = 5000
      val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
      val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
      val out = socket.getOutputStream()

      val requestLine = reader.readLine() ?: return
      val parts = requestLine.split(" ")
      if (parts.size < 2) return

      val method = parts[0]
      val rawPath = parts[1]
      val path = rawPath.substringBefore("?")

      var hostHeader = clientIp
      var contentLength = 0
      var line: String?

      while (reader.readLine().also { line = it } != null) {
        if (line.isNullOrBlank()) break
        val lower = line?.lowercase(Locale.ROOT) ?: ""
        if (lower.startsWith("host:")) {
          hostHeader = line?.substringAfter(":")?.trim() ?: hostHeader
        } else if (lower.startsWith("content-length:")) {
          contentLength = line?.substringAfter(":")?.trim()?.toIntOrNull() ?: 0
        }
      }

      // Record Real URL Log
      if (isLoggingEnabled) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val fullUrl = if (hostHeader.isNotBlank() && !hostHeader.contains("localhost")) {
          "http://$hostHeader$rawPath"
        } else {
          rawPath
        }

        val logEntry = UrlLogEntry(
          id = "log_${System.currentTimeMillis()}_${(10..99).random()}",
          accessTime = dateFormat.format(Date()),
          user = "Guest",
          deviceName = "Client ($clientIp)",
          mac = "Connected via Wi-Fi",
          url = fullUrl
        )
        onUrlLogged(logEntry)
      }

      // Route Requests
      when {
        path == "/files" || path.startsWith("/files/") -> {
          handleFileSharing(path, out)
        }
        path == "/connect" || path == "/login" -> {
          handlePortalConnect(out)
        }
        path == "/generate_204" || path == "/hotspot-detect.html" || path == "/" || captivePortalSettings.enabled -> {
          handleCaptivePortal(out)
        }
        else -> {
          handleDefaultResponse(path, out)
        }
      }
    } catch (_: Exception) {
    } finally {
      try {
        socket.close()
      } catch (_: Exception) {}
    }
  }

  private fun handleCaptivePortal(out: OutputStream) {
    val title = escapeHtml(captivePortalSettings.portalTitle.ifBlank { "MyPublicWiFi Hotspot" })
    val welcome = escapeHtml(captivePortalSettings.welcomeMessage.ifBlank { "Welcome to the wireless hotspot network." })
    val requireTerms = captivePortalSettings.requireTerms

    val html = """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>$title</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #F1F5F9; color: #1E293B; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 90vh; }
          .card { background: white; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); padding: 28px; width: 100%; max-width: 420px; border-top: 5px solid #0096E6; }
          .logo { font-size: 22px; font-weight: bold; color: #0096E6; display: flex; align-items: center; margin-bottom: 12px; }
          .title { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
          .desc { font-size: 14px; color: #64748B; line-height: 1.5; margin-bottom: 20px; }
          .terms { font-size: 13px; color: #475569; margin-bottom: 20px; display: flex; align-items: flex-start; gap: 8px; }
          .btn { background: #0096E6; color: white; border: none; border-radius: 4px; padding: 12px; font-size: 15px; font-weight: bold; width: 100%; cursor: pointer; text-align: center; text-decoration: none; display: block; box-sizing: border-box; }
          .btn:hover { background: #0082c7; }
          .links { margin-top: 18px; text-align: center; font-size: 13px; }
          .links a { color: #0096E6; text-decoration: none; }
        </style>
      </head>
      <body>
        <div class="card">
          <div class="logo">📶 MyPublicWiFi</div>
          <div class="title">$title</div>
          <div class="desc">$welcome</div>
          ${if (requireTerms) """
          <label class="terms">
            <input type="checkbox" checked required>
            <span>I accept the Terms of Service & Acceptable Use Policy.</span>
          </label>
          """ else ""}
          <a class="btn" href="/connect">Connect to Internet</a>
          <div class="links">
            <a href="/files">📁 Browse Shared Files</a>
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()

    sendHtmlResponse(out, 200, "OK", html)
  }

  private fun handlePortalConnect(out: OutputStream) {
    val html = """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Connected - MyPublicWiFi</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #F1F5F9; color: #1E293B; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 90vh; }
          .card { background: white; border-radius: 8px; padding: 28px; width: 100%; max-width: 420px; border-top: 5px solid #22C55E; text-align: center; }
          .icon { font-size: 40px; margin-bottom: 12px; }
          .title { font-size: 20px; font-weight: bold; color: #15803D; margin-bottom: 8px; }
          .desc { font-size: 14px; color: #64748B; margin-bottom: 20px; }
          .btn { background: #0096E6; color: white; border: none; border-radius: 4px; padding: 10px 20px; font-size: 14px; font-weight: bold; text-decoration: none; display: inline-block; }
        </style>
      </head>
      <body>
        <div class="card">
          <div class="icon">✅</div>
          <div class="title">Connection Approved</div>
          <div class="desc">You are now successfully connected to the MyPublicWiFi high-speed network.</div>
          <a class="btn" href="https://www.google.com">Start Browsing</a>
        </div>
      </body>
      </html>
    """.trimIndent()

    sendHtmlResponse(out, 200, "OK", html)
  }

  private fun handleFileSharing(path: String, out: OutputStream) {
    val sharedDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "shared_files")
    if (!sharedDir.exists()) sharedDir.mkdirs()

    // Create a sample welcome file if empty
    val readme = File(sharedDir, "Welcome_MyPublicWiFi.txt")
    if (!readme.exists()) {
      readme.writeText("Welcome to MyPublicWiFi Shared Folder!\nConnected clients can view and download files here.")
    }

    if (path.startsWith("/files/download/")) {
      val filename = try {
        URLDecoder.decode(path.removePrefix("/files/download/"), "UTF-8")
      } catch (_: Exception) {
        ""
      }
      // Only serve regular files that live directly inside the shared folder (blocks ../ traversal)
      val targetFile = File(sharedDir, filename)
      val isInsideSharedDir = filename.isNotBlank() &&
          targetFile.canonicalFile.parentFile == sharedDir.canonicalFile
      if (isInsideSharedDir && targetFile.exists() && targetFile.isFile) {
        val bytes = targetFile.readBytes()
        val safeName = targetFile.name.replace("\"", "")
        val header = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "Content-Disposition: attachment; filename=\"$safeName\"\r\n" +
            "Content-Length: ${bytes.size}\r\n" +
            "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
        return
      }
    }

    // List files
    val fileList = sharedDir.listFiles()?.filter { it.isFile } ?: emptyList()
    val rows = fileList.joinToString("\n") { file ->
      val sizeKb = file.length() / 1024
      """
      <tr>
        <td style="padding: 8px 12px; border-bottom: 1px solid #E2E8F0;">📄 ${escapeHtml(file.name)}</td>
        <td style="padding: 8px 12px; border-bottom: 1px solid #E2E8F0; color: #64748B;">${sizeKb} KB</td>
        <td style="padding: 8px 12px; border-bottom: 1px solid #E2E8F0; text-align: right;">
          <a href="/files/download/${URLEncoder.encode(file.name, "UTF-8").replace("+", "%20")}" style="color: #0096E6; font-weight: bold; text-decoration: none;">Download</a>
        </td>
      </tr>
      """.trimIndent()
    }

    val html = """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Shared Files - MyPublicWiFi</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #F1F5F9; color: #1E293B; margin: 0; padding: 20px; }
          .container { background: white; border-radius: 8px; padding: 24px; max-width: 640px; margin: 0 auto; box-shadow: 0 4px 12px rgba(0,0,0,0.06); border-top: 4px solid #0096E6; }
          .header { font-size: 20px; font-weight: bold; color: #0096E6; margin-bottom: 4px; }
          .sub { font-size: 13px; color: #64748B; margin-bottom: 16px; }
          table { width: 100%; border-collapse: collapse; font-size: 14px; margin-bottom: 16px; }
          th { text-align: left; padding: 8px 12px; background: #F8FAFC; border-bottom: 2px solid #CBD5E1; }
          .back { display: inline-block; font-size: 13px; color: #0096E6; text-decoration: none; margin-top: 8px; }
        </style>
      </head>
      <body>
        <div class="container">
          <div class="header">📁 ${escapeHtml(fileSharingSettings.shareName.ifBlank { "Local Hotspot File Server" })}</div>
          <div class="sub">Download shared documents directly over local Wi-Fi without internet.</div>
          <table>
            <thead>
              <tr><th>Filename</th><th>Size</th><th style="text-align: right;">Action</th></tr>
            </thead>
            <tbody>
              $rows
            </tbody>
          </table>
          <a class="back" href="/">← Return to Hotspot Home</a>
        </div>
      </body>
      </html>
    """.trimIndent()

    sendHtmlResponse(out, 200, "OK", html)
  }

  private fun handleDefaultResponse(path: String, out: OutputStream) {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><meta charset="utf-8"><title>MyPublicWiFi</title></head>
      <body style="font-family: sans-serif; padding: 30px; text-align: center;">
        <h2>MyPublicWiFi Local Gateway</h2>
        <p>Status: Active & Broadcasting</p>
        <p><a href="/">Captive Portal</a> | <a href="/files">Shared Files</a></p>
      </body>
      </html>
    """.trimIndent()
    sendHtmlResponse(out, 200, "OK", html)
  }

  private fun escapeHtml(text: String): String {
    return text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;")
  }

  private fun sendHtmlResponse(out: OutputStream, code: Int, status: String, html: String) {
    val bytes = html.toByteArray(Charsets.UTF_8)
    val header = "HTTP/1.1 $code $status\r\n" +
        "Content-Type: text/html; charset=UTF-8\r\n" +
        "Content-Length: ${bytes.size}\r\n" +
        "Connection: close\r\n\r\n"
    out.write(header.toByteArray(Charsets.UTF_8))
    out.write(bytes)
    out.flush()
  }
}
