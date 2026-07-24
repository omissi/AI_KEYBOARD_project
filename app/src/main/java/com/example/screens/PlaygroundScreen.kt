package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.KeyboardViewModel

@Composable
fun PlaygroundScreen(viewModel: KeyboardViewModel, onNavigateToSettings: () -> Unit, onNavigateToSetup: () -> Unit) {
    val typedText by viewModel.typedText.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isPredictiveMode by viewModel.isPredictiveMode.collectAsState()
    val isShiftActive by viewModel.isShiftActive.collectAsState()
    val selectedFontState by viewModel.selectedFontFamily.collectAsState()
    val undoBanner by viewModel.showUndoBanner.collectAsState()
    val suggestedEmoji by viewModel.suggestedEmoji.collectAsState()

    val fontFamily = when (selectedFontState) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Elegant" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("منطقة تجربة الكتابة", fontWeight = FontWeight.Bold, fontFamily = fontFamily, fontSize = 14.sp)
                    Row {
                        IconButton(onClick = onNavigateToSetup) { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        IconButton(onClick = { viewModel.clearAllText() }) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = typedText.ifBlank { "ابدأ الكتابة هنا..." },
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                    color = if (typedText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                AnimatedVisibility(visible = undoBanner != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(undoBanner ?: "", fontSize = 11.sp, fontFamily = fontFamily, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.undoLastShortcutExpansion() }) { Text("تراجع") }
                    }
                }

                if (suggestedEmoji != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("مقترح: $suggestedEmoji", fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (suggestions.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                items(suggestions) { word ->
                    AssistChip(onClick = { viewModel.selectSuggestion(word) }, label = { Text(word, fontFamily = fontFamily) })
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = isPredictiveMode, onClick = { viewModel.togglePredictiveMode() }, label = { Text(if (isPredictiveMode) "تنبؤي T9" else "ضغط متكرر", fontFamily = fontFamily, fontSize = 11.sp) })
            FilterChip(selected = currentLanguage == "AR", onClick = { viewModel.toggleLanguage() }, label = { Text(currentLanguage, fontFamily = fontFamily, fontSize = 11.sp) })
            FilterChip(selected = isShiftActive, onClick = { viewModel.toggleShift() }, label = { Text("Shift", fontFamily = fontFamily, fontSize = 11.sp) })
        }

        Spacer(modifier = Modifier.height(10.dp))
        NumericKeypad(viewModel = viewModel, fontFamily = fontFamily)
    }
}

@Composable
fun NumericKeypad(viewModel: KeyboardViewModel, fontFamily: FontFamily) {
    val rows = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { digit ->
                    Box(
                        modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.onKeyPress(digit) }
                            .testTag("key_$digit"),
                        contentAlignment = Alignment.Center
                    ) { Text(digit.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = fontFamily) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { viewModel.onKeyPress('\b') },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null) }

            Box(
                modifier = Modifier.weight(2f).height(52.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { viewModel.onKeyPress(' ') },
                contentAlignment = Alignment.Center
            ) { Text("مسافة", fontFamily = fontFamily, fontWeight = FontWeight.Bold) }

            Box(
                modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { viewModel.onKeyPress('\n') },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = null, tint = Color.White) }
        }
    }
}
