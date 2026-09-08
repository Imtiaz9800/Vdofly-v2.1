package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class TutorialTopic(
    val title: String,
    val icon: ImageVector,
    val items: List<Pair<String, String>>
)

@Composable
fun TutorialScreen(onBack: () -> Unit) {
    var expandedTopicIndex by remember { mutableIntStateOf(0) }

    val topics = remember {
        listOf(
            TutorialTopic(
                title = "Video Player & Touch Gestures",
                icon = Icons.Default.PlayCircle,
                items = listOf(
                    "Brightness Adjustment" to "Swipe vertically up or down on the left half of the screen to smoothly change display brightness (0% – 100%).",
                    "Volume & 200% Boost" to "Swipe vertically up or down on the right half of the screen to adjust media volume, with support for 200% volume boost.",
                    "Configurable Double-Tap Seek" to "Double-tap on the left side to rewind or right side to fast forward. Customize the jump step (5s, 10s, 15s, 30s) in Settings.",
                    "Smooth Frame Scrubbing" to "Drag your finger horizontally across the player to scrub smoothly to any target frame with real-time timestamp display.",
                    "Screen Lock Mode" to "Tap the Lock icon on the left edge of the player controls to lock the screen and prevent accidental touch input.",
                    "Aspect Ratio Controls" to "Tap the Aspect Ratio icon to cycle between Fit (original proportions), Zoom / Crop (fill screen), and Stretch."
                )
            ),
            TutorialTopic(
                title = "Subtitles, Audio & Playback Speed",
                icon = Icons.Default.Subtitles,
                items = listOf(
                    "Subtitles & Closed Captions" to "Tap the Subtitle (CC) button in the top player bar to switch between embedded subtitle tracks or turn captions On/Off.",
                    "Import Custom Subtitle Files" to "Tap 'Add Subtitle File' in the subtitles dialog to load external .srt, .vtt, or .ass subtitle files directly from device storage.",
                    "Dual Audio Track Switching" to "Tap the Audio Track button to toggle between different language and commentary audio streams.",
                    "Variable Playback Speed" to "Tap the Speed button (1.0x) to adjust speed smoothly from 0.5x (slow-motion) up to 2.0x (rapid listening).",
                    "Sleep Timer" to "Tap the Timer icon to set an auto-pause timer (5 – 120 minutes). A live countdown badge appears when active.",
                    "Picture-in-Picture (PiP) & Background Play" to "Tap the PiP icon or minimize the app to keep watching in a floating window, or enable Background Audio in Settings."
                )
            ),
            TutorialTopic(
                title = "Library Gestures & Management",
                icon = Icons.Default.Swipe,
                items = listOf(
                    "Swipe Right to Delete" to "Swipe any video item to the right in either List or Grid view to instantly open the delete confirmation dialog.",
                    "Swipe Left to Lock in Private Safe" to "Swipe any video item to the left to quickly hide it into your PIN-protected Private Safe folder.",
                    "List & Grid View Layouts" to "Toggle between the compact file list and the visual 2-column card grid using the layout button in the top bar.",
                    "Batch Multi-Selection" to "Long-press any video to enter selection mode, allowing you to select multiple items for batch lock, batch delete, or sharing.",
                    "Category Filters & Search" to "Quickly filter media by All Folders, Videos, Downloads, WhatsApp Status, or Camera, or search by file title instantly."
                )
            ),
            TutorialTopic(
                title = "Private Safe & Security PIN",
                icon = Icons.Default.Lock,
                items = listOf(
                    "4-Digit Security PIN" to "Protect confidential videos with a custom 4-digit PIN. Configure or change your PIN anytime in Settings.",
                    "Private Vault Access" to "Switch to the Private Safe tab to view locked videos. Your PIN is required to unlock and access hidden media.",
                    "Instant Unhide / Unlock" to "Swipe left on any locked video or use the 3-dot menu to restore it back to your public media library."
                )
            ),
            TutorialTopic(
                title = "Network Streaming & Resume",
                icon = Icons.Default.Link,
                items = listOf(
                    "Network Stream URL" to "Play direct remote streams by entering HTTP, HTTPS, HLS (.m3u8), or RTSP streaming links in the Network tab.",
                    "Auto-Resume & History" to "Every video automatically remembers your exact playback progress so you can resume seamlessly from Recent History.",
                    "Clear Playback History" to "Reset your watched history and saved timestamps anytime via the History tab or Settings menu."
                )
            ),
            TutorialTopic(
                title = "Hardware Decoder Modes",
                icon = Icons.Default.Speed,
                items = listOf(
                    "HW+ (Hardware Plus)" to "High-performance hardware acceleration with optimized buffer management for 4K / 60fps videos and lower battery usage.",
                    "HW (Standard Hardware)" to "Standard Android MediaCodec hardware decoding providing broad compatibility across all devices.",
                    "SW (Software Fallback)" to "CPU-based software decoding pipeline for uncommon, legacy, or complex video codecs."
                )
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = SurfaceContainerLowestDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Settings",
                            tint = PrimaryCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            "App Tutorial & User Guide",
                            color = OnSurfaceDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Complete guide to gestures, streaming & player features",
                            color = OnSurfaceVariantDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = SurfaceContainerLowestDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryContainerCyan.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = OnPrimaryCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Welcome to VDOFLY",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "Hardware-accelerated HD player with intuitive gesture controls, subtitle support, and network streaming.",
                                color = OnSurfaceDark.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "INSTRUCTIONS & FEATURES",
                    color = PrimaryCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(topics) { index, topic ->
                val isExpanded = expandedTopicIndex == index

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) SurfaceContainerHighDark else SurfaceContainerDark
                    ),
                    border = if (isExpanded) BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedTopicIndex = if (isExpanded) -1 else index
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isExpanded) PrimaryContainerCyan else SurfaceContainerHighestDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        topic.icon,
                                        contentDescription = null,
                                        tint = if (isExpanded) OnPrimaryCyan else PrimaryCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    topic.title,
                                    color = OnSurfaceDark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = if (isExpanded) PrimaryCyan else OnSurfaceVariantDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(2.dp))
                                topic.items.forEach { (heading, instruction) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 5.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryCyan)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                heading,
                                                color = PrimaryCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                instruction,
                                                color = OnSurfaceDark.copy(alpha = 0.9f),
                                                fontSize = 12.sp,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = OnPrimaryCyan
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    storageStats: StorageStats,
    totalVideosCount: Int,
    onRescan: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = SurfaceContainerLowestDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Settings",
                            tint = PrimaryCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            "About VDOFLY",
                            color = OnSurfaceDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Player specs, engine architecture & library statistics",
                            color = OnSurfaceVariantDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = SurfaceContainerLowestDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryContainerCyan.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainerCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = OnPrimaryCyan,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "VDOFLY Video Player",
                            color = OnSurfaceDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Version 1.0.0 (Pro Edition)",
                            color = PrimaryCyan,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceContainerHighestDark
                            ) {
                                Text(
                                    "RELEASE BUILD",
                                    color = OnSurfaceDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceContainerHighestDark
                            ) {
                                Text(
                                    "64-BIT KOTLIN",
                                    color = OnSurfaceDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "A next-generation, hardware-accelerated media player engineered for ultra-smooth 4K playback, comprehensive gesture controls, and effortless local & network streaming.",
                            color = OnSurfaceDark.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text("LIBRARY & STORAGE", color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Indexed Videos", color = OnSurfaceVariantDark, fontSize = 13.sp)
                            Text("$totalVideosCount videos", color = OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Media Size on Disk", color = OnSurfaceVariantDark, fontSize = 13.sp)
                            Text(String.format("%.2f GB", storageStats.videoMediaGb), color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = OutlineVariantDark.copy(alpha = 0.5f))
                        Button(
                            onClick = onRescan,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerHighestDark,
                                contentColor = PrimaryCyan
                            )
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rescan Media Library", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Text("CORE CAPABILITIES", color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AboutFeatureRow(
                            icon = Icons.Default.Speed,
                            title = "Multi-Engine Hardware Decoder",
                            desc = "HW+, HW, and SW decoders for smooth 4K/60fps playback with minimal battery drain."
                        )
                        AboutFeatureRow(
                            icon = Icons.Default.TouchApp,
                            title = "Precision Gesture Controls",
                            desc = "Vertical swipes for brightness & volume, double-tap seek, and horizontal frame scrubbing."
                        )
                        AboutFeatureRow(
                            icon = Icons.Default.Subtitles,
                            title = "Subtitles & Multi-Audio",
                            desc = "Internal track switching and external subtitle importing (.srt, .vtt, .ass)."
                        )
                        AboutFeatureRow(
                            icon = Icons.Default.PictureInPictureAlt,
                            title = "Picture-in-Picture & Speed Control",
                            desc = "Floating multitasking window, sleep timer (5-120 min), and speed control (0.5x-2.0x)."
                        )
                        AboutFeatureRow(
                            icon = Icons.Default.Link,
                            title = "Network Live & Stream Player",
                            desc = "Full support for HTTP/HTTPS video links, HLS (.m3u8), and RTSP streams."
                        )
                    }
                }
            }

            item {
                Text("ENGINE & ARCHITECTURE", color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TechSpecRow("Playback Engine", "AndroidX Media3 / ExoPlayer 1.4.1")
                        TechSpecRow("UI Framework", "Jetpack Compose & Material Design 3")
                        TechSpecRow("Video Decoders", "MediaCodec NDK & Coil Frame Pipeline")
                        TechSpecRow("Local Storage", "AndroidX DataStore Preferences")
                        TechSpecRow("Language & Runtime", "Kotlin 2.0 with Coroutines & StateFlow")
                    }
                }
            }

            item {
                Text("PRIVACY & DATA POLICY", color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "100% Offline-First & Private",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "All media scanning, playback positions, and cache are stored strictly on your local device. VDOFLY collects zero analytics, zero telemetry, and never uploads personal data.",
                                color = OnSurfaceVariantDark,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = OnPrimaryCyan
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AboutFeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PrimaryContainerCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = OnSurfaceDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = OnSurfaceVariantDark, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun TechSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = OnSurfaceVariantDark, fontSize = 12.sp)
        Text(value, color = OnSurfaceDark, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}

@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(
                color = SurfaceContainerLowestDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Settings",
                            tint = PrimaryCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "About Developer",
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        },
        containerColor = SurfaceContainerLowestDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainerDark
                ),
                border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainerCyan.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Love",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Made with love by",
                        color = OnSurfaceVariantDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Imtiaz Haque",
                        color = OnSurfaceDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = OnPrimaryCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back to Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
