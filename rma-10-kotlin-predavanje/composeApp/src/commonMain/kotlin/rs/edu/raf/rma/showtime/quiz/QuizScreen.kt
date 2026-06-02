package rs.edu.raf.rma.showtime.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.showtime.quiz.domain.QuestionType
import rs.edu.raf.rma.showtime.quiz.domain.QuizQuestion

@Composable
fun QuizScreen(
    onExit: () -> Unit,
    viewModel: QuizViewModel = org.koin.compose.viewmodel.koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is QuizContract.UiState.Idle -> IdleContent(
            bestScore = s.bestScore,
            onStart = { viewModel.setEvent(QuizContract.UiEvent.Start) },
        )

        QuizContract.UiState.Loading -> CenteredMessage {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Preparing your quiz…")
        }

        is QuizContract.UiState.NotEnough -> CenteredMessage {
            Text(
                text = s.message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onExit) { Text("Back to catalog") }
        }

        is QuizContract.UiState.Error -> CenteredMessage {
            Text(s.message, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { viewModel.setEvent(QuizContract.UiEvent.PlayAgain) }) { Text("Try again") }
        }

        is QuizContract.UiState.Playing -> PlayingContent(
            state = s,
            onSelect = { viewModel.setEvent(QuizContract.UiEvent.SelectAnswer(it)) },
            onExit = onExit,
        )

        is QuizContract.UiState.Result -> ResultContent(
            state = s,
            onPlayAgain = { viewModel.setEvent(QuizContract.UiEvent.PlayAgain) },
            onDone = onExit,
        )
    }
}

@Composable
private fun IdleContent(bestScore: Double?, onStart: () -> Unit) {
    CenteredMessage {
        Text("Movie Knowledge Quiz", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "10 questions · 60 seconds",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (bestScore != null) {
            Spacer(Modifier.height(8.dp))
            Text("Best score: ${formatScore(bestScore)}")
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStart) { Text("Start quiz") }
    }
}

@Composable
private fun PlayingContent(
    state: QuizContract.UiState.Playing,
    onSelect: (Int) -> Unit,
    onExit: () -> Unit,
) {
    var showAbandonDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Header: progress + timer + close (no Up button per spec)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Question ${state.questionNumber}/${state.total}",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${state.remainingSeconds}s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.remainingSeconds <= 10) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            IconButton(onClick = { showAbandonDialog = true }) {
                Icon(Icons.Default.Close, contentDescription = "Abandon quiz")
            }
        }

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { state.remainingSeconds.toFloat() / 60f },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = state.index,
            transitionSpec = {
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
            },
            label = "question",
            modifier = Modifier.weight(1f),
        ) { targetIndex ->
            val isCurrent = targetIndex == state.index
            QuestionBody(
                question = state.questions[targetIndex],
                revealed = isCurrent && state.revealed,
                selectedIndex = if (isCurrent) state.selectedIndex else null,
                onSelect = onSelect,
            )
        }
    }

    if (showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            title = { Text("Abandon quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showAbandonDialog = false
                    onExit()
                }) { Text("Abandon") }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonDialog = false }) { Text("Keep playing") }
            },
        )
    }
}

@Composable
private fun QuestionBody(
    question: QuizQuestion,
    revealed: Boolean,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = question.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (question.type == QuestionType.GUESS_MOVIE) 16f / 9f else 2f / 3f)
                .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.height(12.dp))

        Text(
            text = promptFor(question),
            style = MaterialTheme.typography.titleMedium,
        )
        question.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.options.forEachIndexed { index, option ->
                OptionButton(
                    text = option,
                    state = optionVisualState(index, question.correctIndex, selectedIndex, revealed),
                    enabled = !revealed,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

private enum class OptionVisual { NEUTRAL, CORRECT, WRONG }

private fun optionVisualState(
    index: Int,
    correctIndex: Int,
    selectedIndex: Int?,
    revealed: Boolean,
): OptionVisual = when {
    !revealed -> OptionVisual.NEUTRAL
    index == correctIndex -> OptionVisual.CORRECT
    index == selectedIndex -> OptionVisual.WRONG
    else -> OptionVisual.NEUTRAL
}

@Composable
private fun OptionButton(
    text: String,
    state: OptionVisual,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val correctColor = Color(0xFF2E7D32)
    val wrongColor = Color(0xFFC62828)
    when (state) {
        OptionVisual.NEUTRAL -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(text) }

        OptionVisual.CORRECT -> Button(
            onClick = {},
            enabled = false,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                disabledContainerColor = correctColor,
                disabledContentColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(text) }

        OptionVisual.WRONG -> Button(
            onClick = {},
            enabled = false,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                disabledContainerColor = wrongColor,
                disabledContentColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(text) }
    }
}

@Composable
private fun ResultContent(
    state: QuizContract.UiState.Result,
    onPlayAgain: () -> Unit,
    onDone: () -> Unit,
) {
    CenteredMessage {
        Text("Quiz complete!", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            text = formatScore(state.score),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        Text("Correct: ${state.correct}")
        Text("Incorrect: ${state.incorrect}")
        Text("Time used: ${state.timeUsedSeconds}s")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) { Text("Play again") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

private fun promptFor(question: QuizQuestion): String = when (question.type) {
    QuestionType.GUESS_MOVIE -> "Which movie is this?"
    QuestionType.GUESS_YEAR -> "In which year was this released?"
    QuestionType.GUESS_ACTOR -> "Who stars in this movie?"
}

private fun formatScore(score: Double): String {
    val rounded = (score * 100).toLong()
    val whole = rounded / 100
    val frac = (rounded % 100).toInt()
    val fracStr = if (frac < 10) "0$frac" else "$frac"
    return "$whole.$fracStr"
}
