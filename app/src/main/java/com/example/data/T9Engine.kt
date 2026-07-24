package com.example.data

object T9Engine {
    private val customWordsEn = mutableSetOf<String>()
    private val customWordsAr = mutableSetOf<String>()

    private val keyMapEn = mapOf(
        '2' to listOf('a', 'b', 'c'), '3' to listOf('d', 'e', 'f'), '4' to listOf('g', 'h', 'i'),
        '5' to listOf('j', 'k', 'l'), '6' to listOf('m', 'n', 'o'), '7' to listOf('p', 'q', 'r', 's'),
        '8' to listOf('t', 'u', 'v'), '9' to listOf('w', 'x', 'y', 'z'), '1' to listOf('.', ',', '?', '!')
    )

    private val keyMapAr = mapOf(
        '2' to listOf('ا', 'ب', 'ت'), '3' to listOf('ث', 'ج', 'ح'), '4' to listOf('خ', 'د', 'ذ'),
        '5' to listOf('ر', 'ز', 'س'), '6' to listOf('ش', 'ص', 'ض'), '7' to listOf('ط', 'ظ', 'ع', 'غ'),
        '8' to listOf('ف', 'ق', 'ك'), '9' to listOf('ل', 'م', 'ن', 'ه', 'و', 'ي'), '1' to listOf('.', '،', '؟', '!')
    )

    private val dictionaryEn = listOf(
        "hello", "hi", "how", "are", "you", "good", "morning", "night", "keyboard", "smart",
        "work", "love", "task", "theme", "color", "shortcut", "user", "best", "great", "cool",
        "home", "play", "music", "world", "text", "typing", "happy", "nice", "yes", "no",
        "please", "thanks", "friend", "welcome", "app", "settings", "translate"
    )

    private val dictionaryAr = listOf(
        "مرحبا", "اهلا", "كيف", "حالك", "صباح", "الخير", "مساء", "كيبورد", "ذكي", "عمل",
        "حب", "مهمة", "ثيم", "لون", "اختصار", "مستخدم", "افضل", "رائع", "منزل", "موسيقى",
        "عالم", "نص", "كتابة", "سعيد", "جميل", "نعم", "لا", "شكرا", "صديق", "اهلا بك", "تطبيق"
    )

    private val emojiMap = mapOf(
        "happy" to "😊", "سعيد" to "😊", "love" to "❤️", "حب" to "❤️",
        "smart" to "🧠", "ذكي" to "🧠", "music" to "🎵", "موسيقى" to "🎵",
        "great" to "🔥", "رائع" to "🔥", "friend" to "🤝", "صديق" to "🤝",
        "thanks" to "🙏", "شكرا" to "🙏", "welcome" to "👋", "اهلا" to "👋",
        "cool" to "😎", "morning" to "☀️", "صباح" to "☀️", "night" to "🌙", "مساء" to "🌙",
        "keyboard" to "⌨️", "كيبورد" to "⌨️"
    )

    fun getKeyMap(lang: String): Map<Char, List<Char>> = if (lang == "AR") keyMapAr else keyMapEn

    fun getDictionary(lang: String): List<String> {
        val base = if (lang == "AR") dictionaryAr else dictionaryEn
        val custom = if (lang == "AR") customWordsAr else customWordsEn
        return base + custom
    }

    fun addCustomWord(word: String, lang: String) {
        if (lang == "AR") customWordsAr.add(word.lowercase()) else customWordsEn.add(word.lowercase())
    }

    fun getSuggestedEmoji(word: String): String? = emojiMap[word.lowercase().trim()]

    fun getPredictions(sequence: String, lang: String): List<String> {
        val keyMap = getKeyMap(lang)
        val dictionary = getDictionary(lang)
        if (sequence.isEmpty()) return emptyList()
        return dictionary.filter { word ->
            if (word.length < sequence.length) return@filter false
            sequence.withIndex().all { (index, digit) ->
                val possibleChars = keyMap[digit] ?: return@all false
                index < word.length && word[index].lowercaseChar() in possibleChars
            }
        }.sortedBy { it.length }
    }
}
