package com.example.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.KeyboardViewModel

@Composable
fun AnalyticsScreen(viewModel: KeyboardViewModel) {
    val totalWords by viewModel.totalWordsTyped.collectAsState()
    val totalBackspaces by viewModel.totalBackspaces.collectAsState()
    val avgWpm by viewModel.averageWpm.collectAsState()
    val hasData by viewModel.hasEnoughDataForStats.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("تقارير الإنتاجية - AI KEYBOARD", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp)) }

        if (!hasData) {
            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("لا توجد بيانات كافية بعد. ابدأ الكتابة على AI KEYBOARD لعرض إحصاءاتك هنا.", modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiCard(title = "كلمات", value = totalWords.toString(), icon = Icons.Default.TextFields, modifier = Modifier.weight(1f))
                    KpiCard(title = "متوسط WPM", value = avgWpm.toString(), icon = Icons.Default.Speed, modifier = Modifier.weight(1f))
                }
            }
            item {
                KpiCard(title = "مرات الحذف", value = totalBackspaces.toString(), icon = Icons.AutoMirrored.Filled.KeyboardBackspace, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}
