package com.example

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_PIP_PLAY_PAUSE = "com.example.PIP_PLAY_PAUSE"
        const val ACTION_PIP_REWIND = "com.example.PIP_REWIND"
        const val ACTION_PIP_FORWARD = "com.example.PIP_FORWARD"
        const val EXTRA_IS_PLAYING = "is_playing"
    }

    var pipPlayPauseHandler: (() -> Unit)? = null
    var pipRewindHandler: (() -> Unit)? = null
    var pipForwardHandler: (() -> Unit)? = null
    var isCurrentlyPlayingInPip: Boolean = true
    var currentAspectRatio: Rational = Rational(16, 9)

    var isInPipModeState by mutableStateOf(false)
        private set

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PIP_PLAY_PAUSE -> pipPlayPauseHandler?.invoke()
                ACTION_PIP_REWIND -> pipRewindHandler?.invoke()
                ACTION_PIP_FORWARD -> pipForwardHandler?.invoke()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filter = IntentFilter().apply {
            addAction(ACTION_PIP_PLAY_PAUSE)
            addAction(ACTION_PIP_REWIND)
            addAction(ACTION_PIP_FORWARD)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pipReceiver, filter)
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    VdoflyApp(mainActivity = this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(pipReceiver)
        } catch (_: Exception) {}
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        isInPipModeState = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // If playing video, seamlessly transition to PiP
        if (pipPlayPauseHandler != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPipMode(isCurrentlyPlayingInPip, currentAspectRatio)
        }
    }

    fun updatePipActions(isPlaying: Boolean, aspectRatio: Rational = currentAspectRatio) {
        isCurrentlyPlayingInPip = isPlaying
        currentAspectRatio = aspectRatio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = buildPipParams(isPlaying, aspectRatio)
            try {
                setPictureInPictureParams(params)
            } catch (_: Exception) {}
        }
    }

    fun enterPipMode(isPlaying: Boolean, aspectRatio: Rational = currentAspectRatio) {
        isCurrentlyPlayingInPip = isPlaying
        currentAspectRatio = aspectRatio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = buildPipParams(isPlaying, aspectRatio)
            try {
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    private fun buildPipParams(isPlaying: Boolean, aspectRatio: Rational): PictureInPictureParams {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw UnsupportedOperationException()
        }
        val builder = PictureInPictureParams.Builder()
        
        // Ensure aspect ratio is valid (between 0.418410 and 2.390000 per Android OS requirements)
        val ratioFloat = aspectRatio.toFloat()
        val clampedRatio = if (ratioFloat in 0.42f..2.38f) {
            aspectRatio
        } else if (ratioFloat > 2.38f) {
            Rational(238, 100)
        } else {
            Rational(100, 238)
        }
        builder.setAspectRatio(clampedRatio)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(true)
            builder.setSeamlessResizeEnabled(true)
        }

        // Add Rewind, Play/Pause, and Forward actions
        val actions = mutableListOf<RemoteAction>()

        // Rewind 10s
        val rewindIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent(ACTION_PIP_REWIND).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val rewindIcon = Icon.createWithResource(this, R.drawable.ic_pip_replay10)
        actions.add(RemoteAction(rewindIcon, "Rewind 10s", "Rewind 10s", rewindIntent))

        // Play / Pause
        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            2,
            Intent(ACTION_PIP_PLAY_PAUSE).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIcon = if (isPlaying) {
            Icon.createWithResource(this, R.drawable.ic_pip_pause)
        } else {
            Icon.createWithResource(this, R.drawable.ic_pip_play)
        }
        val playPauseTitle = if (isPlaying) "Pause" else "Play"
        actions.add(RemoteAction(playPauseIcon, playPauseTitle, playPauseTitle, playPauseIntent))

        // Forward 10s
        val forwardIntent = PendingIntent.getBroadcast(
            this,
            3,
            Intent(ACTION_PIP_FORWARD).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val forwardIcon = Icon.createWithResource(this, R.drawable.ic_pip_forward10)
        actions.add(RemoteAction(forwardIcon, "Forward 10s", "Forward 10s", forwardIntent))

        builder.setActions(actions)
        return builder.build()
    }
}

@Composable
fun VdoflyApp(mainActivity: MainActivity? = null) {
    val navController = rememberNavController()
    val viewModel: VideoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            VideoScreen(viewModel = viewModel, onVideoSelected = { index ->
                navController.navigate("player/$index")
            })
        }
        composable(
            "player/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            PlayerScreen(
                viewModel = viewModel,
                initialIndex = index,
                onBack = { navController.popBackStack() },
                mainActivity = mainActivity
            )
        }
    }
}


