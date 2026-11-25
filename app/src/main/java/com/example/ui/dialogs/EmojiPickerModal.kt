package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrandBlue

@Composable
fun EmojiPickerModal(
  onEmojiSelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) }
  var selectedSkinTone by remember { mutableStateOf(0) }

  val skinTones = listOf("🟡", "🏻", "🏼", "🏽", "🏾", "🏿")

  val smileysList = listOf(
    "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
    "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙",
    "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
    "🤐", "🤨", "😐", "😑", "😶", "😶‍🌫️", "😏", "😒", "🙄", "😬",
    "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢",
    "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸",
    "😎", "🤓", "🧐", "😕", "😟", "🙁", "☹️", "😮", "😯", "😲",
    "😳", "🥺", "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱",
    "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠",
    "😈", "👿", "💀", "💩", "🤡", "👹", "👻", "👽", "🤖", "🔥"
  )

  val textEmojisList = listOf(
    ":-)", ";-)", ":-D", "xD", "<3", "(^-^)", "(o_O)",
    "\\(^o^)/", "(♥_♥)", "(─‿‿─)", "(•‿•)", "¯\\_(ツ)_/¯",
    "(ง'̀-'́)ง", "(¬‿¬)", "(T_T)", "(*_*)", "(≧◡≦)", "(ʘ‿ʘ)",
    "ʕ•ᴥ•ʔ", "(/◕ヮ◕)/", "(¬_¬)", "(ಥ_ಥ)", "(◕‿◕✿)", "(✿◠‿◠)"
  )

  val symbolsList = listOf(
    "♡", "♥", "★", "☆", "⚡", "✈", "☁", "☀", "♫", "♪",
    "❄", "☕", "⚓", "✿", "❀", "✓", "✔", "✕", "✖", "✦",
    "✨", "💎", "📶", "📡", "💻", "📱", "🔒", "🔑", "🚀", "🛸",
    "🌐", "🌍", "🌎", "🌏", "⭐", "🌟", "⚡", "💡", "🔔", "🎯"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .height(440.dp)
        .testTag("emoji_picker_dialog"),
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
      ) {
        // Modal Header matching Screenshot 5
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Emoji - Weiter nach einem Emoji suchen",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF64748B)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Tabs: Smileys, Text Emojis, Symbols
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          TabButton(
            label = "😊",
            isSelected = selectedTab == 0,
            onClick = { selectedTab = 0 }
          )
          TabButton(
            label = ";-)",
            isSelected = selectedTab == 1,
            onClick = { selectedTab = 1 }
          )
          TabButton(
            label = "Ω",
            isSelected = selectedTab == 2,
            onClick = { selectedTab = 2 }
          )
        }

        HorizontalDivider(
          color = Color(0xFFE2E8F0),
          thickness = 1.dp,
          modifier = Modifier.padding(vertical = 8.dp)
        )

        // Skin Tone Selector (matching Screenshot 5)
        if (selectedTab == 0) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            skinTones.forEachIndexed { index, tone ->
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (selectedSkinTone == index) BrandBlue.copy(alpha = 0.2f)
                    else Color.Transparent
                  )
                  .clickable { selectedSkinTone = index },
                contentAlignment = Alignment.Center
              ) {
                Text(text = tone, fontSize = 16.sp)
              }
            }
          }
        }

        // Emoji Grid
        val currentItems = when (selectedTab) {
          0 -> smileysList
          1 -> textEmojisList
          else -> symbolsList
        }

        LazyVerticalGrid(
          columns = if (selectedTab == 1) GridCells.Fixed(3) else GridCells.Fixed(7),
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(currentItems) { item ->
            Box(
              modifier = Modifier
                .height(if (selectedTab == 1) 36.dp else 40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF8FAFC))
                .clickable {
                  onEmojiSelected(item)
                  onDismiss()
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = item,
                fontSize = if (selectedTab == 1) 13.sp else 22.sp,
                color = Color.Black
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TabButton(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Text(
      text = label,
      fontSize = 18.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) BrandBlue else Color(0xFF64748B),
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
    if (isSelected) {
      Box(
        modifier = Modifier
          .width(28.dp)
          .height(3.dp)
          .background(BrandBlue, RoundedCornerShape(2.dp))
      )
    }
  }
}
