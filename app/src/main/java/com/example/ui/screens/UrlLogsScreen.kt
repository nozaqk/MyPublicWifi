package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavDestination
import com.example.model.SettingsSection
import com.example.ui.theme.BrandBlue
import com.example.viewmodel.HotspotViewModel

@Composable
fun UrlLogsScreen(
  viewModel: HotspotViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val urlLogs by viewModel.urlLogs.collectAsState()
  val searchQuery by viewModel.urlSearchQuery.collectAsState()
  val selectedLogId by viewModel.selectedLogId.collectAsState()

  var selectedTab by remember { mutableStateOf(1) } // 0: Settings, 1: URL Logs
  var pageSelected by remember { mutableStateOf("1") }
  var showPageMenu by remember { mutableStateOf(false) }

  val filteredLogs = remember(urlLogs, searchQuery) {
    if (searchQuery.isBlank()) urlLogs
    else urlLogs.filter {
      it.url.contains(searchQuery, ignoreCase = true) ||
          it.deviceName.contains(searchQuery, ignoreCase = true) ||
          it.mac.contains(searchQuery, ignoreCase = true)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(12.dp)
      .testTag("url_logs_screen")
  ) {
    // Header title matching Screenshot 4: "URL Logs"
    Text(
      text = "URL Logs",
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Top Tabs: [ Settings ]  [ URL Logs ]  plus Page dropdown and menu
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Bottom
    ) {
      // "Settings" tab
      Box(
        modifier = Modifier
          .width(90.dp)
          .height(38.dp)
          .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
          .background(if (selectedTab == 0) BrandBlue else Color.Transparent)
          .clickable {
            selectedTab = 0
            viewModel.setNavDestination(NavDestination.SETTINGS)
            viewModel.setSettingsSection(SettingsSection.SECURITY)
          },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Settings",
          fontSize = 13.sp,
          fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
          color = if (selectedTab == 0) Color.White else Color(0xFF475569)
        )
      }

      // "URL Logs" tab (selected)
      Box(
        modifier = Modifier
          .width(110.dp)
          .height(38.dp)
          .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
          .background(if (selectedTab == 1) BrandBlue else Color.Transparent)
          .clickable { selectedTab = 1 },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "URL Logs",
          fontSize = 13.sp,
          fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
          color = if (selectedTab == 1) Color.White else Color(0xFF475569)
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // Page dropdown "[ 1 v ]" matching Screenshot 4
      Box {
        Row(
          modifier = Modifier
            .width(60.dp)
            .height(32.dp)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
            .clickable { showPageMenu = true }
            .padding(horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = pageSelected, fontSize = 12.sp, modifier = Modifier.weight(1f))
          Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        }

        DropdownMenu(
          expanded = showPageMenu,
          onDismissRequest = { showPageMenu = false }
        ) {
          listOf("1", "2", "3", "All").forEach { p ->
            DropdownMenuItem(
              text = { Text(p) },
              onClick = {
                pageSelected = p
                showPageMenu = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      IconButton(
        onClick = {
          Toast.makeText(context, "Log filtering and sorting options", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Options",
          tint = Color(0xFF475569)
        )
      }
    }

    HorizontalDivider(color = BrandBlue, thickness = 2.dp)

    Spacer(modifier = Modifier.height(8.dp))

    // Search and quick action row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setUrlSearchQuery(it) },
        placeholder = { Text("Search domain/URL or device...", fontSize = 12.sp) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.size(16.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setUrlSearchQuery("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                modifier = Modifier.size(16.dp)
              )
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("url_search_field")
      )

      IconButton(
        onClick = { viewModel.clearUrlLogs() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Clear Logs",
          tint = Color(0xFFE53935)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // URL Table Container matching Screenshot 4
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      shape = RoundedCornerShape(2.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
      val horizontalScroll = rememberScrollState()

      Column(modifier = Modifier.fillMaxSize()) {
        // Table Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9))
            .horizontalScroll(horizontalScroll)
            .padding(vertical = 8.dp, horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Spacer(modifier = Modifier.width(28.dp)) // Icon column
          Text(
            text = "Access time",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.width(135.dp)
          )
          Text(
            text = "User",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.width(60.dp)
          )
          Text(
            text = "Device name",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.width(125.dp)
          )
          Text(
            text = "MAC",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.width(135.dp)
          )
          Text(
            text = "URL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.width(220.dp)
          )
        }

        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

        // Table Rows
        if (filteredLogs.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No URL activity recorded yet. Start the Hotspot to capture client web visits.",
              fontSize = 13.sp,
              color = Color(0xFF64748B)
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .horizontalScroll(horizontalScroll)
          ) {
            itemsIndexed(filteredLogs) { index, item ->
              val isSelected = item.id == selectedLogId
              val rowBackground = when {
                isSelected -> Color(0xFFD6EEFB) // Exact blue highlight from Screenshot 4!
                index % 2 == 1 -> Color(0xFFF8FAFC)
                else -> Color.White
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(rowBackground)
                  .clickable { viewModel.selectLogEntry(item.id) }
                  .padding(vertical = 7.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // User silhouette icon matching Screenshot 4
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color(0xFF334155),
                  modifier = Modifier
                    .width(28.dp)
                    .size(16.dp)
                )

                Text(
                  text = item.accessTime,
                  fontSize = 11.sp,
                  color = Color(0xFF1E293B),
                  modifier = Modifier.width(135.dp)
                )

                Text(
                  text = item.user,
                  fontSize = 11.sp,
                  color = Color(0xFF1E293B),
                  modifier = Modifier.width(60.dp)
                )

                Text(
                  text = item.deviceName,
                  fontSize = 11.sp,
                  color = Color(0xFF1E293B),
                  modifier = Modifier.width(125.dp)
                )

                Text(
                  text = item.mac,
                  fontSize = 11.sp,
                  color = Color(0xFF1E293B),
                  modifier = Modifier.width(135.dp)
                )

                Text(
                  text = item.url,
                  fontSize = 11.sp,
                  color = Color(0xFF1E293B),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.width(220.dp)
                )
              }

              HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
            }
          }
        }
      }
    }
  }
}
