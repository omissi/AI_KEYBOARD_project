package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.KeyboardViewModel

@Composable
fun SettingsScreen(viewModel: KeyboardViewModel, onNavigateToSetup: () -> Unit) {
    val context = LocalContext.current
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val isSuggestionsEnabled by viewModel.isSuggestionsEnabled.collectAsState()
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var shortcutAbbrev by remember { mutableStateOf("") }
    var shortcutFull by remember { mutableStateOf("") }
    val shortcuts by viewModel.allShortcuts.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("إعدادات وأدوات AI KEYBOARD", fontWeight = FontWeight.Bold, fontSize = 18.sp) }

        item {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("الاهتزاز عند الضغط")
                        Switch(checked = isHapticEnabled, onCheckedChange = { viewModel.toggleHapticFeedback() })
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("تفعيل الاقتراحات")
                        Switch(checked = isSuggestionsEnabled, onCheckedChange = { viewModel.toggleSuggestionsEnabled() })
                    }
                }
            }
        }

        item {
            Button(onClick = onNavigateToSetup, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Keyboard, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إعادة تفعيل AI KEYBOARD")
            }
        }

        item {
            Button(onClick = { viewModel.exportLocalBackup(context) { result -> backupMessage = result } }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("نسخ احتياطي محلي للإعدادات")
            }
        }
        if (backupMessage != null) { item { Text(backupMessage ?: "", fontSize = 11.sp) } }

        item {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("الاختصارات النصية", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = shortcutAbbrev, onValueChange = { shortcutAbbrev = it }, label = { Text("اختصار") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = shortcutFull, onValueChange = { shortcutFull = it }, label = { Text("النص الكامل") }, modifier = Modifier.weight(1.5f), singleLine = true)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (shortcutAbbrev.isNotBlank() && shortcutFull.isNotBlank()) {
                                viewModel.addShortcut(shortcutAbbrev, shortcutFull)
                                shortcutAbbrev = ""; shortcutFull = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("إضافة اختصار") }

                    shortcuts.forEach { item ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column {
                                Text(item.shortcut, fontWeight = FontWeight.Bold)
                                Text(item.expandedText, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.deleteShortcut(item.id) }) { Icon(Icons.Default.Delete, contentDescription = null) }
                        }
                    }
                }
            }
        }
    }
}
