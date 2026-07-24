package com.example.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object GeminiTranslationService {

    private const val MODEL_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    sealed class ServiceResult {
        data class Success(val translatedText: String) : ServiceResult()
        data class Error(val message: String) : ServiceResult()
    }

    suspend fun translate(text: String, fromLang: String, toLang: String, apiKey: String): ServiceResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext ServiceResult.Error("مفتاح Gemini API غير مُعرّف. يرجى إضافته في إعدادات التطبيق.")
        if (text.isBlank()) return@withContext ServiceResult.Error("النص المطلوب ترجمته فارغ.")

        try {
            val url = URL("$MODEL_ENDPOINT?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 15000

            val promptText = "Translate the following text from $fromLang to $toLang. Return ONLY the translated text with no extra explanation:\n\n$text"
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", promptText)))))
            }

            val outputStream: OutputStream = connection.outputStream
            outputStream.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val response = reader.readText()
                reader.close()
                val json = JSONObject(response)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val translated = parts?.optJSONObject(0)?.optString("text")?.trim()
                    if (!translated.isNullOrBlank()) return@withContext ServiceResult.Success(translated)
                }
                ServiceResult.Error("لم يتم استلام ترجمة صالحة من الخدمة. حاول مجدداً.")
            } else {
                val errorStream = connection.errorStream
                val errorMsg = errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "خطأ غير معروف"
                ServiceResult.Error("فشل الاتصال بخدمة الترجمة (كود $responseCode). $errorMsg")
            }
        } catch (e: java.net.UnknownHostException) {
            ServiceResult.Error("لا يوجد اتصال بالإنترنت. تحقق من الشبكة وحاول مجدداً.")
        } catch (e: java.net.SocketTimeoutException) {
            ServiceResult.Error("انتهت مهلة الاتصال بالخدمة. حاول مجدداً.")
        } catch (e: Exception) {
            ServiceResult.Error("حدث خطأ غير متوقع: ${e.message ?: "غير معروف"}")
        }
    }
}
