package com.simats.selfora.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    private var currentLanguage = Locale.US

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(currentLanguage)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(0.80f) // Slower, autism-friendly calm speech rate
                tts?.setPitch(1.05f) // Friendly clear voice pitch
            } else {
                // Fallback to US English if requested locale is unsupported
                tts?.setLanguage(Locale.US)
                isInitialized = true
                tts?.setSpeechRate(0.80f)
            }
        }
    }

    fun setLanguage(languageCode: String) {
        currentLanguage = if (languageCode.equals("TA", ignoreCase = true)) {
            Locale("ta", "IN")
        } else {
            Locale.US
        }
        if (isInitialized) {
            val res = tts?.setLanguage(currentLanguage)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
        }
    }

    fun speak(text: String, isAudioEnabled: Boolean = true) {
        if (isAudioEnabled && isInitialized && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SelforaTTS")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
