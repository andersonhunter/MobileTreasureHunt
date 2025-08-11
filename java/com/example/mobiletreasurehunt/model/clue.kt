/*Name: Hunter Anderson
* OSU ONID: andershu
* Email: andershu@oregonstate.edu */

package com.example.mobiletreasurehunt.model

data class Clue(
    var name: String,
    var details: String,
    var description: String,
    var hint: String,
    var latitude: Double,
    var longitude: Double,
    var completed: Boolean = false
)
