package com.simats.selfora.ui.therapist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StepRecordState(
    val stepId: Long,
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val titleTa: String? = null,
    val instructionTa: String? = null,
    val childInstruction: String? = null,
    val childInstructionTa: String? = null,
    val media: List<MediaResponse> = emptyList(),
    val selectedLevel: ClinicalPromptLevel? = null,
    val outcome: String = "PENDING", // COMPLETED, UNABLE, NOT_ASSESSED
    val notes: String = "",
    val isSaved: Boolean = false,
    val durationSeconds: Int = 15
)

data class TherapySessionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val childId: Long = 0L,
    val activityId: Long = 0L,
    val activityTitle: String = "",
    val sessionId: Long? = null,
    val stage: TherapySessionStage = TherapySessionStage.SELECT_ACTIVITY,
    val steps: List<TaskStepResponse> = emptyList(),
    val currentStepIndex: Int = 0,
    val stepRecords: Map<Int, StepRecordState> = emptyMap(),
    val secondsElapsed: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TherapySessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TherapySessionUiState())
    val uiState: StateFlow<TherapySessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initSession(childId: Long, activityId: Long, existingSessionId: Long? = null) {
        _uiState.update {
            it.copy(
                childId = childId,
                activityId = activityId,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                // 1. Fetch task steps for activity
                val stepsRes = ApiClient.apiService.getTaskSteps(activityId)
                val loadedSteps = if (stepsRes.isSuccessful && !stepsRes.body().isNullOrEmpty()) {
                    stepsRes.body()!!
                } else {
                    SelfidoCatalogue.getStepsForActivity(activityId).ifEmpty { getSampleTShirtSteps() }
                }

                // 2. Obtain or resume therapy session
                var sId = existingSessionId
                var existingPerformanceMap = mutableMapOf<Long, PerformanceRecordResponse>()

                if (sId != null && sId > 0L) {
                    // Load existing session records
                    val sessionRes = ApiClient.apiService.getSessionById(sId)
                    if (sessionRes.isSuccessful && sessionRes.body() != null) {
                        val sessionData = sessionRes.body()!!
                        sessionData.performanceRecords.forEach { pr ->
                            existingPerformanceMap[pr.stepId] = pr
                        }
                    }
                } else {
                    // Start new clinical therapy session
                    val startRes = ApiClient.apiService.startSession(
                        StartSessionRequest(
                            childId = childId,
                            activityId = activityId,
                            sessionType = "CLINIC_THERAPY",
                            environment = "CLINIC"
                        )
                    )
                    if (startRes.isSuccessful && startRes.body() != null) {
                        sId = startRes.body()!!.sessionId
                    }
                }

                // 3. Build step records, preserving existing saved values if present
                val recordsMap = mutableMapOf<Int, StepRecordState>()
                loadedSteps.forEachIndexed { idx, st ->
                    val existing = existingPerformanceMap[st.id]
                    if (existing != null) {
                        val promptLevel = if (existing.outcome == "UNABLE") {
                            ClinicalPromptLevel.UNABLE
                        } else if (existing.outcome == "NOT_ASSESSED") {
                            ClinicalPromptLevel.NOT_ASSESSED
                        } else {
                            ClinicalPromptLevel.fromId(existing.promptLevelId)
                        }

                        recordsMap[idx] = StepRecordState(
                            stepId = st.id,
                            stepNumber = st.stepNumber,
                            title = st.title,
                            instruction = st.instructionText ?: st.childInstruction ?: "",
                            titleTa = st.titleTa,
                            instructionTa = st.instructionTextTa ?: st.childInstructionTa,
                            childInstruction = st.childInstruction,
                            childInstructionTa = st.childInstructionTa,
                            media = st.media,
                            selectedLevel = promptLevel,
                            outcome = existing.outcome,
                            notes = existing.therapistNote ?: "",
                            isSaved = true,
                            durationSeconds = existing.durationSeconds
                        )
                    } else {
                        recordsMap[idx] = StepRecordState(
                            stepId = st.id,
                            stepNumber = st.stepNumber,
                            title = st.title,
                            instruction = st.instructionText ?: st.childInstruction ?: "",
                            titleTa = st.titleTa,
                            instructionTa = st.instructionTextTa ?: st.childInstructionTa,
                            childInstruction = st.childInstruction,
                            childInstructionTa = st.childInstructionTa,
                            media = st.media,
                            selectedLevel = null,
                            outcome = "PENDING",
                            notes = "",
                            isSaved = false
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sessionId = sId,
                        steps = loadedSteps,
                        stepRecords = recordsMap,
                        currentStepIndex = 0
                    )
                }

                startTimer()
            } catch (e: Exception) {
                // Fallback graceful initialization
                val loadedSteps = SelfidoCatalogue.getStepsForActivity(activityId).ifEmpty {
                    when (activityId) {
                        2L -> getSampleShirtSteps()
                        3L -> getSampleJacketSteps()
                        4L -> getSampleVestSteps()
                        5L -> getSampleTrousersSteps()
                        else -> getSampleTShirtSteps()
                    }
                }
                val recordsMap = loadedSteps.mapIndexed { idx, st ->
                    idx to StepRecordState(
                        stepId = st.id,
                        stepNumber = st.stepNumber,
                        title = st.title,
                        instruction = st.instructionText ?: st.childInstruction ?: "",
                        titleTa = st.titleTa,
                        instructionTa = st.instructionTextTa ?: st.childInstructionTa,
                        childInstruction = st.childInstruction,
                        childInstructionTa = st.childInstructionTa,
                        media = st.media,
                        selectedLevel = null,
                        outcome = "PENDING"
                    )
                }.toMap()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        steps = loadedSteps,
                        stepRecords = recordsMap,
                        currentStepIndex = 0
                    )
                }
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(secondsElapsed = it.secondsElapsed + 1) }
            }
        }
    }

    fun setStage(stage: TherapySessionStage) {
        _uiState.update { it.copy(stage = stage, errorMessage = null) }
    }

    fun selectPromptLevel(stepIndex: Int, level: ClinicalPromptLevel) {
        val currentRecords = _uiState.value.stepRecords.toMutableMap()
        val current = currentRecords[stepIndex] ?: return

        val outcome = when (level) {
            ClinicalPromptLevel.UNABLE -> "UNABLE"
            ClinicalPromptLevel.NOT_ASSESSED -> "NOT_ASSESSED"
            else -> "COMPLETED"
        }

        currentRecords[stepIndex] = current.copy(
            selectedLevel = level,
            outcome = outcome
        )

        _uiState.update {
            it.copy(
                stepRecords = currentRecords,
                errorMessage = null
            )
        }
    }

    fun updateTherapistNotes(stepIndex: Int, notes: String) {
        val currentRecords = _uiState.value.stepRecords.toMutableMap()
        val current = currentRecords[stepIndex] ?: return
        currentRecords[stepIndex] = current.copy(notes = notes)
        _uiState.update { it.copy(stepRecords = currentRecords) }
    }

    fun saveCurrentStepPerformance(onAdvanceToNextStep: () -> Unit, onSessionComplete: () -> Unit) {
        val state = _uiState.value
        val currentIndex = state.currentStepIndex
        val currentRecord = state.stepRecords[currentIndex]
        val steps = state.steps

        if (currentRecord == null || currentRecord.selectedLevel == null) {
            _uiState.update { it.copy(errorMessage = "Please select a prompt level (L0-L6, Unable, or Not Assessed) before saving.") }
            return
        }

        val level = currentRecord.selectedLevel
        val outcome = when (level) {
            ClinicalPromptLevel.UNABLE -> "UNABLE"
            ClinicalPromptLevel.NOT_ASSESSED -> "NOT_ASSESSED"
            else -> "COMPLETED"
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val sId = state.sessionId
                if (sId != null && sId > 0L) {
                    val req = RecordPerformanceRequest(
                        stepId = currentRecord.stepId,
                        promptLevelId = level.id,
                        outcome = outcome,
                        durationSeconds = 15,
                        therapistNote = currentRecord.notes.ifBlank { null },
                        environment = "CLINIC"
                    )
                    ApiClient.apiService.recordPerformance(sId, req)
                }

                // Update saved state in map
                val updatedRecords = _uiState.value.stepRecords.toMutableMap()
                updatedRecords[currentIndex] = currentRecord.copy(isSaved = true)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        stepRecords = updatedRecords,
                        successMessage = "Step ${currentIndex + 1} performance recorded: ${level.shortLabel}"
                    )
                }

                // Progress to next step or complete session
                if (currentIndex < steps.size - 1) {
                    _uiState.update {
                        it.copy(
                            currentStepIndex = currentIndex + 1,
                            stage = TherapySessionStage.STEP_VIEW
                        )
                    }
                    onAdvanceToNextStep()
                } else {
                    onSessionComplete()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to persist record: ${e.message}"
                    )
                }
            }
        }
    }

    fun completeSession(onCompleted: (Long) -> Unit) {
        val sId = _uiState.value.sessionId
        if (sId == null || sId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Cannot complete session: session has not been initialized.") }
            return
        }
        viewModelScope.launch {
            try {
                ApiClient.apiService.completeSession(sId)
            } catch (_: Exception) {}
            onCompleted(sId)
        }
    }

    fun goToStep(index: Int) {
        val steps = _uiState.value.steps
        if (index in steps.indices) {
            _uiState.update { it.copy(currentStepIndex = index, errorMessage = null) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

