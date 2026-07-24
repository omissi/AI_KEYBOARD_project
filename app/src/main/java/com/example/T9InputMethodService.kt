package com.example

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputConnection
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.KeyboardDatabase
import com.example.data.KeyboardRepository
import com.example.viewmodel.InputDelegate
import com.example.viewmodel.KeyboardViewModel

/**
 * خدمة إدخال Android الحقيقية (Input Method Service) التي تعرض واجهة الكيبورد
 * فوق تطبيقات النظام. تربط أزرار الكيبورد بـ [KeyboardViewModel] عبر [InputDelegate]
 * لإرسال النص فعلياً إلى حقل الإدخال النشط (InputConnection)، بدل أن تكون مجرد محاكاة داخلية.
 */
class T9InputMethodService : InputMethodService() {

    private var viewModel: KeyboardViewModel? = null
    private var lifecycleOwner: KeyboardServiceLifecycleOwner? = null

    override fun onCreate() {
        super.onCreate()
        val db = KeyboardDatabase.getDatabase(applicationContext)
        val repo = KeyboardRepository(db.keyboardDao())
        viewModel = KeyboardViewModel(repo)
        lifecycleOwner = KeyboardServiceLifecycleOwner().apply { onCreate() }
    }

    override fun onCreateInputView(): View {
        val vm = viewModel ?: throw IllegalStateException("ViewModel not initialized")

        vm.inputDelegate = object : InputDelegate {
            override fun commitText(text: CharSequence) {
                currentInputConnection?.commitText(text, 1)
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int) {
                currentInputConnection?.deleteSurroundingText(beforeLength, afterLength)
            }
        }

        val composeView = ComposeView(this).apply {
            val owner = lifecycleOwner
            if (owner != null) {
                setViewTreeLifecycleOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
            }
            setContent {
                com.example.ui.theme.MyApplicationTheme {
                    com.example.components.KeyboardView(viewModel = vm)
                }
            }
        }
        return composeView
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleOwner?.onResume()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleOwner?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner?.onDestroy()
    }
}

/**
 * Lifecycle/SavedState owner بسيط ليدعم عرض Compose داخل InputMethodService،
 * الذي لا يوفر بشكل افتراضي دورة حياة متوافقة مع Jetpack Compose.
 */
class KeyboardServiceLifecycleOwner :
    androidx.lifecycle.LifecycleOwner,
    androidx.savedstate.SavedStateRegistryOwner {

    private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
    private val savedStateRegistryController = androidx.savedstate.SavedStateRegistryController.create(this)

    override val lifecycle: androidx.lifecycle.Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: androidx.savedstate.SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
    }

    fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
    }
}
