# AI KEYBOARD

تطبيق لوحة مفاتيح ذكية (T9 وتنبؤية) مبني بـ Kotlin و Jetpack Compose.

## المتطلبات
- Android Studio
- تشغيل التطبيق على جهاز أو محاكي يدعم Android 8.0 (API 26) فأعلى

## بعد فتح المشروع
1. افتح المجلد في Android Studio واسمح له بمزامنة Gradle.
2. لتفعيل ميزة الترجمة الذكية عبر Gemini، أضف مفتاح API الخاص بك عند استدعاء
   `viewModel.translateWithAI(text, from, to, apiKey)`.
3. شغّل التطبيق، فعّل AI KEYBOARD من الإعدادات، واختره كلوحة مفاتيح افتراضية.
