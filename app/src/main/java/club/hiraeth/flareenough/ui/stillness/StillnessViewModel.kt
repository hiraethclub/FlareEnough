package club.hiraeth.flareenough.ui.stillness

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.hiraeth.flareenough.data.db.entity.SessionType
import club.hiraeth.flareenough.data.repository.MeditationRepository
import club.hiraeth.flareenough.stillness.BellPlayer
import club.hiraeth.flareenough.stillness.StillnessTimerScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BreathPhase { IN, HOLD, OUT }

/** A breathing pattern as a sequence of phases with a length in seconds each. */
data class BreathPattern(val steps: List<Pair<BreathPhase, Int>>)

val breathPatterns: Map<String, BreathPattern> = mapOf(
    "46" to BreathPattern(listOf(BreathPhase.IN to 4, BreathPhase.OUT to 6)),
    "478" to BreathPattern(listOf(BreathPhase.IN to 4, BreathPhase.HOLD to 7, BreathPhase.OUT to 8)),
    "box" to BreathPattern(
        listOf(
            BreathPhase.IN to 4,
            BreathPhase.HOLD to 4,
            BreathPhase.OUT to 4,
            BreathPhase.HOLD to 4,
        ),
    ),
)

class StillnessViewModel(
    private val appContext: Context,
    private val meditationRepository: MeditationRepository,
) : ViewModel() {

    private val scheduler = StillnessTimerScheduler(appContext)

    // Timer state.
    var durationMinutes by mutableIntStateOf(10)
        private set
    var intervalMinutes by mutableIntStateOf(0)
        private set
    var startBell by mutableStateOf(true)
        private set
    var intervalBell by mutableStateOf(false)
        private set
    var endBell by mutableStateOf(true)
        private set
    var timerRunning by mutableStateOf(false)
        private set
    var remainingSeconds by mutableIntStateOf(0)
        private set

    // Breathing state.
    var breathingRunning by mutableStateOf(false)
        private set
    var breathPhase by mutableStateOf(BreathPhase.IN)
        private set
    var breathTargetScale by mutableStateOf(0.4f)
        private set
    var breathPhaseMillis by mutableIntStateOf(4000)
        private set
    var breathPatternKey by mutableStateOf("46")
        private set

    private var timerJob: Job? = null
    private var breathingJob: Job? = null
    private var timerStartMillis = 0L
    private var breathStartMillis = 0L

    fun setDuration(minutes: Int) { if (!timerRunning) durationMinutes = minutes.coerceAtLeast(1) }
    fun setInterval(minutes: Int) { if (!timerRunning) intervalMinutes = minutes.coerceAtLeast(0) }
    fun toggleStartBell() { if (!timerRunning) startBell = !startBell }
    fun toggleIntervalBell() { if (!timerRunning) intervalBell = !intervalBell }
    fun toggleEndBell() { if (!timerRunning) endBell = !endBell }
    fun setBreathPattern(key: String) { if (!breathingRunning) breathPatternKey = key }

    fun startTimer() {
        if (timerRunning) return
        timerRunning = true
        timerStartMillis = System.currentTimeMillis()
        val totalSeconds = durationMinutes * 60
        remainingSeconds = totalSeconds

        if (endBell) {
            scheduler.scheduleEnd(
                endAtMillis = timerStartMillis + totalSeconds * 1000L,
                startTimeMillis = timerStartMillis,
                durationSeconds = totalSeconds,
            )
        }
        if (startBell) BellPlayer.play()

        timerJob = viewModelScope.launch {
            var elapsed = 0
            while (timerRunning && remainingSeconds > 0) {
                delay(1000)
                remainingSeconds -= 1
                elapsed += 1
                if (intervalBell && intervalMinutes > 0 &&
                    remainingSeconds > 0 && elapsed % (intervalMinutes * 60) == 0
                ) {
                    BellPlayer.play(durationSeconds = 1.5)
                }
            }
            // The end alarm rings the ending bell and logs the session. If the end
            // bell is off, log here instead so the session is still recorded.
            if (timerRunning && remainingSeconds == 0) {
                if (!endBell) {
                    meditationRepository.log(
                        startTimeMillis = timerStartMillis,
                        durationSeconds = totalSeconds,
                        type = SessionType.TIMER,
                        nowMillis = System.currentTimeMillis(),
                    )
                }
                timerRunning = false
            }
        }
    }

    fun stopTimer() {
        if (!timerRunning) return
        timerJob?.cancel()
        scheduler.cancel()
        val elapsed = durationMinutes * 60 - remainingSeconds
        timerRunning = false
        if (elapsed >= 15) {
            viewModelScope.launch {
                meditationRepository.log(
                    startTimeMillis = timerStartMillis,
                    durationSeconds = elapsed,
                    type = SessionType.TIMER,
                    nowMillis = System.currentTimeMillis(),
                )
            }
        }
    }

    fun startBreathing() {
        if (breathingRunning) return
        val pattern = breathPatterns[breathPatternKey] ?: return
        breathingRunning = true
        breathStartMillis = System.currentTimeMillis()
        breathingJob = viewModelScope.launch {
            while (breathingRunning) {
                for ((phase, seconds) in pattern.steps) {
                    if (!breathingRunning) break
                    breathPhase = phase
                    breathPhaseMillis = seconds * 1000
                    breathTargetScale = when (phase) {
                        BreathPhase.IN -> 1.0f
                        BreathPhase.OUT -> 0.4f
                        BreathPhase.HOLD -> breathTargetScale
                    }
                    vibrateTick()
                    delay(seconds * 1000L)
                }
            }
        }
    }

    fun stopBreathing() {
        if (!breathingRunning) return
        breathingJob?.cancel()
        breathingRunning = false
        val elapsed = ((System.currentTimeMillis() - breathStartMillis) / 1000).toInt()
        if (elapsed >= 15) {
            viewModelScope.launch {
                meditationRepository.log(
                    startTimeMillis = breathStartMillis,
                    durationSeconds = elapsed,
                    type = SessionType.BREATHING,
                    nowMillis = System.currentTimeMillis(),
                )
            }
        }
    }

    private fun vibrateTick() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(VibratorManager::class.java)
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Vibrator::class.java)
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {
            // Vibration is a nicety. Never let it crash the pacer.
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        breathingJob?.cancel()
    }
}
