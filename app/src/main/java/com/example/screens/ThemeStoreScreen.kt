package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.KeyboardViewModel

@Composable
fun ThemeStoreScreen(viewModel: KeyboardViewModel) {
    val allThemes by viewModel.allThemes.collectAsState()
    val activeTheme by viewModel.activeTheme.collectAsState()
    val selectedFontState by viewModel.selectedFontFamily.collectAsState()

    var newThemeName by remember { mutableStateOf("ثيمي المخصص") }
    var selectedBgColor by remember { mutableStateOf("#11052C") }
    var selectedKeyBgColor by remember { mutableStateOf("#2D0E5E") }
    var selectedKeyTextColor by remember { mutableStateOf("#FFFFFF") }
    var selectedAccentColor by remember { mutableStateOf("#3B82F6") }
    var selectedCorners by remember { mutableStateOf(8f) }

    val colorPalette = listOf(
        "#121214" to "أسود مائل للرمادي", "#1C1C24" to "رمادي متوسط", "#0A0518" to "أزرق ليلي عميق",
        "#FFF0F3" to "أبيض وردي", "#FFFFFF" to "أبيض نقي", "#2E7D32" to "أخضر غابة",
        "#1565C0" to "أزرق محيطي", "#8E24AA" to "بنفسجي أوركيد", "#000000" to "أسود Amoled",
        "#3B82F6" to "أزرق العلامة", "#2ECC71" to "أخضر نعناعي"
    )

    val fontFamily = when (selectedFontState) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Elegant" -> FontFamily.Cursive
        else -> FontFamily.SansSerif
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)))
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(text = "متجر وتخصيص ثيمات AI KEYBOARD", color = Color.White, fontSize = 17.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "تصفح الثيمات الجاهزة أو صمم لوحة مفاتيح فريدة تناسب ذوقك تمامًا.", color = Color.White.copy(alpha = 0.85f), fontFamily = fontFamily, fontSize = 11.sp)
                    }
                }
            }
        }

        item { Text(text = "الثيمات المتاحة والمنشأة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = fontFamily, modifier = Modifier.padding(bottom = 4.dp)) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(allThemes, key = { it.id }) { theme ->
                    val isActive = activeTheme?.id == theme.id
                    val themeBg = Color(android.graphics.Color.parseColor(theme.bgColor))
                    val themeKeyBg = Color(android.graphics.Color.parseColor(theme.keyBgColor))
                    val themeKeyText = Color(android.graphics.Color.parseColor(theme.keyTextColor))
                    val themeAccent = Color(android.graphics.Color.parseColor(theme.accentColor))

                    Card(
                        modifier = Modifier.width(170.dp).clickable { viewModel.setActiveTheme(theme) }
                            .border(width = if (isActive) 2.5.dp else 1.dp, color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = themeBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = theme.name, color = themeKeyText, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily, maxLines = 1)
                                if (theme.isSystem) {
                                    Badge(containerColor = themeAccent, contentColor = Color.White) { Text("رسمي", fontSize = 8.sp, fontFamily = fontFamily) }
                                } else {
                                    IconButton(onClick = { viewModel.deleteTheme(theme) }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الثيم", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(4.dp)).background(themeBg).padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(theme.borderRadius.dp / 3)).background(themeKeyBg),
                                        contentAlignment = Alignment.Center
                                    ) { Text("A", color = themeKeyText, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                }
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(theme.borderRadius.dp / 3)).background(themeAccent),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp)) }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(themeBg).border(0.5.dp, Color.Gray, CircleShape))
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(themeKeyBg).border(0.5.dp, Color.Gray, CircleShape))
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(themeAccent).border(0.5.dp, Color.Gray, CircleShape))
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "${theme.borderRadius}dp", color = themeKeyText.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = fontFamily)
                            }
                        }
                    }
                }
            }
        }

        item { Text(text = "صمم ثيمك الخاص", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = fontFamily, modifier = Modifier.padding(top = 8.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(value = newThemeName, onValueChange = { newThemeName = it }, label = { Text("اسم الثيم الجديد", fontFamily = fontFamily) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Text("معاينة حية للتصميم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = fontFamily)

                    Box(
                        modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color(android.graphics.Color.parseColor(selectedBgColor)))
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Text("تنبؤ ذكي", color = Color(android.graphics.Color.parseColor(selectedAccentColor)), fontSize = 11.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("AI KEYBOARD", color = Color(android.graphics.Color.parseColor(selectedKeyTextColor)).copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = fontFamily)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(selectedCorners.dp)).background(Color(android.graphics.Color.parseColor(selectedKeyBgColor))),
                                        contentAlignment = Alignment.Center
                                    ) { Text("5", color = Color(android.graphics.Color.parseColor(selectedKeyTextColor)), fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = fontFamily) }
                                }
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(selectedCorners.dp)).background(Color(android.graphics.Color.parseColor(selectedAccentColor))),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(34.dp)) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(selectedCorners.dp)).background(Color(android.graphics.Color.parseColor(selectedKeyBgColor))),
                                    contentAlignment = Alignment.Center
                                ) { Text("🌐", fontSize = 12.sp) }
                                Box(
                                    modifier = Modifier.weight(2f).fillMaxHeight().clip(RoundedCornerShape(selectedCorners.dp)).background(Color(android.graphics.Color.parseColor(selectedKeyBgColor))),
                                    contentAlignment = Alignment.Center
                                ) { Text("مسافة", color = Color(android.graphics.Color.parseColor(selectedAccentColor)), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily) }
                            }
                        }
                    }

                    Divider()

                    ColorRowSelector("لون الخلفية", selectedBgColor, colorPalette, fontFamily) { selectedBgColor = it }
                    ColorRowSelector("لون الأزرار", selectedKeyBgColor, colorPalette, fontFamily) { selectedKeyBgColor = it }
                    ColorRowSelector("لون حروف الأزرار", selectedKeyTextColor, colorPalette, fontFamily) { selectedKeyTextColor = it }
                    ColorRowSelector("لون التمييز (Accent)", selectedAccentColor, colorPalette, fontFamily) { selectedAccentColor = it }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("استدارة زوايا الأزرار", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                            Text("${selectedCorners.toInt()}dp", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(value = selectedCorners, onValueChange = { selectedCorners = it }, valueRange = 0f..24f, steps = 5)
                    }

                    Button(
                        onClick = {
                            if (newThemeName.isNotBlank()) {
                                viewModel.addCustomTheme(name = newThemeName, bgColor = selectedBgColor, keyBg = selectedKeyBgColor, keyText = selectedKeyTextColor, accent = selectedAccentColor, corners = selectedCorners.toInt())
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_theme_button")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ وتطبيق الثيم المخصص", fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
fun ColorRowSelector(label: String, selectedHex: String, palette: List<Pair<String, String>>, fontFamily: FontFamily, onHexSelected: (String) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(selectedHex))).border(0.5.dp, Color.Gray, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(selectedHex, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            items(palette, key = { it.first }) { (hex, _) ->
                val isSel = selectedHex.lowercase() == hex.lowercase()
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(hex)))
                        .border(width = if (isSel) 2.5.dp else 0.5.dp, color = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray, shape = CircleShape)
                        .clickable { onHexSelected(hex) }
                )
            }
        }
    }
}
