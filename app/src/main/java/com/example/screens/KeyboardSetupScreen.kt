package com.example.screens

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.KeyboardViewModel

fun isKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.enabledInputMethodList.any { it.packageName == context.packageName }
}

fun isKeyboardSelected(context: Context): Boolean {
    val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    return defaultIme?.startsWith(context.packageName) == true
}

@Composable
fun KeyboardSetupScreen(viewModel: KeyboardViewModel, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("تفعيل AI KEYBOARD", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "لتتمكن من استخدام لوحة المفاتيح الذكية، يرجى تفعيلها في إعدادات النظام ثم اختيارها كلوحة مفاتيح افتراضية.",
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { context.startActivity(android.content.Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("1. تفعيل لوحة المفاتيح") }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("2. اختيار AI KEYBOARD") }

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBack) { Text("تخطي والاستمرار") }
    }
}
