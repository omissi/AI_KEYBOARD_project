package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.services.GeminiTranslationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class KeyboardViewModel(private val repository: KeyboardRepository) : ViewModel() {

    private val _typedText = MutableStateFlow("")
    val typedText: StateFlow<String> = _typedText.asStateFlow()

    private val _t9Sequence = MutableStateFlow("")
    val t9Sequence: StateFlow<String> = _t9Sequence.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _currentLanguage = MutableStateFlow("EN")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isPredictiveMode = MutableStateFlow(false)
    val isPredictiveMode: StateFlow<Boolean> = _isPredictiveMode.asStateFlow()

    private val _keyboardMode = MutableStateFlow(KeyboardMode.MULTI_TAP)
    val keyboardMode: StateFlow<KeyboardMode> = _keyboardMode.asStateFlow()

    private val _isSuggestionsEnabled = MutableStateFlow(true)
    val isSuggestionsEnabled: StateFlow<Boolean> = _isSuggestionsEnabled.asStateFlow()

    fun toggleSuggestionsEnabled() { _isSuggestionsEnabled.value = !_isSuggestionsEnabled.value; updateSuggestions() }

    private val _shiftState = MutableStateFlow(ShiftState.OFF)
    val shiftState: StateFlow<ShiftState> = _shiftState.asStateFlow()
    val isShiftActive: StateFlow<Boolean> = _shiftState.map { it != ShiftState.OFF }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val suggestedEmoji = MutableStateFlow<String?>(null)
    val suggestedEmoji: StateFlow<String?> = suggestedEmoji.asStateFlow()

    private val _activePreviewChar = MutableStateFlow<Char?>(null)
    val activePreviewChar: StateFlow<Char?> = _activePreviewChar.asStateFlow()

    private val isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = isHapticEnabled.asStateFlow()
    fun toggleHapticFeedback() { isHapticEnabled.value = !isHapticEnabled.value }

    private val oneHandedMode = MutableStateFlow(OneHandedMode.OFF)
    val oneHandedMode: StateFlow<OneHandedMode> = oneHandedMode.asStateFlow()
    fun setOneHandedMode(mode: OneHandedMode) { oneHandedMode.value = mode }

    private val clipboardHistory = MutableStateFlow<List<ClipboardEntry>>(emptyList())
    val clipboardHistory: StateFlow<List<ClipboardEntry>> = clipboardHistory.asStateFlow()

    fun addClipboardEntry(text: String) {
        if (text.isBlank()) return
        clipboardHistory.value = (listOf(ClipboardEntry(text)) + clipboardHistory.value.filter { it.text != text }).take(10)
    }
    fun clearClipboardHistory() { clipboardHistory.value = emptyList() }

    private val showUndoBanner = MutableStateFlow<String?>(null)
    val showUndoBanner: StateFlow<String?> = showUndoBanner.asStateFlow()
    private var lastShortcutSnapshot: Pair<String, String>? = null
    private var undoBannerJob: kotlinx.coroutines.Job? = null

    fun dismissUndoBanner() { undoBannerJob?.cancel(); showUndoBanner.value = null }

    fun undoLastShortcutExpansion() {
        lastShortcutSnapshot?.let { (previousText, _) -> _typedText.value = previousText }
        dismissUndoBanner()
    }

    private val translationResult = MutableStateFlow<TranslationResult?>(null)
    val translationResult: StateFlow<TranslationResult?> = translationResult.asStateFlow()
    fun clearTranslationResult() { translationResult.value = null }

    fun translateWithAI(text: String, fromLang: String, toLang: String, apiKey: String = "") {
        if (text.isBlank()) return
        translationResult.value = TranslationResult(originalText = text, isLoading = true)
        viewModelScope.launch {
            when (val result = GeminiTranslationService.translate(text, fromLang, toLang, apiKey)) {
                is GeminiTranslationService.ServiceResult.Success ->
                    translationResult.value = TranslationResult(originalText = text, translatedText = result.translatedText)
                is GeminiTranslationService.ServiceResult.Error ->
                    translationResult.value = TranslationResult(originalText = text, translatedText = result.message, isError = true)
            }
        }
    }

    fun reportAiContent(original: String, translated: String, reason: String) { /* logged locally in production build */ }

    private var lastKeyPressed: Char? = null
    private var multiTapIndex = 0
    private var lastPressTime = 0L
    private val MULTI_TAP_TIMEOUT = 800L
    private var multiTapCommitJob: kotlinx.coroutines.Job? = null

    val allThemes: StateFlow<List<CustomTheme>> = repository.allThemes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTheme = MutableStateFlow<CustomTheme?>(null)
    val activeTheme: StateFlow<CustomTheme?> = _activeTheme.asStateFlow()

    val allShortcuts: StateFlow<List<TextShortcut>> = repository.allShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStats: StateFlow<List<TypingStat>> = repository.allStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWordsTyped: StateFlow<Int> = allStats.map { s -> s.sumOf { it.wordsTyped } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBackspaces: StateFlow<Int> = allStats.map { s -> s.sumOf { it.backspacesPressed } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageWpm: StateFlow<Int> = allStats.map { stats ->
        val totalActiveSeconds = stats.sumOf { it.activeTimeSeconds }
        val totalWords = stats.sumOf { it.wordsTyped }
        if (totalActiveSeconds > 0) ((totalWords.toFloat() / totalActiveSeconds) * 60).toInt() else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hasEnoughDataForStats: StateFlow<Boolean> = allStats
        .map { it.isNotEmpty() && it.sumOf { s -> s.wordsTyped } > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allTasks: StateFlow<List<PendingTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val keyboardHeightScaleRaw = MutableStateFlow(1.0f)
    val keyboardHeightScale: StateFlow<String> = keyboardHeightScaleRaw.map { String.format("%.1fx", it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1.0x")

    private val _selectedFontFamily = MutableStateFlow("Sans-Serif")
    val selectedFontFamily: StateFlow<String> = _selectedFontFamily.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
            repository.allThemes.collect { themes ->
                if (themes.isNotEmpty() && _activeTheme.value == null) _activeTheme.value = themes.first()
            }
        }
    }

    fun setLanguage(lang: String) { commitCurrentBuffer(); _currentLanguage.value = lang.uppercase(); updateSuggestions() }
    fun toggleLanguage() { setLanguage(when (_currentLanguage.value) { "EN" -> "AR"; else -> "EN" }) }

    fun togglePredictiveMode() {
        commitCurrentBuffer()
        if (_keyboardMode.value == KeyboardMode.QWERTY) setKeyboardMode(KeyboardMode.MULTI_TAP) else setKeyboardMode(KeyboardMode.QWERTY)
    }

    fun setKeyboardMode(mode: KeyboardMode) {
        commitCurrentBuffer()
        _keyboardMode.value = mode
        _isPredictiveMode.value = mode == KeyboardMode.T9_PREDICTIVE
        updateSuggestions()
    }

    fun translateTextLocal(text: String, from: String, to: String): String {
        if (text.isBlank()) return text
        val source = from.uppercase().trim(); val target = to.uppercase().trim()
        if (source == target) return text
        val dictionaryEnAr = mapOf(
            "hello" to "مرحباً", "hi" to "أهلاً", "how are you" to "كيف حالك", "good morning" to "صباح الخير",
            "good night" to "مساء الخير", "keyboard" to "كيبورد", "smart" to "ذكي", "work" to "عمل",
            "love" to "حب", "task" to "مهمة", "theme" to "ثيم", "color" to "لون", "yes" to "نعم", "no" to "لا",
            "thanks" to "شكراً", "friend" to "صديق", "welcome" to "أهلاً بك", "happy" to "سعيد", "nice" to "جميل"
        )
        val dictionaryArEn = dictionaryEnAr.entries.associate { (k, v) -> v to k }
        val cleanText = text.trim().lowercase()
        val dict = if (source == "EN") dictionaryEnAr else dictionaryArEn
        dict[cleanText]?.let { return it }
        val words = text.split(" ")
        if (words.size > 1) {
            return words.joinToString(" ") { word -> dict[word.lowercase().filter { it.isLetter() }] ?: word }
        }
        return text
    }

    private var lastShiftPressTime = 0L
    private val DOUBLE_CLICK_TIMEOUT = 400L

    fun toggleShift() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShiftPressTime < DOUBLE_CLICK_TIMEOUT) {
            _shiftState.value = ShiftState.CAPS_LOCK
        } else {
            _shiftState.value = when (_shiftState.value) {
                ShiftState.OFF -> ShiftState.ONCE
                ShiftState.ONCE -> ShiftState.OFF
                ShiftState.CAPS_LOCK -> ShiftState.OFF
            }
        }
        lastShiftPressTime = currentTime
    }

    fun updateKeyboardHeight(scale: Float) { keyboardHeightScaleRaw.value = scale }
    fun updateFontFamily(font: String) { _selectedFontFamily.value = font }

    var inputDelegate: InputDelegate? = null

    fun appendTypedTextDirectly(text: String) {
        _typedText.value = _typedText.value + text
        inputDelegate?.commitText(text)
        if (_shiftState.value == ShiftState.ONCE && text.any { it.isLetter() }) _shiftState.value = ShiftState.OFF
    }

    fun onKeyPress(key: Char) {
        val currentTime = System.currentTimeMillis()
        val currentLang = _currentLanguage.value

        if (key in '1'..'9') {
            if (_isPredictiveMode.value) {
                _t9Sequence.value = _t9Sequence.value + key
                updateSuggestions()
            } else {
                val charList = T9Engine.getKeyMap(currentLang)[key] ?: emptyList()
                if (charList.isNotEmpty()) {
                    multiTapCommitJob?.cancel()
                    if (lastKeyPressed == key) {
                        multiTapIndex = (multiTapIndex + 1) % charList.size
                        val currentText = _typedText.value
                        if (currentText.isNotEmpty()) {
                            val newChar = maybeCapitalize(charList[multiTapIndex])
                            _typedText.value = currentText.dropLast(1) + newChar
                            inputDelegate?.deleteSurroundingText(1, 0)
                            inputDelegate?.commitText(newChar.toString())
                            _activePreviewChar.value = newChar
                        }
                    } else {
                        if (lastKeyPressed != null && _shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
                        multiTapIndex = 0
                        val newChar = maybeCapitalize(charList[0])
                        _typedText.value = _typedText.value + newChar
                        inputDelegate?.commitText(newChar.toString())
                        _activePreviewChar.value = newChar
                    }
                    lastKeyPressed = key
                    lastPressTime = currentTime
                    updateSuggestions()
                    multiTapCommitJob = viewModelScope.launch {
                        kotlinx.coroutines.delay(MULTI_TAP_TIMEOUT)
                        lastKeyPressed = null; multiTapIndex = 0; _activePreviewChar.value = null
                    }
                }
            }
        } else if (key == '0' || key == ' ') {
            handleSpaceKeyPress()
        } else if (key == '\b') {
            handleBackspace()
        } else if (key == '\n') {
            commitCurrentBuffer()
            _typedText.value = _typedText.value + "\n"
            inputDelegate?.commitText("\n")
        }
    }

    fun deleteLastWord() {
        val text = _typedText.value
        if (text.isBlank()) return
        val trimmed = text.trimEnd()
        val lastSpaceIndex = trimmed.lastIndexOf(' ')
        val deleteCount = text.length - (if (lastSpaceIndex >= 0) lastSpaceIndex + 1 else 0)
        _typedText.value = if (lastSpaceIndex >= 0) trimmed.substring(0, lastSpaceIndex + 1) else ""
        inputDelegate?.deleteSurroundingText(deleteCount, 0)
        lastKeyPressed = null
        updateSuggestions()
    }

    private fun maybeCapitalize(char: Char): Char {
        val isUpper = _shiftState.value != ShiftState.OFF
        return if (isUpper) char.uppercaseChar() else char
    }

    private fun handleSpaceKeyPress() {
        var topSuggestionCommitted = ""
        if (_isPredictiveMode.value && _t9Sequence.value.isNotEmpty()) {
            val topSuggestion = _suggestions.value.firstOrNull() ?: _t9Sequence.value
            _typedText.value = _typedText.value + topSuggestion
            topSuggestionCommitted = topSuggestion
            _t9Sequence.value = ""; _suggestions.value = emptyList()
            if (_shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
            inputDelegate?.commitText(topSuggestion)
        }

        val textBeforeSpace = _typedText.value
        val words = textBeforeSpace.split(" ")
        val lastWord = words.lastOrNull() ?: ""
        val cleanWord = lastWord.lowercase().filter { it.isLetter() }
        val shortcut = allShortcuts.value.find { it.shortcut.lowercase() == cleanWord }

        if (shortcut != null) {
            val newText = textBeforeSpace.substring(0, textBeforeSpace.length - lastWord.length) + shortcut.expandedText
            lastShortcutSnapshot = textBeforeSpace to shortcut.expandedText
            _typedText.value = newText
            val deleteLen = if (topSuggestionCommitted.isNotEmpty()) topSuggestionCommitted.length else lastWord.length
            inputDelegate?.deleteSurroundingText(deleteLen, 0)
            inputDelegate?.commitText(shortcut.expandedText)

            undoBannerJob?.cancel()
            showUndoBanner.value = "تم استبدال \"${shortcut.shortcut}\" بـ \"${shortcut.expandedText}\""
            undoBannerJob = viewModelScope.launch { kotlinx.coroutines.delay(3000); showUndoBanner.value = null }
        }

        suggestedEmoji.value = T9Engine.getSuggestedEmoji(cleanWord)
        _typedText.value = _typedText.value + " "
        inputDelegate?.commitText(" ")
        logWordTyped(words.size)
    }

    private fun handleBackspace() {
        if (_isPredictiveMode.value && _t9Sequence.value.isNotEmpty()) {
            _t9Sequence.value = _t9Sequence.value.dropLast(1)
            updateSuggestions()
        } else {
            val text = _typedText.value
            if (text.isNotEmpty()) _typedText.value = text.dropLast(1)
            lastKeyPressed = null
            updateSuggestions()
            inputDelegate?.deleteSurroundingText(1, 0)
        }
        incrementBackspaceCount()
    }

    fun selectSuggestion(word: String) {
        if (_keyboardMode.value == KeyboardMode.T9_PREDICTIVE) {
            if (_t9Sequence.value.isNotEmpty()) {
                _typedText.value = _typedText.value + word
                _t9Sequence.value = ""; _suggestions.value = emptyList()
                logWordTyped(1)
                if (_shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
                inputDelegate?.commitText(word)
            }
        } else {
            val text = _typedText.value
            val lastWord = text.split(" ").lastOrNull() ?: ""
            if (lastWord.isNotEmpty()) {
                val baseText = text.substring(0, text.length - lastWord.length)
                _typedText.value = baseText + word
                _suggestions.value = emptyList()
                lastKeyPressed = null; multiTapIndex = 0
                logWordTyped(1)
                if (_shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
                inputDelegate?.deleteSurroundingText(lastWord.length, 0)
                inputDelegate?.commitText(word)
            }
        }
    }

    fun clearBuffer() { _t9Sequence.value = ""; _suggestions.value = emptyList(); lastKeyPressed = null }
    fun clearAllText() { _typedText.value = ""; clearBuffer() }

    fun addCustomWord(word: String) {
        if (word.isBlank()) return
        T9Engine.addCustomWord(word, _currentLanguage.value)
        updateSuggestions()
    }

    private fun commitCurrentBuffer() {
        if (_isPredictiveMode.value && _t9Sequence.value.isNotEmpty()) {
            val topSuggestion = _suggestions.value.firstOrNull() ?: _t9Sequence.value
            _typedText.value = _typedText.value + topSuggestion
            _t9Sequence.value = ""; _suggestions.value = emptyList()
            if (_shiftState.value == ShiftState.ONCE) _shiftState.value = ShiftState.OFF
            inputDelegate?.commitText(topSuggestion)
        }
        lastKeyPressed = null
    }

    private fun updateSuggestions() {
        if (!_isSuggestionsEnabled.value) { _suggestions.value = emptyList(); return }
        val currentLang = _currentLanguage.value
        if (_keyboardMode.value == KeyboardMode.T9_PREDICTIVE) {
            val sequence = _t9Sequence.value
            _suggestions.value = if (sequence.isNotEmpty()) formatSuggestions(T9Engine.getPredictions(sequence, currentLang)) else emptyList()
        } else {
            val text = _typedText.value
            if (text.isNotEmpty() && !text.endsWith(" ")) {
                val lastWord = text.split(" ").lastOrNull() ?: ""
                if (lastWord.isNotEmpty()) {
                    val dictionary = T9Engine.getDictionary(currentLang)
                    val matches = dictionary.filter { it.startsWith(lastWord, ignoreCase = true) && it.length > lastWord.length }
                    _suggestions.value = formatSuggestions(matches)
                } else _suggestions.value = emptyList()
            } else _suggestions.value = emptyList()
        }
    }

    private fun formatSuggestions(rawPredictions: List<String>): List<String> {
        return if (_shiftState.value != ShiftState.OFF) {
            rawPredictions.map { word ->
                if (_shiftState.value == ShiftState.CAPS_LOCK) word.uppercase()
                else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        } else rawPredictions
    }

    private fun logWordTyped(wordsCount: Int) {
        viewModelScope.launch {
            val stats = repository.allStats.first()
            val today = getCurrentDayOfWeek()
            val existing = stats.find { it.dateString == today }
            if (existing != null) {
                repository.insertStat(existing.copy(wordsTyped = existing.wordsTyped + 1, charsTyped = existing.charsTyped + 5, activeTimeSeconds = existing.activeTimeSeconds + 3))
            } else {
                repository.insertStat(TypingStat(dateString = today, wordsTyped = 1, charsTyped = 5, backspacesPressed = 0, activeTimeSeconds = 3))
            }
        }
    }

    private fun incrementBackspaceCount() {
        viewModelScope.launch {
            val stats = repository.allStats.first()
            val today = getCurrentDayOfWeek()
            val existing = stats.find { it.dateString == today }
            if (existing != null) repository.insertStat(existing.copy(backspacesPressed = existing.backspacesPressed + 1))
        }
    }

    private fun getCurrentDayOfWeek(): String {
        val days = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
        val calendar = java.util.Calendar.getInstance()
        return days[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    }

    fun setActiveTheme(theme: CustomTheme) { _activeTheme.value = theme }

    fun addCustomTheme(name: String, bgColor: String, keyBg: String, keyText: String, accent: String, corners: Int) {
        viewModelScope.launch {
            val newTheme = CustomTheme(name = name, bgColor = bgColor, keyBgColor = keyBg, keyTextColor = keyText, accentColor = accent, borderRadius = corners, fontFamily = _selectedFontFamily.value, isSystem = false)
            repository.insertTheme(newTheme)
            _activeTheme.value = newTheme
        }
    }

    fun deleteTheme(theme: CustomTheme) {
        viewModelScope.launch {
            if (!theme.isSystem) {
                repository.deleteTheme(theme)
                val themes = repository.allThemes.first()
                if (_activeTheme.value == theme) _activeTheme.value = themes.firstOrNull { it != theme } ?: themes.firstOrNull()
            }
        }
    }

    fun addShortcut(shortcut: String, expanded: String) {
        viewModelScope.launch { repository.insertShortcut(TextShortcut(shortcut = shortcut.lowercase().trim(), expandedText = expanded.trim())) }
    }
    fun deleteShortcut(id: Int) { viewModelScope.launch { repository.deleteShortcutById(id) } }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            val count = repository.allTasks.first().size
            repository.insertTask(PendingTask(title = title, description = description, orderIndex = count))
        }
    }
    fun toggleTaskCompletion(task: PendingTask) { viewModelScope.launch { repository.updateTask(task.copy(isCompleted = !task.isCompleted)) } }
    fun deleteTask(id: Int) { viewModelScope.launch { repository.deleteTaskById(id) } }

    fun moveTaskUp(task: PendingTask) {
        viewModelScope.launch {
            val tasks = repository.allTasks.first()
            val index = tasks.indexOf(task)
            if (index > 0) {
                val above = tasks[index - 1]
                repository.updateTask(task.copy(orderIndex = above.orderIndex))
                repository.updateTask(above.copy(orderIndex = task.orderIndex))
            }
        }
    }

    fun moveTaskDown(task: PendingTask) {
        viewModelScope.launch {
            val tasks = repository.allTasks.first()
            val index = tasks.indexOf(task)
            if (index < tasks.size - 1) {
                val below = tasks[index + 1]
                repository.updateTask(task.copy(orderIndex = below.orderIndex))
                repository.updateTask(below.copy(orderIndex = task.orderIndex))
            }
        }
    }

    fun exportLocalBackup(context: Context, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = org.json.JSONObject().apply {
                    put("language", _currentLanguage.value)
                    put("font", _selectedFontFamily.value)
                    put("activeThemeName", _activeTheme.value?.name ?: "")
                    put("hapticEnabled", isHapticEnabled.value)
                    put("oneHandedMode", oneHandedMode.value.name)
                    put("timestamp", System.currentTimeMillis())
                }
                val backupDir = File(context.filesDir, "backups")
                if (!backupDir.exists()) backupDir.mkdirs()
                val file = File(backupDir, "ai_keyboard_backup.json")
                file.writeText(json.toString(2))
                onResult("تم الحفظ بنجاح في: ${file.absolutePath}")
            } catch (e: Exception) {
                onResult("فشل الحفظ: ${e.message ?: "خطأ غير معروف"}")
            }
        }
    }
}

class KeyboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            val db = KeyboardDatabase.getDatabase(context)
            val repo = KeyboardRepository(db.keyboardDao())
            return KeyboardViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
