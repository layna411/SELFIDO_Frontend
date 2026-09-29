package com.simats.selfora.ui.screens.dressing

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simats.selfora.data.model.dressing.*
import com.simats.selfora.data.repository.DressingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

data class DressingUiState(
    val activity: DressingActivity? = null,
    val currentStepIndex: Int = 0,
    val currentPromptLevel: PromptLevel = PromptLevel.LEVEL_1,
    val isAutoSpeechEnabled: Boolean = true,
    val isSpeaking: Boolean = false,
    val isCompleted: Boolean = false,
    val stepRecords: Map<Int, StepPerformanceRecord> = emptyMap(),
    val isAssetMissing: Boolean = false,
    val currentReplayCount: Int = 0
) {
    val currentStep: DressingStep?
        get() = activity?.steps?.getOrNull(currentStepIndex)

    val totalSteps: Int
        get() = activity?.totalSteps ?: 18

    val progressPercentage: Float
        get() = if (totalSteps > 0) ((currentStepIndex + 1).toFloat() / totalSteps.toFloat()) * 100f else 0f
}

class DressingViewModel(
    application: Application
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = DressingRepository()

    private val _uiState = MutableStateFlow(DressingUiState())
    val uiState: StateFlow<DressingUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(application.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                // Play spoken guidance for initial step if auto speech enabled
                if (_uiState.value.isAutoSpeechEnabled) {
                    speakCurrentGuidance()
                }
            } else {
                Log.e("DressingViewModel", "TTS language missing or not supported")
            }
        } else {
            Log.e("DressingViewModel", "TTS initialization failed with status $status")
        }
    }

    fun loadActivity(activityId: String) {
        val activity = repository.getActivityById(activityId)
        _uiState.update {
            it.copy(
                activity = activity,
                currentStepIndex = 0,
                isCompleted = false,
                stepRecords = emptyMap(),
                currentReplayCount = 0
            )
        }
        if (isTtsReady && _uiState.value.isAutoSpeechEnabled) {
            speakCurrentGuidance()
        }
    }

    fun nextStep() {
        val state = _uiState.value
        val activity = state.activity ?: return
        val currentIdx = state.currentStepIndex

        // Record performance for current step
        recordStepPerformance(currentIdx, state.currentPromptLevel)

        if (currentIdx < activity.steps.size - 1) {
            _uiState.update {
                it.copy(
                    currentStepIndex = currentIdx + 1,
                    currentReplayCount = 0,
                    currentPromptLevel = PromptLevel.LEVEL_1
                )
            }
            if (isTtsReady && _uiState.value.isAutoSpeechEnabled) {
                speakCurrentGuidance()
            }
        } else {
            // Reached Step 18 - Complete activity
            _uiState.update {
                it.copy(
                    isCompleted = true
                )
            }
            speakGuidance("Great Job! All 18 steps completed!")
        }
    }

    fun previousStep() {
        val currentIdx = _uiState.value.currentStepIndex
        if (currentIdx > 0) {
            _uiState.update {
                it.copy(
                    currentStepIndex = currentIdx - 1,
                    currentReplayCount = 0,
                    isCompleted = false
                )
            }
            if (isTtsReady && _uiState.value.isAutoSpeechEnabled) {
                speakCurrentGuidance()
            }
        }
    }

    fun replayAnimation() {
        _uiState.update {
            it.copy(currentReplayCount = it.currentReplayCount + 1)
        }
        speakCurrentGuidance()
    }

    fun setPromptLevel(level: PromptLevel) {
        _uiState.update {
            it.copy(currentPromptLevel = level)
        }
    }

    fun toggleAutoSpeech() {
        _uiState.update {
            it.copy(isAutoSpeechEnabled = !it.isAutoSpeechEnabled)
        }
    }

    fun speakCurrentGuidance() {
        val guidance = _uiState.value.currentStep?.childGuidance
        if (!guidance.isNullOrEmpty()) {
            speakGuidance(guidance)
        }
    }

    private fun speakGuidance(text: String) {
        if (isTtsReady && tts != null) {
            _uiState.update { it.copy(isSpeaking = true) }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DressingGuidanceTTS")
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    private fun recordStepPerformance(stepIndex: Int, promptLevel: PromptLevel) {
        val currentStep = _uiState.value.currentStep ?: return
        val updatedRecords = _uiState.value.stepRecords.toMutableMap()
        updatedRecords[stepIndex] = StepPerformanceRecord(
            stepId = currentStep.stepId,
            stepNumber = currentStep.stepNumber,
            promptLevel = promptLevel,
            completed = true,
            replayCount = _uiState.value.currentReplayCount
        )
        _uiState.update { it.copy(stepRecords = updatedRecords) }
    }

    fun restartSession() {
        _uiState.update {
            it.copy(
                currentStepIndex = 0,
                isCompleted = false,
                currentReplayCount = 0,
                stepRecords = emptyMap()
            )
        }
        speakCurrentGuidance()
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
