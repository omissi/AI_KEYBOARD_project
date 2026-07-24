package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.KeyboardViewModel
import com.example.viewmodel.OneHandedMode

/**
 * الواجهة الفعلية لكيبورد AI KEYBOARD كما تظهر فوق التطبيقات الأخرى (InputMethodService).
 * تدعم وضع اليد الواحدة، شريط التراجع عن الاستبدال التلقائي، ومقترحات الإيموجي الذكية.
 */
@Composable
fun KeyboardView(viewModel: KeyboardViewModel) {
    val suggestions by viewModel.suggestions.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isShiftActive by viewModel.isShiftActive.collectAsState()
    val undoBanner by viewModel.showUndoBanner.collectAsState()
    val suggestedEmoji by viewModel.suggestedEmoji.collectAsState()
    val oneHandedMode by viewModel.oneHandedMode.collectAsState()
    val activeTheme by viewModel.activeTheme.collectAsState()

    val bgColor = activeTheme?.let { Color(android.graphics.Color.parseColor(it.bgColor)) } ?: Color(0xFF0F172A)
    val keyBgColor = activeTheme?.let { Color(android.graphics.Color.parseColor(it.keyBgColor)) } ?: Color(0xFF1E293B)
    val keyTextColor = activeTheme?.let { Color(android.graphics.Color.parseColor(it.keyTextColor)) } ?: Color.White
    val accentColor = activeTheme?.let { Color(android.graphics.Color.parseColor(it.accentColor)) } ?: Color(0xFF3B82F6)

    val horizontalPadding = when (oneHandedMode) {
        OneHandedMode.LEFT -> PaddingValues(start = 4.dp, end = 80.dp)
        OneHandedMode.RIGHT -> PaddingValues(start = 80.dp, end = 4.dp)
        OneHandedMode.OFF -> PaddingValues(horizontal = 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontalPadding).padding(vertical = 6.dp)
    ) {
        undoBanner?.let { message ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(accentColor.copy(alpha = 0.15f)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message, color = keyTextColor, fontSize = 11.sp)
                Text(
                    "تراجع", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.clickable { viewModel.undoLastShortcutExpansion() }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        val suggestionsScrollState = rememberScrollState()
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(suggestionsScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            suggestions.forEach { word ->
                Text(
                    text = word, color = keyTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(keyBgColor)
                        .clickable { viewModel.selectSuggestion(word) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            suggestedEmoji?.let { emoji ->
                Text(
                    text = emoji, fontSize = 16.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .clickable { viewModel.appendTypedTextDirectly(emoji) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val numRows = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9')
        )
        numRows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    KeyButton(label = key.toString(), modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor) {
                        viewModel.onKeyPress(key)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton(icon = Icons.Default.ArrowUpward, modifier = Modifier.weight(1f), keyBgColor = if (isShiftActive) accentColor else keyBgColor, keyTextColor = keyTextColor) {
                viewModel.toggleShift()
            }
            KeyButton(label = "0", modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor) {
                viewModel.onKeyPress('0')
            }
            KeyButton(icon = Icons.Filled.Backspace, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = keyTextColor) {
                viewModel.onKeyPress('\b')
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton(label = currentLanguage, modifier = Modifier.weight(1f), keyBgColor = keyBgColor, keyTextColor = accentColor) {
                viewModel.toggleLanguage()
            }
            KeyButton(label = "مسافة", modifier = Modifier.weight(2.5f), keyBgColor = keyBgColor, keyTextColor = keyTextColor) {
                viewModel.onKeyPress(' ')
            }
            KeyButton(icon = Icons.Filled.KeyboardReturn, modifier = Modifier.weight(1f), keyBgColor = accentColor, keyTextColor = Color.White) {
                viewModel.onKeyPress('\n')
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    keyBgColor: Color,
    keyTextColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(keyBgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = keyTextColor, modifier = Modifier.size(18.dp))
        } else if (label != null) {
            Text(label, color = keyTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
