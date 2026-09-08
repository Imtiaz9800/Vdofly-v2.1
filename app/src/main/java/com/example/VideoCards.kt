package com.example

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.ui.theme.*

@Composable
fun FilterChipItem(
    category: FilterCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
    hiddenCount: Int = 0
) {
    val (label, icon) = when (category) {
        FilterCategory.ALL_FOLDERS -> "All Folders" to Icons.Default.Folder
        FilterCategory.VIDEOS -> "All Videos" to Icons.Default.VideoLibrary
        FilterCategory.DOWNLOADED -> "Downloads" to Icons.Default.Download
        FilterCategory.WHATSAPP -> "WhatsApp" to Icons.Default.Chat
        FilterCategory.CAMERA -> "Camera" to Icons.Default.CameraAlt
        FilterCategory.HIDDEN -> "Safe Folder" to Icons.Default.Lock
    }

    val isHiddenFilter = category == FilterCategory.HIDDEN
    val containerColor = when {
        isSelected && isHiddenFilter -> TertiaryContainerAmber
        isSelected -> PrimaryContainerCyan
        else -> SurfaceContainerDark
    }
    val contentColor = when {
        isSelected && isHiddenFilter -> OnTertiaryAmber
        isSelected -> OnPrimaryCyan
        isHiddenFilter -> TertiaryAmber
        else -> OnSurfaceVariantDark
    }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            if (isHiddenFilter && hiddenCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) OnTertiaryAmber.copy(alpha = 0.2f) else TertiaryContainerAmber
                ) {
                    Text(
                        text = "$hiddenCount",
                        color = if (isSelected) OnTertiaryAmber else OnTertiaryAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    video: VideoItem,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    val progress = if (video.duration > 0) {
        (video.resumePosition.toFloat() / video.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        modifier = Modifier
            .width(240.dp)
            .height(155.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(SurfaceContainerHighestDark)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.uri)
                        .videoFrameMillis(video.resumePosition.coerceAtLeast(1000))
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryCyan.copy(alpha = 0.85f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Resume Playback",
                                tint = OnPrimaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceContainerLowestDark.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        "${formatDuration(video.resumePosition)} / ${formatDuration(video.duration)}",
                        color = OnSurfaceDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = PrimaryCyan,
                    trackColor = OutlineVariantDark.copy(alpha = 0.4f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = video.name,
                    color = OnSurfaceDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% completed",
                        color = PrimaryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = video.bucketName,
                        color = OnSurfaceVariantDark,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun getFolderIcon(name: String): ImageVector {
    return when {
        name.contains("WhatsApp", ignoreCase = true) -> Icons.Default.Chat
        name.contains("Camera", ignoreCase = true) -> Icons.Default.CameraAlt
        name.contains("Download", ignoreCase = true) -> Icons.Default.Download
        name.contains("Screen", ignoreCase = true) -> Icons.Default.Screenshot
        name.contains("Movie", ignoreCase = true) -> Icons.Default.Movie
        name.contains("Music", ignoreCase = true) -> Icons.Default.MusicVideo
        else -> Icons.Default.Folder
    }
}

private fun getFolderColor(name: String): Color {
    return when {
        name.contains("WhatsApp", ignoreCase = true) -> Color(0xFF25D366)
        name.contains("Camera", ignoreCase = true) -> PrimaryCyan
        name.contains("Movie", ignoreCase = true) -> SecondaryBlue
        name.contains("Download", ignoreCase = true) -> TertiaryAmber
        else -> OnSurfaceDark
    }
}

@Composable
fun FolderListItem(
    folder: VideoFolder,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    onLockFolder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp, 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerHighestDark)
            ) {
                if (folder.latestVideoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(folder.latestVideoUri)
                            .videoFrameMillis(1000)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getFolderIcon(folder.name),
                        contentDescription = null,
                        tint = getFolderColor(folder.name),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        folder.name,
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (folder.isNew) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TertiaryContainerAmber
                        ) {
                            Text(
                                "NEW",
                                color = OnTertiaryAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${folder.videoCount} video${if (folder.videoCount > 1) "s" else ""}",
                        color = OnSurfaceVariantDark,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", color = OutlineDark, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        formatFileSize(folder.totalSizeBytes),
                        color = OnSurfaceVariantDark,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onLockFolder,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Hide folder to safe",
                        tint = TertiaryAmber,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = OnSurfaceVariantDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FolderGridCard(
    folder: VideoFolder,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    onLockFolder: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(SurfaceContainerHighestDark)
            ) {
                if (folder.latestVideoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(folder.latestVideoUri)
                            .videoFrameMillis(1000)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getFolderIcon(folder.name),
                        contentDescription = null,
                        tint = getFolderColor(folder.name),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (folder.isNew) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TertiaryContainerAmber,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                "NEW",
                                color = OnTertiaryAmber,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Surface(
                        onClick = onLockFolder,
                        shape = CircleShape,
                        color = SurfaceContainerLowestDark.copy(alpha = 0.85f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Hide folder to safe",
                                tint = TertiaryAmber,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    folder.name,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${folder.videoCount} videos • ${formatFileSize(folder.totalSizeBytes)}",
                    color = OnSurfaceVariantDark,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItemCard(
    video: VideoItem,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onLockToggle: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = when {
        isSelected && isLocked -> TertiaryContainerAmber.copy(alpha = 0.25f)
        isSelected -> PrimaryContainerCyan.copy(alpha = 0.25f)
        else -> SurfaceContainerDark
    }

    val borderStroke = when {
        isSelected && isLocked -> androidx.compose.foundation.BorderStroke(1.5.dp, TertiaryAmber)
        isSelected -> androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryCyan)
        else -> null
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) (if (isLocked) TertiaryAmber else PrimaryCyan)
                            else SurfaceContainerHighestDark
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isLocked) OnTertiaryAmber else OnPrimaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerDark)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(90.dp, 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerHighestDark)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.uri)
                        .videoFrameMillis(1000)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isLocked) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = SurfaceContainerLowestDark.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopStart).padding(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Hidden in Vault",
                            tint = TertiaryAmber,
                            modifier = Modifier.size(12.dp).padding(1.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = SurfaceContainerLowestDark.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp)
                ) {
                    Text(
                        formatDuration(video.duration),
                        color = OnSurfaceDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    video.name,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = SurfaceContainerHighestDark
                    ) {
                        Text(
                            video.resolution,
                            color = PrimaryCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = TertiaryContainerAmber
                        ) {
                            Text(
                                "SAFE",
                                color = OnTertiaryAmber,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatFileSize(video.size), color = OnSurfaceVariantDark, fontSize = 11.sp)
                }
            }

            if (!isSelectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = PrimaryCyan, modifier = Modifier.size(28.dp))
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = OnSurfaceVariantDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceContainerDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play", color = OnSurfaceDark) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryCyan) },
                                onClick = {
                                    showMenu = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isLocked) "Unhide Video" else "Lock & Hide Video", color = TertiaryAmber) },
                                leadingIcon = {
                                    Icon(
                                        if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TertiaryAmber
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onLockToggle()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Details", color = OnSurfaceDark) },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = OnSurfaceVariantDark) },
                                onClick = {
                                    showMenu = false
                                    onInfoClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Video", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridCard(
    video: VideoItem,
    imageLoader: ImageLoader,
    isLocked: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onLockToggle: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = when {
        isSelected && isLocked -> TertiaryContainerAmber.copy(alpha = 0.25f)
        isSelected -> PrimaryContainerCyan.copy(alpha = 0.25f)
        else -> SurfaceContainerDark
    }

    val borderStroke = when {
        isSelected && isLocked -> androidx.compose.foundation.BorderStroke(1.5.dp, TertiaryAmber)
        isSelected -> androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryCyan)
        else -> null
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(SurfaceContainerHighestDark)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.uri)
                        .videoFrameMillis(1000)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = SurfaceContainerLowestDark.copy(alpha = 0.85f)
                    ) {
                        Text(
                            video.resolution,
                            color = PrimaryCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                    if (isLocked) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = TertiaryContainerAmber
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = OnTertiaryAmber, modifier = Modifier.size(9.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "SAFE",
                                    color = OnTertiaryAmber,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) (if (isLocked) TertiaryAmber else PrimaryCyan)
                                else SurfaceContainerLowestDark.copy(alpha = 0.85f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (isLocked) OnTertiaryAmber else OnPrimaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerDark.copy(alpha = 0.9f))
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = SurfaceContainerLowestDark.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            formatDuration(video.duration),
                            color = OnSurfaceDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        video.name,
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!isSelectionMode) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = OnSurfaceVariantDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(SurfaceContainerDark)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Play", color = OnSurfaceDark) },
                                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryCyan) },
                                    onClick = {
                                        showMenu = false
                                        onClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isLocked) "Unhide Video" else "Lock & Hide Video", color = TertiaryAmber) },
                                    leadingIcon = {
                                        Icon(
                                            if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = TertiaryAmber
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onLockToggle()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Details", color = OnSurfaceDark) },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = OnSurfaceVariantDark) },
                                    onClick = {
                                        showMenu = false
                                        onInfoClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Video", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    formatFileSize(video.size),
                    color = OnSurfaceVariantDark,
                    fontSize = 10.sp
                )
            }
        }
    }
}
