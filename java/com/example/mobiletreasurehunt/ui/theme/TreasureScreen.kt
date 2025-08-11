/*Name: Hunter Anderson
* OSU ONID: andershu
* Email: andershu@oregonstate.edu */

package com.example.mobiletreasurehunt.ui.theme

import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mobiletreasurehunt.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mobiletreasurehunt.model.Clue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.coroutines.resume
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

enum class TreasureScreen{
    Permissions,
    Start,
    Clues,
    ClueSolved
}

suspend fun getCurrentFineLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context
): Location? = suspendCoroutine { cont ->
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
    ) {
        cont.resume(null)
        return@suspendCoroutine
    }
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    )
        .addOnSuccessListener { location: Location? ->
            cont.resume(location)
        }
        .addOnFailureListener {
            cont.resume(null)
        }
}

@Composable
fun PermissionsScreen(
    permissionGranted: MutableState<Boolean>,
    permissionRequest: () -> Unit = {},
    onContinue: () -> Unit = {},
    modifier: Modifier
) {
    if(permissionGranted.value) {
        onContinue()
    }
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text (
            text = stringResource(R.string.appName),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text (stringResource(R.string.permissions))
        Button(onClick = permissionRequest) {Text(stringResource(R.string.accept))}
    }
}

@Composable
fun StartScreen(
    onStartButtonClicked: () -> Unit,
    modifier: Modifier
) {
    LazyColumn (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        item {Text (
            text = stringResource(R.string.appName),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )}
        item {
            Text(text = stringResource(R.string.rules))
            Button(onClick = onStartButtonClicked) {
                Text(stringResource(R.string.start))
            }
        }

    }
}

@Composable
fun ClueItem(
    clue: Clue,
    onFoundButtonPressed: (Clue, (Boolean) -> Unit) -> Unit,
    viewModel: TreasureViewModel,
    clueNumber: Int,
    modifier: Modifier
) {
    val showHint = remember{ mutableStateOf(false) }
    if(showHint.value) {
        Dialog(onDismissRequest = {showHint.value = false}) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                .background(Color.White)) {
                Column (Modifier.padding(16.dp)){
                    Text(clue.hint, Modifier.padding(8.dp))
                    Button(onClick = {showHint.value = false}) {Text(stringResource(R.string.dismiss))}
                }
            }
        }
    }
    val locationWrong = remember{ mutableStateOf(false) }
    if(locationWrong.value) {
        Dialog(onDismissRequest = {
            locationWrong.value = false
        }) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                .background(Color.White)) {
                Column (Modifier.padding(16.dp)){
                    Text(stringResource(R.string.clueNum) +" ${clueNumber + 1}")
                    Text(stringResource(R.string.wrong), Modifier.padding(8.dp))
                    Button(onClick = {locationWrong.value = false}) {Text(stringResource(R.string.dismiss))}
                }
            }
        }
    }
    val findingLocation = remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(stringResource(R.string.clueNum) + " ${clueNumber + 1}:")
        Text(clue.description)
        Row {
            Button(onClick = {showHint.value = true}) {
                Text(stringResource(R.string.hint))
            }
            Button(onClick = {
                findingLocation.value = true
                onFoundButtonPressed(clue) {isTooFar ->
                    findingLocation.value = !isTooFar
                    locationWrong.value = isTooFar
                }
                viewModel.uiState.value.currentClue = clue
            }) {
                Text(stringResource(R.string.found))
            }
        }
        if(findingLocation.value) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp)
            )
        }
    }
}

@Composable
fun ClueScreen(
    clues: List<Clue>,
    onFoundButtonPressed: (Clue, (Boolean) -> Unit) -> Unit,
    viewModel: TreasureViewModel,
    getTimer: () -> Int,
    setTimer: (Int) -> Unit,
    modifier: Modifier
) {
    var ticks by remember {mutableIntStateOf(getTimer())}
    val timerTicking = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while(timerTicking.value) {
            delay(1.seconds)
            ticks++
            setTimer(ticks)
        }
    }
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.timer) + " ${ticks.seconds}",
                color = Color.Red
            )
        }
        item {Text (
            text = stringResource(R.string.appName),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )}
        item {
            ClueItem(
                onFoundButtonPressed = onFoundButtonPressed,
                clue = clues[viewModel.getCluesCompleted()],
                viewModel = viewModel,
                clueNumber = viewModel.getCluesCompleted(),
                modifier = modifier
                )
        }
    }
}

