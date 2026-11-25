package com.example.util

import android.net.TrafficStats
import com.example.model.TrafficHistoryPoint

data class TrafficSnapshot(
  val downloadSpeedMbit: Float,
  val uploadSpeedMbit: Float,
  val totalSessionRxMb: Float,
  val totalSessionTxMb: Float
)

class RealTrafficMonitor {

  private var lastRxBytes: Long = 0L
  private var lastTxBytes: Long = 0L
  private var lastSampleTimeMs: Long = 0L

  private var sessionStartRxBytes: Long = 0L
  private var sessionStartTxBytes: Long = 0L

  fun startSession() {
    val rx = TrafficStats.getTotalRxBytes()
    val tx = TrafficStats.getTotalTxBytes()

    lastRxBytes = if (rx != TrafficStats.UNSUPPORTED.toLong()) rx else 0L
    lastTxBytes = if (tx != TrafficStats.UNSUPPORTED.toLong()) tx else 0L
    sessionStartRxBytes = lastRxBytes
    sessionStartTxBytes = lastTxBytes
    lastSampleTimeMs = System.currentTimeMillis()
  }

  fun sample(): TrafficSnapshot {
    val now = System.currentTimeMillis()
    val timeDeltaSec = (now - lastSampleTimeMs) / 1000f
    lastSampleTimeMs = now

    val currentRx = TrafficStats.getTotalRxBytes()
    val currentTx = TrafficStats.getTotalTxBytes()

    if (currentRx == TrafficStats.UNSUPPORTED.toLong() || currentTx == TrafficStats.UNSUPPORTED.toLong()) {
      return TrafficSnapshot(0f, 0f, 0f, 0f)
    }

    val deltaRxBytes = if (lastRxBytes > 0 && currentRx >= lastRxBytes) currentRx - lastRxBytes else 0L
    val deltaTxBytes = if (lastTxBytes > 0 && currentTx >= lastTxBytes) currentTx - lastTxBytes else 0L

    lastRxBytes = currentRx
    lastTxBytes = currentTx

    val effectiveTime = if (timeDeltaSec > 0.1f) timeDeltaSec else 1f

    // (Bytes * 8 bits) / (1_000_000 bits/Mbit) / seconds
    val downMbit = ((deltaRxBytes * 8f) / (1024f * 1024f)) / effectiveTime
    val upMbit = ((deltaTxBytes * 8f) / (1024f * 1024f)) / effectiveTime

    val sessionRxMb = ((currentRx - sessionStartRxBytes).coerceAtLeast(0L) / (1024f * 1024f))
    val sessionTxMb = ((currentTx - sessionStartTxBytes).coerceAtLeast(0L) / (1024f * 1024f))

    return TrafficSnapshot(
      downloadSpeedMbit = downMbit,
      uploadSpeedMbit = upMbit,
      totalSessionRxMb = sessionRxMb,
      totalSessionTxMb = sessionTxMb
    )
  }
}
