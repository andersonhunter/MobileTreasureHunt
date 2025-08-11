/*Name: Hunter Anderson
* OSU ONID: andershu
* Email: andershu@oregonstate.edu */

package com.example.mobiletreasurehunt.ui.theme
import androidx.lifecycle.ViewModel
import com.example.mobiletreasurehunt.data.TreasureUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.location.Location
import com.example.mobiletreasurehunt.model.Clue
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class TreasureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TreasureUiState())
    val uiState: StateFlow<TreasureUiState> = _uiState.asStateFlow()

    fun markCompleted(clue: Clue) {
        _uiState.value.clues[_uiState.value.clues.indexOf(clue)].completed = true
        decrementCluesRemaining()
        incrementCluesCompleted()
    }

    fun getClues(): MutableList<Clue> {
        return _uiState.value.clues
    }

    fun haversine(clue: Clue? = getCurrentClue(), location: Location): Double {
        val earthRadiusKm: Double = 6372.8
        val kmToFeet: Double = 3280.84
        if (clue != null) {
            val dLat = Math.toRadians(clue.latitude - location.latitude)
            val dLon = Math.toRadians(clue.longitude - location.longitude)
            val originLat = Math.toRadians(location.latitude)
            val destinationLat = Math.toRadians(clue.latitude)
            val a = sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(destinationLat)
            val c = 2 * asin(sqrt(a))
            return earthRadiusKm * c * kmToFeet
        }
        return 5000.0
    }

    fun getCurrentClue(): Clue? {
        return _uiState.value.currentClue
    }

    fun setTimer(time: Int): Unit {
        _uiState.update { currentState ->
            currentState.copy(timer = time)
        }
    }

    fun getTimer(): Int {
        return _uiState.value.timer
    }

    private fun decrementCluesRemaining(): Unit {
        _uiState.update {currentState ->
            currentState.copy(remainingClues = currentState.remainingClues - 1)
        }
    }

    private fun incrementCluesCompleted(): Unit {
        _uiState.update {currentState ->
            currentState.copy(cluesCompleted = currentState.cluesCompleted + 1)
        }
    }

    fun getCluesRemaining(): Boolean {
        return _uiState.value.remainingClues <= 0
    }

    fun getCluesCompleted(): Int {
        return _uiState.value.cluesCompleted
    }

}