@Composable
fun ClueSolved(
    onContinueButtonPressed: () -> Unit,
    currentClue: Clue?,
    getTimer: () -> Int,
    getRemainingClues: () -> Boolean,
    onLastClue: () -> Unit,
    modifier: Modifier = Modifier
) {
    if(getRemainingClues()) {
        Column(modifier = modifier) {
            Text (
                text = stringResource(R.string.appName),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.congrats))
            if (currentClue != null) {
                Text(currentClue.name)
                Text(currentClue.details)
            }
            Text(
                text = stringResource(R.string.finalTime) + " ${getTimer().seconds}",
                color = Color.Green
                )
            Button(onClick = onLastClue) {Text(stringResource(R.string.home))}
        }
    }
    else {
        Column(modifier = modifier) {
            Text (
                text = stringResource(R.string.appName),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.elapsed) + " ${getTimer()}",
                color = Color.Red
            )
            if (currentClue != null) {
                Text(currentClue.name)
                Text(currentClue.details)
            }
            Button(onClick = onContinueButtonPressed) {Text(stringResource(R.string.cont))}
        }
    }
}

@Composable
fun MobileTreasureScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: TreasureViewModel = viewModel(),
    permissionGranted: MutableState<Boolean>,
    fusedLocationClient: FusedLocationProviderClient,
    location: MutableState<Location?>,
    permissionRequest: () -> Unit,
    modifier: Modifier = Modifier
        .systemBarsPadding()
        .fillMaxWidth()
        .padding(16.dp)
) {
    NavHost(
        navController = navController,
        startDestination = TreasureScreen.Permissions.name
    ) {
        composable(route = TreasureScreen.Permissions.name) {
            PermissionsScreen(
                onContinue = {navController.navigate(TreasureScreen.Start.name)},
                permissionRequest = permissionRequest,
                permissionGranted = permissionGranted,
                modifier = modifier
            )
        }
        composable(route = TreasureScreen.Start.name) {
            StartScreen(
                onStartButtonClicked = {navController.navigate(TreasureScreen.Clues.name)},
                modifier = modifier
            )
        }
        composable(route = TreasureScreen.Clues.name) {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            ClueScreen(
                clues = viewModel.getClues(),
                onFoundButtonPressed = { clue, onDistanceChecked ->
                    coroutineScope.launch {
                        val loc = getCurrentFineLocation(fusedLocationClient, context)
                        if (loc != null) {
                            location.value = loc
                            val distance: Double = viewModel.haversine(location = location.value!!)
                            println("Distance = $distance")
                            if (distance <= 1016.5) {
                                viewModel.markCompleted(clue)
                                onDistanceChecked(false)
                                navController.navigate(TreasureScreen.ClueSolved.name)
                            }
                            else {
                                onDistanceChecked(true)
                            }
                        }
                        else {
                            onDistanceChecked(true)
                        }
                    }
                },
                viewModel = viewModel,
                getTimer = { viewModel.getTimer() },
                setTimer = { viewModel.setTimer(it) },
                modifier = modifier
            )
        }

        composable(route = TreasureScreen.ClueSolved.name) {
            ClueSolved(
                onContinueButtonPressed = { navController.navigate(TreasureScreen.Clues.name)},
                currentClue = viewModel.getCurrentClue(),
                getTimer = { viewModel.getTimer() },
                getRemainingClues = { viewModel.getCluesRemaining() },
                onLastClue = {navController.navigate(TreasureScreen.Start.name)},
                modifier = modifier
            )
        }
    }
}

