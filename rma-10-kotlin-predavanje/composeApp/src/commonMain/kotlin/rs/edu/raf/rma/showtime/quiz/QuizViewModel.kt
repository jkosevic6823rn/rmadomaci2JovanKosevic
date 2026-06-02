package rs.edu.raf.rma.showtime.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.quiz.data.QuizRepository
import rs.edu.raf.rma.showtime.quiz.domain.QuizScoring

class QuizViewModel(
    private val repository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<QuizContract.UiState>(QuizContract.UiState.Loading)
    val state = _state.asStateFlow()

    private val events = MutableSharedFlow<QuizContract.UiEvent>()
    fun setEvent(event: QuizContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private var timerJob: Job? = null

    init {
        observeEvents()
        showIdle()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    QuizContract.UiEvent.Start,
                    QuizContract.UiEvent.PlayAgain -> startQuiz()
                    is QuizContract.UiEvent.SelectAnswer -> onAnswer(event.optionIndex)
                }
            }
        }
    }

    private fun showIdle() {
        viewModelScope.launch {
            val best = runCatching { repository.observeBestScore().first() }.getOrNull()
            _state.value = QuizContract.UiState.Idle(bestScore = best)
        }
    }

    private fun startQuiz() {
        timerJob?.cancel()
        viewModelScope.launch {
            _state.value = QuizContract.UiState.Loading
            runCatching { repository.ensurePool() }

            if (!repository.canStartQuiz()) {
                _state.value = QuizContract.UiState.NotEnough(
                    "Browse the catalog first to populate your quiz pool.",
                )
                return@launch
            }

            val questions = runCatching { repository.buildSession() }.getOrDefault(emptyList())
            if (questions.size < QuizScoring.TOTAL_QUESTIONS) {
                _state.value = QuizContract.UiState.Error(
                    "Could not prepare the quiz. Check your connection and try again.",
                )
                return@launch
            }

            _state.value = QuizContract.UiState.Playing(
                questions = questions,
                index = 0,
                selectedIndex = null,
                revealed = false,
                correctCount = 0,
                remainingSeconds = QuizScoring.TIME_LIMIT_SECONDS,
            )
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value as? QuizContract.UiState.Playing ?: break
                val remaining = current.remainingSeconds - 1
                if (remaining <= 0) {
                    _state.value = current.copy(remainingSeconds = 0)
                    finish(timedOut = true)
                    break
                }
                _state.value = current.copy(remainingSeconds = remaining)
            }
        }
    }

    private fun onAnswer(optionIndex: Int) {
        val current = _state.value as? QuizContract.UiState.Playing ?: return
        if (current.revealed) return

        val isCorrect = optionIndex == current.current.correctIndex
        _state.value = current.copy(
            selectedIndex = optionIndex,
            revealed = true,
            correctCount = current.correctCount + if (isCorrect) 1 else 0,
        )

        viewModelScope.launch {
            delay(REVEAL_DELAY_MS)
            advance()
        }
    }

    private fun advance() {
        val current = _state.value as? QuizContract.UiState.Playing ?: return
        val next = current.index + 1
        if (next >= current.questions.size) {
            finish(timedOut = false)
        } else {
            _state.value = current.copy(index = next, selectedIndex = null, revealed = false)
        }
    }

    private fun finish(timedOut: Boolean) {
        val current = _state.value as? QuizContract.UiState.Playing ?: return
        timerJob?.cancel()

        val remaining = if (timedOut) 0 else current.remainingSeconds
        val correct = current.correctCount
        val score = QuizScoring.score(correct, remaining)

        _state.value = QuizContract.UiState.Result(
            score = score,
            correct = correct,
            incorrect = current.questions.size - correct,
            timeUsedSeconds = QuizScoring.TIME_LIMIT_SECONDS - remaining,
        )

        viewModelScope.launch { repository.submitResult(score, correct) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val REVEAL_DELAY_MS = 800L
    }
}
