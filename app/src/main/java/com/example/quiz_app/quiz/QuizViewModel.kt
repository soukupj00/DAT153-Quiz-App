package com.example.quiz_app.quiz

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quiz_app.data.AppDatabase
import com.example.quiz_app.data.QuizRepository
import com.example.quiz_app.quiz.util.generateQuizEntry
import com.example.quiz_app.types.GalleryEntry
import com.example.quiz_app.types.QuizEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Quiz screen. Manages the quiz logic, state, and data loading.
 *
 * @param application The application context.
 * @param savedStateHandle A handle to the saved state of the ViewModel. This is used to
 * restore the quiz state after process death (e.g., orientation change).
 */
class QuizViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val repository: QuizRepository

    // The private, mutable state flow that holds the current UI state.
    private val _quizState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    // The public, read-only state flow that the UI observes for changes.
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    private var allEntries: List<GalleryEntry> = emptyList()
    private var quizEntries: List<QuizEntry> = emptyList()

    // Properties that are saved and restored automatically by `savedStateHandle`.
    // This is crucial for surviving process death.
    private var currentIndex: Int by savedStateHandle.delegate(0)
    private var score: Int by savedStateHandle.delegate(0)
    private var attempts: Int by savedStateHandle.delegate(0)
    private var selectedOption: String? by savedStateHandle.delegate(null)
    private var isAnswered: Boolean by savedStateHandle.delegate(false)

    init {
        val quizItemDao = AppDatabase.getDatabase(application, viewModelScope).quizItemDao()
        repository = QuizRepository(quizItemDao)
        loadQuiz()
    }

    /**
     * Loads quiz data from the repository. This function is called once during
     * ViewModel initialization and collects changes from the database.
     */
    private fun loadQuiz() {
        viewModelScope.launch {
            repository.allItems.collect { items ->
                if (items.isNotEmpty()) {
                    val newAllEntries = items.map { item ->
                        GalleryEntry(item.name, if (item.isDrawable) item.uri.substringAfterLast("/").toInt() else Uri.parse(item.uri))
                    }

                    // Check for new entries that are not yet in the quiz.
                    val existingNames = quizEntries.map { it.correctName }.toSet()
                    val addedEntries = newAllEntries.filter { it.name !in existingNames }

                    allEntries = newAllEntries

                    if (addedEntries.isNotEmpty()) {
                        // If the quiz is new, shuffle all entries. Otherwise, append new ones.
                        val newQuizEntries = if (quizEntries.isEmpty()) {
                            allEntries.shuffled().map { generateQuizEntry(it, allEntries) }
                        } else {
                            addedEntries.shuffled().map { generateQuizEntry(it, allEntries) }
                        }
                        quizEntries = quizEntries + newQuizEntries
                    }

                    updateCurrentQuestion()
                }
            }
        }
    }

    /**
     * Updates the UI state with the current question or finishes the quiz if all questions are answered.
     */
    private fun updateCurrentQuestion() {
        if (currentIndex < quizEntries.size) {
            val currentEntry = quizEntries[currentIndex]
            _quizState.value = QuizUiState.Question(
                entry = currentEntry,
                score = score,
                questionNumber = currentIndex + 1,
                totalQuestions = quizEntries.size,
                isAnswered = isAnswered,
                selectedOption = selectedOption
            )
        } else if (quizEntries.isNotEmpty()) {
            _quizState.value = QuizUiState.Finished(score, attempts)
        }
    }

    /**
     * Submits an answer for the current question.
     */
    fun submitAnswer(option: String) {
        if (!isAnswered) {
            selectedOption = option
            isAnswered = true
            attempts++
            val state = _quizState.value
            if (state is QuizUiState.Question) {
                if (option == state.entry.correctName) {
                    score++
                }
                updateCurrentQuestion()
            }
        }
    }

    /**
     * Moves to the next question.
     */
    fun nextQuestion() {
        isAnswered = false
        selectedOption = null
        currentIndex++
        updateCurrentQuestion()
    }

    /**
     * Resets the quiz to its initial state with a new set of shuffled questions.
     */
    fun restartQuiz() {
        currentIndex = 0
        score = 0
        attempts = 0
        isAnswered = false
        selectedOption = null
        quizEntries = allEntries.shuffled().map { generateQuizEntry(it, allEntries) }
        updateCurrentQuestion()
    }
}

/**
 * A sealed class representing the different states of the Quiz UI.
 */
sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Question(
        val entry: QuizEntry,
        val score: Int,
        val questionNumber: Int,
        val totalQuestions: Int,
        val isAnswered: Boolean,
        val selectedOption: String?
    ) : QuizUiState()
    data class Finished(val score: Int, val total: Int) : QuizUiState()
}

/**
 * A delegate for accessing properties from a SavedStateHandle.
 * This is a common pattern to simplify state persistence in ViewModels.
 */
private fun <T> SavedStateHandle.delegate(defaultValue: T) = object : kotlin.properties.ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        return get<T>(property.name) ?: defaultValue
    }

    override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
        set(property.name, value)
    }
}
