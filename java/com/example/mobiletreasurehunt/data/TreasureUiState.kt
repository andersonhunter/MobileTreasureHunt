/*Name: Hunter Anderson
* OSU ONID: andershu
* Email: andershu@oregonstate.edu */

package com.example.mobiletreasurehunt.data

import com.example.mobiletreasurehunt.model.Clue

data class TreasureUiState(
    var currentClue: Clue? = null,
    val clues: MutableList<Clue> = mutableListOf(
        Clue(
            name = "Smoothie King",
            description = "Pay your respects to the refreshing king of Route 211",
            hint = "This king sure is... smooth",
            details = "" +
                    "The go-to spot for healthy smoothies! " +
                    "If you're thirsty, grab yourself a drink at this New York stable chain " +
                    "and fuel up for your next clue.",
            latitude = 41.4560,
            longitude = -74.3978
        ),
        Clue(
            name = "Orange County Speedway",
            description = "Round and round they go, when they stop, you'll finally hear silence",
            hint = "Look for the speediest spot in all of Middletown",
            details = "" +
                    "Orange county's own speedway, " +
                    "home of the fastest races this side of the Hudson. " +
                    "The track is 1.006km around, and was built in 1857 for horse racing. " +
                    "Now used for car racing since 1919.",
            latitude = 41.4478,
            longitude = -74.3938
        )
    ),
    var timer: Int = 0,
    var remainingClues: Int = clues.size,
    var cluesCompleted: Int = 0
)
