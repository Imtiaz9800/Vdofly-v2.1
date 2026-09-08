package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

@Composable
fun SettingsContent(
    viewModel: VideoViewModel,
    storageStats: StorageStats,
    totalVideosCount: Int,
    onChangePin: () -> Unit
) {
    val decoderMode by viewModel.decoderMode.collectAsStateWithLifecycle()
    val autoResume by viewModel.autoResume.collectAsStateWithLifecycle()
    val backgroundPlay by viewModel.backgroundPlay.collectAsStateWithLifecycle()
    val seekInterval by viewModel.seekInterval.collectAsStateWithLifecycle()
    val gestureControls by viewModel.gestureControls.collectAsStateWithLifecycle()
    val volumeBoost by viewModel.volumeBoost.collectAsStateWithLifecycle()
    val subtitleAutoLoad by viewModel.subtitleAutoLoad.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val vaultPin by viewModel.vaultPin.collectAsStateWithLifecycle()
    val hiddenVideosCount by viewModel.hiddenVideosCount.collectAsStateWithLifecycle()

    var showTutorialPage by remember { mutableStateOf(false) }
    var showAboutPage by remember { mutableStateOf(false) }
    var showDeveloperPage by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    if (showTutorialPage) {
        BackHandler { showTutorialPage = false }
        TutorialScreen(onBack = { showTutorialPage = false })
        return
    }

    if (showAboutPage) {
        BackHandler { showAboutPage = false }
        AboutScreen(
            storageStats = storageStats,
            totalVideosCount = totalVideosCount,
            onRescan = { viewModel.refreshLibrary() },
            onBack = { showAboutPage = false }
        )
        return
    }

    if (showDeveloperPage) {
        BackHandler { showDeveloperPage = false }
        DeveloperScreen(onBack = { showDeveloperPage = false })
        return
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = SurfaceContainerDark,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Clear Playback History?",
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    "This will reset all saved playback resume positions and remove items from the Recent history tab. Your physical video files will not be deleted.",
                    color = OnSurfaceVariantDark,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearPlaybackHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearHistoryDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = OnSurfaceVariantDark)
                }
            }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            containerColor = SurfaceContainerDark,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerCyan.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = PrimaryCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Clear Thumbnail Cache?",
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    "This will clean cached video thumbnails and trigger a full rescan of your media library to refresh all metadata and preview posters.",
                    color = OnSurfaceVariantDark,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearThumbnailCache()
                        showClearCacheDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = OnPrimaryCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear & Rescan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCacheDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = OnSurfaceVariantDark)
                }
            }
        )
    }

    val spinTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainerDark
                ),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Settings & Preferences",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Customize decoding, gestures, storage & security",
                            color = OnSurfaceVariantDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerCyan.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Playback & Decoder Engine
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Memory,
                title = "PLAYBACK & DECODER ENGINE",
                subtitle = "Hardware acceleration and performance options"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Video Decoder Mode",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                when (decoderMode) {
                                    DecoderMode.HW_PLUS -> "HW+: High-efficiency custom hardware acceleration with smooth frame sync"
                                    DecoderMode.HW -> "HW: Standard Android system hardware acceleration"
                                    DecoderMode.SW -> "SW: Software CPU decoder for maximum codec compatibility"
                                },
                                color = OnSurfaceVariantDark,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DecoderOptionChip(
                            title = "HW+",
                            subtitle = "Hardware+",
                            isSelected = decoderMode == DecoderMode.HW_PLUS,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDecoderMode(DecoderMode.HW_PLUS) }
                        )
                        DecoderOptionChip(
                            title = "HW",
                            subtitle = "Hardware",
                            isSelected = decoderMode == DecoderMode.HW,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDecoderMode(DecoderMode.HW) }
                        )
                        DecoderOptionChip(
                            title = "SW",
                            subtitle = "Software",
                            isSelected = decoderMode == DecoderMode.SW,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDecoderMode(DecoderMode.SW) }
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingSwitchRow(
                        icon = Icons.Default.History,
                        title = "Auto-Resume Playback",
                        subtitle = "Remember exact position and resume where you left off",
                        isChecked = autoResume,
                        onCheckedChange = { viewModel.setAutoResume(it) }
                    )

                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.VolumeUp,
                        title = "200% Volume Boost",
                        subtitle = "Allow volume slider to boost quiet audio tracks up to double",
                        isChecked = volumeBoost,
                        onCheckedChange = { viewModel.setVolumeBoost(it) }
                    )

                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.PictureInPicture,
                        title = "Background Audio Playback",
                        subtitle = "Continue playing audio when app is minimized or screen is locked",
                        isChecked = backgroundPlay,
                        onCheckedChange = { viewModel.setBackgroundPlay(it) }
                    )
                }
            }
        }

        // Section 2: Gestures & Seeking
        item {
            SettingsSectionHeader(
                icon = Icons.Default.TouchApp,
                title = "GESTURES & CONTROLS",
                subtitle = "Touch gestures, double-tap seek & subtitles"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingSwitchRow(
                        icon = Icons.Default.Swipe,
                        title = "Touch Screen Gestures",
                        subtitle = "Left swipe brightness, right swipe volume, center scrub",
                        isChecked = gestureControls,
                        onCheckedChange = { viewModel.setGestureControls(it) }
                    )

                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.Subtitles,
                        title = "Auto-Detect Subtitles",
                        subtitle = "Automatically scan and load embedded & local .srt subtitles",
                        isChecked = subtitleAutoLoad,
                        onCheckedChange = { viewModel.setSubtitleAutoLoad(it) }
                    )

                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Double-Tap Seek Jump", color = OnSurfaceDark, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Fast forward / rewind step duration", color = OnSurfaceVariantDark, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30).forEach { seconds ->
                                val isSelected = seekInterval == seconds
                                Surface(
                                    onClick = { viewModel.setSeekInterval(seconds) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryCyan else SurfaceContainerHighestDark,
                                    border = if (isSelected) null else BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 9.dp)
                                    ) {
                                        Text(
                                            "${seconds}s",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) OnPrimaryCyan else OnSurfaceDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Privacy & Security
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Security,
                title = "PRIVACY & SECURITY",
                subtitle = "Private Safe folder & security PIN"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TertiaryContainerAmber.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (vaultPin.isNullOrEmpty()) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = TertiaryAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Private Safe Folder",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (vaultPin.isNullOrEmpty()) "No PIN set • Tap to secure files"
                                else "Protected with 4-digit PIN • $hiddenVideosCount items secured",
                                color = if (vaultPin.isNullOrEmpty()) TertiaryAmber else OnSurfaceVariantDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = onChangePin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TertiaryAmber,
                            contentColor = OnTertiaryAmber
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (vaultPin.isNullOrEmpty()) "Set PIN" else "Change",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 4: Storage & Maintenance
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Storage,
                title = "STORAGE & MAINTENANCE",
                subtitle = "Device memory, thumbnails & library cache"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Device Storage",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "${String.format("%.1f", storageStats.usedGb)} GB used of ${String.format("%.1f", storageStats.totalGb)} GB",
                                color = OnSurfaceVariantDark,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryContainerCyan.copy(alpha = 0.25f),
                            contentColor = PrimaryCyan
                        ) {
                            Text(
                                "$totalVideosCount Videos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segmented storage bar
                    val total = storageStats.totalGb.coerceAtLeast(1f)
                    val videoFrac = (storageStats.videoMediaGb / total).coerceIn(0.02f, 1f)
                    val systemFrac = (storageStats.systemGb / total).coerceIn(0.02f, 1f)
                    val otherFrac = (storageStats.otherGb / total).coerceIn(0.02f, 1f)
                    val freeFrac = ((storageStats.totalGb - storageStats.usedGb).coerceAtLeast(0f) / total).coerceIn(0.02f, 1f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(modifier = Modifier.weight(videoFrac).fillMaxHeight().background(PrimaryCyan))
                        Box(modifier = Modifier.weight(systemFrac).fillMaxHeight().background(TertiaryAmber))
                        Box(modifier = Modifier.weight(otherFrac).fillMaxHeight().background(Color(0xFF9C27B0)))
                        Box(modifier = Modifier.weight(freeFrac).fillMaxHeight().background(OutlineVariantDark))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StorageLegend(color = PrimaryCyan, label = "Videos: ${String.format("%.1f", storageStats.videoMediaGb)} GB")
                        StorageLegend(color = TertiaryAmber, label = "System: ${String.format("%.1f", storageStats.systemGb)} GB")
                        StorageLegend(color = Color(0xFF9C27B0), label = "Other: ${String.format("%.1f", storageStats.otherGb)} GB")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.refreshLibrary() },
                            enabled = !isScanning,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .then(if (isScanning) Modifier.rotate(spinAngle) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isScanning) "Scanning..." else "Rescan Media", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { showClearCacheDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OutlineVariantDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Cache", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Danger Zone: Clear History
        item {
            Card(
                onClick = { showClearHistoryDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Clear Playback History",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Reset watch progress & clear Recent video list",
                                color = OnSurfaceVariantDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Section 5: Help & Information
        item {
            SettingsSectionHeader(
                icon = Icons.Default.HelpOutline,
                title = "HELP & ABOUT",
                subtitle = "User manual, developer info & version details"
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                border = BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsNavRow(
                        icon = Icons.Default.MenuBook,
                        iconBg = PrimaryContainerCyan.copy(alpha = 0.25f),
                        iconTint = PrimaryCyan,
                        title = "App Tutorial & Gesture Guide",
                        subtitle = "Master touch gestures, subtitles, PiP & shortcuts",
                        onClick = { showTutorialPage = true }
                    )

                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsNavRow(
                        icon = Icons.Default.Info,
                        iconBg = PrimaryContainerCyan.copy(alpha = 0.25f),
                        iconTint = PrimaryCyan,
                        title = "About VDOFLY Player",
                        subtitle = "Version 1.0.0 Pro • Codec specs & engine details",
                        onClick = { showAboutPage = true }
                    )

                    HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsNavRow(
                        icon = Icons.Default.Favorite,
                        iconBg = TertiaryContainerAmber.copy(alpha = 0.25f),
                        iconTint = TertiaryAmber,
                        title = "About Developer",
                        subtitle = "Made with love by Imtiaz Haque",
                        onClick = { showDeveloperPage = true }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = PrimaryCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun DecoderOptionChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryContainerCyan else SurfaceContainerHighestDark,
        border = if (isSelected) BorderStroke(1.5.dp, PrimaryCyan) else BorderStroke(1.dp, OutlineVariantDark.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isSelected) PrimaryCyan else OnSurfaceDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) PrimaryCyan.copy(alpha = 0.85f) else OnSurfaceVariantDark
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) PrimaryCyan else OnSurfaceVariantDark,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = title,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = OnSurfaceVariantDark,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimaryCyan,
                checkedTrackColor = PrimaryCyan,
                uncheckedThumbColor = OnSurfaceVariantDark,
                uncheckedTrackColor = SurfaceContainerHighestDark,
                uncheckedBorderColor = OutlineVariantDark
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = OnSurfaceVariantDark,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = OnSurfaceVariantDark,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun StorageLegend(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceVariantDark
        )
    }
}
