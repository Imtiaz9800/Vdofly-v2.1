package com.example

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.common.MimeTypes
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Language
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.SeekParameters
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.media3.common.PlaybackParameters
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlin.math.abs

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: VideoViewModel,
    initialIndex: Int,
    onBack: () -> Unit,
    mainActivity: MainActivity? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = (context as? ComponentActivity) ?: mainActivity
    val isActivityInPip = (activity as? MainActivity)?.isInPipModeState ?: false
    
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val autoResume by viewModel.autoResume.collectAsStateWithLifecycle()
    val seekInterval by viewModel.seekInterval.collectAsStateWithLifecycle()
    val gestureControls by viewModel.gestureControls.collectAsStateWithLifecycle()
    var currentMediaIndex by remember { mutableIntStateOf(initialIndex) }

    val loadControl = remember {
        DefaultLoadControl.Builder()
            .setBackBuffer(30_000, true) // Retain 30s buffer for instant backward seek
            .setBufferDurationsMs(
                15_000, // minBufferMs
                50_000, // maxBufferMs
                1_000,  // bufferForPlaybackMs
                2_000   // bufferForPlaybackAfterRebufferMs
            )
            .build()
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build().apply {
                val mediaItems = videos.map { 
                    MediaItem.Builder()
                        .setUri(it.uri)
                        .setMediaId(it.uri.toString())
                        .build()
                }
                setMediaItems(mediaItems, initialIndex, 0L)
                prepare()
                playWhenReady = true
            }
    }
    
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var userManualRotation by remember { mutableStateOf<Int?>(null) }
    
    var sleepTimerMinutes by remember { mutableStateOf(0) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showAudioTrackDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var tracksStateVersion by remember { mutableIntStateOf(0) }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                val subName = getSubtitleDisplayName(context, uri)
                val ext = subName.substringAfterLast('.', "").lowercase()
                val mimeType = when (ext) {
                    "srt" -> MimeTypes.APPLICATION_SUBRIP
                    "vtt" -> MimeTypes.TEXT_VTT
                    "ssa", "ass" -> MimeTypes.TEXT_SSA
                    "ttml", "xml" -> MimeTypes.APPLICATION_TTML
                    else -> MimeTypes.APPLICATION_SUBRIP
                }

                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(uri)
                    .setMimeType(mimeType)
                    .setLanguage("custom")
                    .setLabel(subName)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_FORCED)
                    .build()

                val curPos = exoPlayer.currentPosition
                val curIdx = exoPlayer.currentMediaItemIndex
                val curItem = exoPlayer.getMediaItemAt(curIdx)
                val existingConfigs = curItem.localConfiguration?.subtitleConfigurations ?: emptyList()
                val updatedConfigs = existingConfigs + subtitleConfig

                val newItem = curItem.buildUpon()
                    .setSubtitleConfigurations(updatedConfigs)
                    .build()

                exoPlayer.replaceMediaItem(curIdx, newItem)
                exoPlayer.seekTo(curIdx, curPos)

                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .build()
                tracksStateVersion++
                Toast.makeText(context, "Added subtitle: $subName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load subtitle: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(1f) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var isUnlockButtonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isScreenLocked, isUnlockButtonVisible) {
        if (isScreenLocked && isUnlockButtonVisible) {
            delay(3500L)
            isUnlockButtonVisible = false
        }
    }

    BackHandler(enabled = isScreenLocked) {
        // When screen is locked, back button reveals the unlock button
        isUnlockButtonVisible = true
    }

    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            delay(sleepTimerMinutes * 60 * 1000L)
            exoPlayer.pause()
            sleepTimerMinutes = 0
        }
    }

    LaunchedEffect(isPlaying, isSeeking) {
        if (isPlaying && !isSeeking) {
            while(true) {
                currentPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(0L)
                
                // Save position periodically
                val currentMediaItem = exoPlayer.currentMediaItem
                if (currentMediaItem != null && currentPosition > 0) {
                    viewModel.saveVideoPosition(currentMediaItem.mediaId, currentPosition)
                }

                delay(1000L)
            }
        } else if (!isSeeking) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            val currentMediaItem = exoPlayer.currentMediaItem
            if (currentMediaItem != null && currentPosition > 0) {
                viewModel.saveVideoPosition(currentMediaItem.mediaId, currentPosition)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (autoResume) {
            val initialUriStr = videos[initialIndex].uri.toString()
            val savedPos = viewModel.getVideoPosition(initialUriStr)
            if (savedPos > 0) {
                exoPlayer.seekTo(initialIndex, savedPos)
            }
        }
    }

    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying && !isSeeking) {
            delay(4500L)
            isControlsVisible = false
        }
    }

    val mainAct = activity as? MainActivity

    // Register PiP action handlers with MainActivity
    DisposableEffect(exoPlayer, mainAct) {
        if (mainAct != null) {
            mainAct.pipPlayPauseHandler = {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }
            mainAct.pipRewindHandler = {
                val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                exoPlayer.seekTo(newPos)
            }
            mainAct.pipForwardHandler = {
                val maxDur = exoPlayer.duration.coerceAtLeast(0L)
                val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(maxDur)
                exoPlayer.seekTo(newPos)
            }
        }
        onDispose {
            if (mainAct != null) {
                mainAct.pipPlayPauseHandler = null
                mainAct.pipRewindHandler = null
                mainAct.pipForwardHandler = null
            }
        }
    }

    // Keep MainActivity updated with current player aspect ratio and play state
    LaunchedEffect(isPlaying, currentMediaIndex) {
        val format = exoPlayer.videoFormat
        val rational = if (format != null && format.width > 0 && format.height > 0) {
            Rational(format.width, format.height)
        } else {
            Rational(16, 9)
        }
        mainAct?.updatePipActions(isPlaying, rational)
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaIndex = exoPlayer.currentMediaItemIndex
                userManualRotation = null
                val vSize = exoPlayer.videoSize
                if (vSize.width > 0 && vSize.height > 0) {
                    val rational = Rational(vSize.width, vSize.height)
                    mainAct?.updatePipActions(exoPlayer.isPlaying, rational)
                    if (!isActivityInPip) {
                        if (vSize.width > vSize.height) {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        } else {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        }
                    }
                }
            }
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    val rational = Rational(videoSize.width, videoSize.height)
                    mainAct?.updatePipActions(exoPlayer.isPlaying, rational)
                    if (userManualRotation == null && !isActivityInPip) {
                        if (videoSize.width > videoSize.height) {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        } else {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        }
                    }
                }
            }
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                isPlaying = isPlayingState
                val format = exoPlayer.videoFormat
                val rational = if (format != null && format.width > 0 && format.height > 0) {
                    Rational(format.width, format.height)
                } else {
                    Rational(16, 9)
                }
                mainAct?.updatePipActions(isPlayingState, rational)
            }
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                currentSpeed = playbackParameters.speed
            }
            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                tracksStateVersion++
            }
        }
        exoPlayer.addListener(listener)
        
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, it.decorView) }
        insetsController?.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController?.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Only pause if NOT currently in Picture-in-Picture mode
                    val isInPip = (activity as? MainActivity)?.isInPipModeState ?: false
                    if (!isInPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity?.isInPictureInPictureMode == true) {
                        // is in PiP, do not pause!
                    } else if (!isInPip) {
                        exoPlayer.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Only resume if was playing before
                    if (exoPlayer.playWhenReady) {
                        exoPlayer.play()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    val isInPip = (activity as? MainActivity)?.isInPipModeState ?: false
                    val activityInPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) activity?.isInPictureInPictureMode == true else false
                    if (!isInPip && !activityInPip) {
                        exoPlayer.pause()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            exoPlayer.removeListener(listener)
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
            
            // Restore system UI & Orientation
            insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Only show gestures, lock button, and controls if NOT in PiP mode
        if (!isActivityInPip) {
            // Overlay for gestures
            GestureOverlay(
                exoPlayer = exoPlayer,
                isScreenLocked = isScreenLocked,
                gestureEnabled = gestureControls,
                seekStepSeconds = seekInterval,
                onLockedTap = { isUnlockButtonVisible = !isUnlockButtonVisible },
                onTap = { isControlsVisible = !isControlsVisible }
            )

            // Floating Unlock Button when Screen is Locked
            AnimatedVisibility(
                visible = isScreenLocked && isUnlockButtonVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 24.dp)
            ) {
                Surface(
                    onClick = {
                        isScreenLocked = false
                        isControlsVisible = true
                        isUnlockButtonVisible = false
                        Toast.makeText(context, "Controls unlocked", Toast.LENGTH_SHORT).show()
                    },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.85f),
                    border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Unlock Screen",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tap to Unlock",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        if (isControlsVisible && !isScreenLocked) {
            val currentVideo = videos.getOrNull(currentMediaIndex)
            val currentTitle = currentVideo?.name ?: (exoPlayer.currentMediaItem?.mediaMetadata?.title?.toString() ?: "Playing Video")
            val currentSubtitle = buildString {
                if (currentVideo != null) {
                    append(currentVideo.resolution)
                    if (currentVideo.bucketName.isNotBlank()) {
                        append(" • ")
                        append(currentVideo.bucketName)
                    }
                }
            }

            val isSubtitlesActive = remember(tracksStateVersion, exoPlayer.trackSelectionParameters) {
                !exoPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
            }

            ControlsOverlay(
                videoTitle = currentTitle,
                videoSubtitle = currentSubtitle,
                isPlaying = isPlaying,
                sleepTimerMinutes = sleepTimerMinutes,
                isSubtitlesActive = isSubtitlesActive,
                currentPosition = currentPosition,
                duration = duration,
                currentSpeed = currentSpeed,
                seekStepSeconds = seekInterval,
                onSeek = { pos -> 
                    currentPosition = pos
                    isSeeking = true
                    exoPlayer.seekTo(pos)
                },
                onSeekFinished = { pos -> 
                    exoPlayer.seekTo(pos)
                    isSeeking = false
                },
                onRewind10 = {
                    val stepMs = seekInterval * 1000L
                    val newPos = (exoPlayer.currentPosition - stepMs).coerceAtLeast(0L)
                    exoPlayer.seekTo(newPos)
                    currentPosition = newPos
                },
                onForward10 = {
                    val stepMs = seekInterval * 1000L
                    val maxDur = exoPlayer.duration.coerceAtLeast(0L)
                    val newPos = (exoPlayer.currentPosition + stepMs).coerceAtMost(maxDur)
                    exoPlayer.seekTo(newPos)
                    currentPosition = newPos
                },
                onPlayPause = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                onNext = { exoPlayer.seekToNextMediaItem() },
                onPrev = { exoPlayer.seekToPreviousMediaItem() },
                onRotate = {
                    val currentOrientation = context.resources.configuration.orientation
                    val targetOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                    userManualRotation = targetOrientation
                    activity?.requestedOrientation = targetOrientation
                },
                onSpeed = {
                    val newSpeed = when (currentSpeed) {
                        1f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 2f
                        2f -> 0.5f
                        else -> 1f
                    }
                    exoPlayer.setPlaybackSpeed(newSpeed)
                },
                onSubtitles = { showSubtitleDialog = true },
                onAudioTrack = { showAudioTrackDialog = true },
                onSleepTimer = { showSleepTimerDialog = true },
                onPip = {
                    isControlsVisible = false
                    val format = exoPlayer.videoFormat
                    val rational = if (format != null && format.width > 0 && format.height > 0) {
                        Rational(format.width, format.height)
                    } else {
                        Rational(16, 9)
                    }
                    if (mainAct != null) {
                        mainAct.enterPipMode(exoPlayer.isPlaying, rational)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pipParams = PictureInPictureParams.Builder()
                        val ratioFloat = rational.toFloat()
                        val clampedRatio = if (ratioFloat in 0.42f..2.38f) {
                            rational
                        } else if (ratioFloat > 2.38f) {
                            Rational(238, 100)
                        } else {
                            Rational(100, 238)
                        }
                        pipParams.setAspectRatio(clampedRatio)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            pipParams.setAutoEnterEnabled(true)
                            pipParams.setSeamlessResizeEnabled(true)
                        }
                        try {
                            activity?.enterPictureInPictureMode(pipParams.build())
                        } catch (_: Exception) {}
                    }
                },
                onLockScreen = {
                    isScreenLocked = true
                    isControlsVisible = false
                    isUnlockButtonVisible = true
                    Toast.makeText(context, "Screen locked. Tap to unlock.", Toast.LENGTH_SHORT).show()
                },
                onBack = {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    onBack()
                }
            )
        }
        if (showSubtitleDialog) {
            val textGroups = exoPlayer.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
            val isSubtitlesDisabled = exoPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
            val hasManualTextOverride = !isSubtitlesDisabled && exoPlayer.trackSelectionParameters.overrides.any { it.key.type == C.TRACK_TYPE_TEXT }
            val isAutoSelected = !isSubtitlesDisabled && !hasManualTextOverride

            AlertDialog(
                onDismissRequest = { showSubtitleDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Subtitles,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Subtitles & Captions", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            Text(
                                "PLAYBACK SUBTITLES",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        // Option: Off
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                            .build()
                                        tracksStateVersion++
                                        showSubtitleDialog = false
                                        isControlsVisible = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSubtitlesDisabled,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Off", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                    Text("Disable subtitles completely", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        // Option: Auto / Default
                        if (textGroups.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                .buildUpon()
                                                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                                .build()
                                            tracksStateVersion++
                                            showSubtitleDialog = false
                                            isControlsVisible = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isAutoSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Auto (Default)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                        Text("Automatically detect and show track", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        // Embedded & Added Tracks
                        items(textGroups.size) { groupIndex ->
                            val group = textGroups[groupIndex]
                            val format = group.mediaTrackGroup.getFormat(0)
                            val label = format.label?.takeIf { it.isNotBlank() }
                                ?: format.language?.let { "Language: $it" }
                                ?: "Track ${groupIndex + 1}"
                            val mime = format.sampleMimeType?.substringAfterLast('/')?.uppercase() ?: ""
                            val isSelected = !isSubtitlesDisabled && group.isSelected

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                            .setOverrideForType(
                                                TrackSelectionOverride(group.mediaTrackGroup, listOf(0))
                                            )
                                            .build()
                                        tracksStateVersion++
                                        showSubtitleDialog = false
                                        isControlsVisible = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                    if (mime.isNotBlank() || format.language != null) {
                                        Text(
                                            listOfNotNull(format.language?.let { "Lang: $it" }, mime.takeIf { it.isNotBlank() }).joinToString(" • "),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        item {
                            Button(
                                onClick = {
                                    showSubtitleDialog = false
                                    subtitlePickerLauncher.launch(
                                        arrayOf(
                                            "text/*",
                                            "application/x-subrip",
                                            "text/vtt",
                                            "text/x-ssa",
                                            "application/octet-stream",
                                            "*/*"
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Subtitle File (.srt, .vtt, .ass)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSubtitleDialog = false }) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
        if (showSleepTimerDialog) {
            AlertDialog(
                onDismissRequest = { showSleepTimerDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                title = { Text("Sleep Timer", color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column {
                        listOf(0, 15, 30, 45, 60).forEach { mins ->
                            val label = if (mins == 0) "Off" else "$mins minutes"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sleepTimerMinutes = mins
                                        showSleepTimerDialog = false
                                        isControlsVisible = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sleepTimerMinutes == mins,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = label, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSleepTimerDialog = false }) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
        if (showAudioTrackDialog) {
            val audioGroups = exoPlayer.currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
            AlertDialog(
                onDismissRequest = { showAudioTrackDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                title = { Text("Select Audio Track", color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    LazyColumn {
                        if (audioGroups.isEmpty()) {
                            item {
                                Text(
                                    text = "No additional audio tracks found.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        items(audioGroups.size) { groupIndex ->
                            val group = audioGroups[groupIndex]
                            val format = group.mediaTrackGroup.getFormat(0)
                            val language = format.language ?: "Unknown"
                            val isSelected = group.isSelected
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .setOverrideForType(
                                                TrackSelectionOverride(group.mediaTrackGroup, listOf(0))
                                            )
                                            .build()
                                        showAudioTrackDialog = false
                                        isControlsVisible = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Track ${groupIndex + 1}: $language", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAudioTrackDialog = false }) { Text("Close", color = MaterialTheme.colorScheme.primary) }
                }
            )
        }
    }
}


@Composable
fun ControlsOverlay(
    videoTitle: String,
    videoSubtitle: String,
    isPlaying: Boolean,
    sleepTimerMinutes: Int,
    isSubtitlesActive: Boolean,
    currentPosition: Long,
    duration: Long,
    currentSpeed: Float,
    seekStepSeconds: Int = 10,
    onSeek: (Long) -> Unit,
    onSeekFinished: (Long) -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onRotate: () -> Unit,
    onSpeed: () -> Unit,
    onSubtitles: () -> Unit,
    onAudioTrack: () -> Unit,
    onSleepTimer: () -> Unit,
    onPip: () -> Unit,
    onLockScreen: () -> Unit = {},
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableLongStateOf(0L) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
        // Top Header Bar with File Name and Action Controls
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = videoTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (videoSubtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "HW+",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = videoSubtitle,
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSubtitles) {
                        Icon(
                            imageVector = if (isSubtitlesActive) Icons.Filled.Subtitles else Icons.Filled.SubtitlesOff,
                            contentDescription = "Subtitles",
                            tint = if (isSubtitlesActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onAudioTrack) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = "Audio Track",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onSleepTimer) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerMinutes > 0) MaterialTheme.colorScheme.primary else Color.White
                            )
                            if (sleepTimerMinutes > 0) {
                                Text(
                                    text = "$sleepTimerMinutes",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                                        .padding(horizontal = 2.dp)
                                    )
                            }
                        }
                    }
                    IconButton(onClick = onSpeed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = "Playback Speed",
                                tint = Color.White
                            )
                            Text(
                                text = "${currentSpeed}x",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        IconButton(onClick = onPip) {
                            Icon(
                                imageVector = Icons.Filled.PictureInPictureAlt,
                                contentDescription = "Picture in Picture",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = onLockScreen) {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = "Lock Screen Controls",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // Center Controls with Previous, -10s, Play/Pause, +10s, Next
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPrev, modifier = Modifier.size(52.dp)) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRewind10()
                },
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "Rewind $seekStepSeconds seconds",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onPlayPause, modifier = Modifier.size(76.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onForward10()
                },
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "Forward $seekStepSeconds seconds",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNext, modifier = Modifier.size(52.dp)) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        // Bottom timeline bar with real-time scrub feedback & timestamp tooltip
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp, start = 16.dp, end = 16.dp)
                .navigationBarsPadding()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isScrubbing) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "${formatTime(scrubPositionMs)} / ${formatTime(duration)}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            val displayPos = if (isScrubbing) scrubPositionMs else currentPosition
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(displayPos),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = if (duration > 0) (displayPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                    onValueChange = { frac ->
                        isScrubbing = true
                        val targetMs = (frac * duration).toLong()
                        scrubPositionMs = targetMs
                        onSeek(targetMs)
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        onSeekFinished(scrubPositionMs)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                    )
                )
                Text(
                    text = formatTime(duration),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        IconButton(
            onClick = onRotate,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).navigationBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Filled.ScreenRotation,
                contentDescription = "Rotate",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

enum class DragType { NONE, HORIZONTAL_SEEK, VERTICAL_BRIGHTNESS, VERTICAL_VOLUME }

@Composable
fun GestureOverlay(
    exoPlayer: ExoPlayer,
    isScreenLocked: Boolean = false,
    gestureEnabled: Boolean = true,
    seekStepSeconds: Int = 10,
    onLockedTap: () -> Unit = {},
    onTap: () -> Unit
) {
    if (isScreenLocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onLockedTap() }
                    )
                }
        )
        return
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    var volumeLevel by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
    
    val activity = context as? ComponentActivity
    var brightnessLevel by remember { 
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness.let { if (it == null || it < 0) 0.5f else it }
        ) 
    }

    var showOverlay by remember { mutableStateOf(false) }
    var dragType by remember { mutableStateOf(DragType.NONE) }
    var startPositionMs by remember { mutableLongStateOf(0L) }
    var seekPositionMs by remember { mutableLongStateOf(0L) }
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var overlaySeekDelta by remember { mutableStateOf("") }
    var lastHapticStep by remember { mutableIntStateOf(0) }
    var lastThrottledSeekTime by remember { mutableLongStateOf(0L) }

    // Double tap multi-tap accumulator state
    var doubleTapLeft by remember { mutableStateOf(false) }
    var doubleTapRight by remember { mutableStateOf(false) }
    var accumulatedSecondsLeft by remember { mutableIntStateOf(seekStepSeconds) }
    var accumulatedSecondsRight by remember { mutableIntStateOf(seekStepSeconds) }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }
    
    val rippleAlphaLeft by animateFloatAsState(
        targetValue = if (doubleTapLeft) 0.35f else 0f,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "rippleAlphaLeft"
    )

    val rippleAlphaRight by animateFloatAsState(
        targetValue = if (doubleTapRight) 0.35f else 0f,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "rippleAlphaRight"
    )

    LaunchedEffect(doubleTapLeft) {
        if (doubleTapLeft) {
            delay(750)
            doubleTapLeft = false
            accumulatedSecondsLeft = seekStepSeconds
        }
    }

    LaunchedEffect(doubleTapRight) {
        if (doubleTapRight) {
            delay(750)
            doubleTapRight = false
            accumulatedSecondsRight = seekStepSeconds
        }
    }

    LaunchedEffect(showOverlay, dragType) {
        if (showOverlay && dragType == DragType.NONE) {
            delay(800)
            showOverlay = false
        }
    }

    val stepMs = seekStepSeconds * 1000L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(seekStepSeconds, gestureEnabled) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset ->
                        if (!gestureEnabled) {
                            onTap()
                            return@detectTapGestures
                        }
                        val now = System.currentTimeMillis()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (offset.x < size.width / 2) {
                            if (doubleTapLeft && (now - lastTapTimestamp) < 800) {
                                accumulatedSecondsLeft += seekStepSeconds
                            } else {
                                accumulatedSecondsLeft = seekStepSeconds
                            }
                            doubleTapLeft = true
                            doubleTapRight = false
                            lastTapTimestamp = now
                            val newPos = (exoPlayer.currentPosition - stepMs).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                        } else {
                            if (doubleTapRight && (now - lastTapTimestamp) < 800) {
                                accumulatedSecondsRight += seekStepSeconds
                            } else {
                                accumulatedSecondsRight = seekStepSeconds
                            }
                            doubleTapRight = true
                            doubleTapLeft = false
                            lastTapTimestamp = now
                            val duration = exoPlayer.duration.coerceAtLeast(0L)
                            val newPos = (exoPlayer.currentPosition + stepMs).coerceAtMost(duration)
                            exoPlayer.seekTo(newPos)
                        }
                    }
                )
            }
            .pointerInput(gestureEnabled) {
                if (!gestureEnabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { _ ->
                        dragType = DragType.NONE
                        totalDragX = 0f
                        startPositionMs = exoPlayer.currentPosition
                        seekPositionMs = exoPlayer.currentPosition
                        lastHapticStep = 0
                        lastThrottledSeekTime = 0L
                    },
                    onDragEnd = {
                        if (dragType == DragType.HORIZONTAL_SEEK) {
                            exoPlayer.seekTo(seekPositionMs)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        dragType = DragType.NONE
                    },
                    onDragCancel = {
                        dragType = DragType.NONE
                        showOverlay = false
                    }
                ) { change, dragAmount ->
                    change.consume()
                    if (dragType == DragType.NONE) {
                        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y) && kotlin.math.abs(dragAmount.x) > 6f) {
                            dragType = DragType.HORIZONTAL_SEEK
                            startPositionMs = exoPlayer.currentPosition
                            seekPositionMs = startPositionMs
                            totalDragX = 0f
                            lastHapticStep = 0
                        } else if (kotlin.math.abs(dragAmount.y) > 6f) {
                            if (change.position.x < size.width / 2) {
                                dragType = DragType.VERTICAL_BRIGHTNESS
                            } else {
                                dragType = DragType.VERTICAL_VOLUME
                            }
                        }
                    }

                    when (dragType) {
                        DragType.HORIZONTAL_SEEK -> {
                            totalDragX += dragAmount.x
                            // Dynamic adaptive sensitivity: fine control for small swipes, rapid scrubbing for large swipes
                            val absX = kotlin.math.abs(totalDragX)
                            val sensitivity = if (absX < 250f) 25f else if (absX < 600f) 40f else 60f
                            val seekDeltaMs = (totalDragX * sensitivity).toLong()
                            val duration = exoPlayer.duration.coerceAtLeast(0L)
                            seekPositionMs = (startPositionMs + seekDeltaMs).coerceIn(0L, duration)
                            
                            val deltaSec = (seekPositionMs - startPositionMs) / 1000
                            val sign = if (deltaSec >= 0) "+" else ""
                            overlaySeekDelta = "$sign${deltaSec}s"
                            showOverlay = true

                            // Live seek preview update (throttled to every 50ms for buttery-smooth performance)
                            val now = System.currentTimeMillis()
                            if (now - lastThrottledSeekTime > 50) {
                                exoPlayer.seekTo(seekPositionMs)
                                lastThrottledSeekTime = now
                            }

                            // Haptic pulse when crossing each 5-second delta milestone
                            val step = (deltaSec / 5).toInt()
                            if (step != lastHapticStep) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastHapticStep = step
                            }
                        }
                        DragType.VERTICAL_BRIGHTNESS -> {
                            val diff = dragAmount.y / 600f
                            brightnessLevel = (brightnessLevel - diff).coerceIn(0.01f, 1f)
                            activity?.window?.attributes = activity?.window?.attributes?.apply {
                                screenBrightness = brightnessLevel
                            }
                            showOverlay = true
                        }
                        DragType.VERTICAL_VOLUME -> {
                            val diff = (dragAmount.y / 400f) * maxVolume
                            volumeLevel = (volumeLevel - diff).coerceIn(0f, maxVolume.toFloat())
                            audioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                volumeLevel.toInt(),
                                0
                            )
                            showOverlay = true
                        }
                        else -> {}
                    }
                }
            }
    )

    // Double Tap Ripple Overlay with accumulated seconds counter and animated chevrons
    if (rippleAlphaLeft > 0f || rippleAlphaRight > 0f) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (doubleTapLeft) Color.White.copy(alpha = rippleAlphaLeft) else Color.Transparent,
                        shape = CircleShape.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (doubleTapLeft) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FastRewind,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-$accumulatedSecondsLeft Seconds",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (doubleTapRight) Color.White.copy(alpha = rippleAlphaRight) else Color.Transparent,
                        shape = CircleShape.copy(topEnd = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (doubleTapRight) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+$accumulatedSecondsRight Seconds",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showOverlay) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.82f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (dragType) {
                        DragType.HORIZONTAL_SEEK -> {
                            val isForward = seekPositionMs >= startPositionMs
                            Icon(
                                imageVector = if (isForward) Icons.Filled.FastForward else Icons.Filled.FastRewind,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatTime(seekPositionMs),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " / ${formatTime(exoPlayer.duration.coerceAtLeast(0L))}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (overlaySeekDelta.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isForward) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "[$overlaySeekDelta]",
                                        color = if (isForward) MaterialTheme.colorScheme.primary else Color(0xFFFF6B6B),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val dur = exoPlayer.duration.coerceAtLeast(1L)
                            val seekProgress = (seekPositionMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { seekProgress },
                                modifier = Modifier.width(160.dp).height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.25f)
                            )
                        }
                        DragType.VERTICAL_BRIGHTNESS -> {
                            Icon(
                                imageVector = Icons.Filled.BrightnessHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(brightnessLevel * 100).toInt()}%",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { brightnessLevel },
                                modifier = Modifier.width(100.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                        DragType.VERTICAL_VOLUME -> {
                            val volPct = (volumeLevel / maxVolume).coerceIn(0f, 1f)
                            Icon(
                                imageVector = if (volPct > 0f) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(volPct * 100).toInt()}%",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { volPct },
                                modifier = Modifier.width(100.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}


