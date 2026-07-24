package com.example.viewmodel

import androidx.compose.runtime.Immutable

@Immutable
data class ClipboardEntry(val text: String, val timestamp: Long = System.currentTimeMillis())

data class TranslationResult(
    val originalText: String = "",
    val translatedText: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

enum class OneHandedMode { OFF, LEFT, RIGHT }
enum class KeyboardMode { T9_PREDICTIVE, MULTI_TAP, QWERTY }
enum class ShiftState { OFF, ONCE, CAPS_LOCK }

interface InputDelegate {
    fun commitText(text: CharSequence)
    fun deleteSurroundingText(beforeLength: Int, afterLength: Int)
}
