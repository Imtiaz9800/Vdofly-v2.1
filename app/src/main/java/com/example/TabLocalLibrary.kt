package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.example.ui.theme.*

@Composable
fun FilterCategoryTabChip(
    icon: ImageVector,
    label: String,
    countBadge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) PrimaryContainerCyan else SurfaceContainerHighDark,
        contentColor = if (isSelected) OnPrimaryCyan else OnSurfaceDark,
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) OnPrimaryCyan else PrimaryCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (countBadge != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) OnPrimaryCyan.copy(alpha = 0.2f) else SurfaceContainerHighestDark
                ) {
                    Text(
                        countBadge,
                        color = if (isSelected) OnPrimaryCyan else PrimaryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalLibraryContent(
    viewModel: VideoViewModel,
    videos: List<VideoItem>,
    filteredVideos: List<VideoItem>,
    continueWatching: List<VideoItem>,
    folders: List<VideoFolder>,
    selectedFilter: FilterCategory,
    selectedFolder: String?,
    isGridView: Boolean,
    imageLoader: ImageLoader,
    isVaultUnlocked: Boolean = false,
    hiddenVideosCount: Int = 0,
    hiddenVideoUris: Set<String> = emptySet(),
    isSelectionMode: Boolean = false,
    selectedUris: Set<android.net.Uri> = emptySet(),
    onToggleSelectUri: (android.net.Uri) -> Unit = {},
    onEnterSelectionMode: (VideoItem) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeselectAll: () -> Unit = {},
    onUnlockVaultRequested: () -> Unit = {},
    onChangePinRequested: () -> Unit = {},
    onLockToggle: (VideoItem) -> Unit = {},
    onLockToggleFolder: (String) -> Unit = {},
    onToggleGridView: () -> Unit,
    onVideoSelected: (VideoItem) -> Unit,
    onVideoLongClick: (VideoItem) -> Unit,
    onDeleteRequested: (VideoItem) -> Unit,
    onInfoRequested: (VideoItem) -> Unit,
    onFolderSelected: (String) -> Unit,
    onNavigateToRecent: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterCategoryTabChip(
                        icon = Icons.Default.FolderOpen,
                        label = "All Folders",
                        isSelected = selectedFilter == FilterCategory.ALL_FOLDERS,
                        onClick = { viewModel.setFilterCategory(FilterCategory.ALL_FOLDERS) }
                    )
                }
                item {
                    FilterCategoryTabChip(
                        icon = Icons.Default.PlayCircle,
                        label = "Videos",
                        countBadge = videos.size.toString(),
                        isSelected = selectedFilter == FilterCategory.VIDEOS,
                        onClick = { viewModel.setFilterCategory(FilterCategory.VIDEOS) }
                    )
                }
                item {
                    FilterCategoryTabChip(
                        icon = Icons.Default.Done,
                        label = "Downloaded",
                        isSelected = selectedFilter == FilterCategory.DOWNLOADED,
                        onClick = { viewModel.setFilterCategory(FilterCategory.DOWNLOADED) }
                    )
                }
                item {
                    FilterCategoryTabChip(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp Status",
                        isSelected = selectedFilter == FilterCategory.WHATSAPP,
                        onClick = { viewModel.setFilterCategory(FilterCategory.WHATSAPP) }
                    )
                }
                item {
                    FilterCategoryTabChip(
                        icon = Icons.Default.Videocam,
                        label = "Camera",
                        isSelected = selectedFilter == FilterCategory.CAMERA,
                        onClick = { viewModel.setFilterCategory(FilterCategory.CAMERA) }
                    )
                }
                item {
                    FilterCategoryTabChip(
                        icon = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        label = if (isVaultUnlocked) "Private Safe" else "Hidden Folder",
                        countBadge = if (hiddenVideosCount > 0) "$hiddenVideosCount" else null,
                        isSelected = selectedFilter == FilterCategory.HIDDEN,
                        onClick = {
                            if (isVaultUnlocked) {
                                viewModel.setFilterCategory(FilterCategory.HIDDEN)
                            } else {
                                onUnlockVaultRequested()
                            }
                        }
                    )
                }
            }
        }

        if (continueWatching.isNotEmpty() && selectedFolder == null && selectedFilter == FilterCategory.ALL_FOLDERS) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Continue Watching",
                                color = OnSurfaceDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        TextButton(
                            onClick = onNavigateToRecent,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("History", color = PrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(14.dp))
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(continueWatching, key = { it.id }) { video ->
                            ContinueWatchingCard(
                                video = video,
                                imageLoader = imageLoader,
                                onClick = { onVideoSelected(video) }
                            )
                        }
                    }
                }
            }
        }

        if (selectedFilter == FilterCategory.HIDDEN) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = BorderStroke(1.dp, TertiaryAmber.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TertiaryAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = TertiaryAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Private Safe Active",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = OnSurfaceDark
                                )
                                Text(
                                    "${filteredVideos.size} files hidden from gallery",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantDark
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = onChangePinRequested,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Change PIN", color = TertiaryAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    viewModel.lockVault()
                                    viewModel.setFilterCategory(FilterCategory.ALL_FOLDERS)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TertiaryAmber,
                                    contentColor = OnTertiaryAmber
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedFolder != null) {
                        IconButton(onClick = { viewModel.selectFolder(null) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryCyan)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedFolder, color = OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    } else {
                        Text(
                            when (selectedFilter) {
                                FilterCategory.ALL_FOLDERS -> "Folders"
                                FilterCategory.HIDDEN -> "Protected Files"
                                else -> "Videos"
                            },
                            color = if (selectedFilter == FilterCategory.HIDDEN) TertiaryAmber else OnSurfaceDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = SurfaceContainerHighDark
                        ) {
                            Text(
                                if (selectedFilter == FilterCategory.ALL_FOLDERS) "${folders.size}" else "${filteredVideos.size}",
                                color = OnSurfaceVariantDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedFolder != null) {
                        Surface(
                            onClick = { onLockToggleFolder(selectedFolder) },
                            shape = RoundedCornerShape(8.dp),
                            color = TertiaryContainerAmber.copy(alpha = 0.85f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Hide entire folder",
                                    tint = OnTertiaryAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Hide Folder",
                                    color = OnTertiaryAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (filteredVideos.isNotEmpty() && (selectedFolder != null || selectedFilter != FilterCategory.ALL_FOLDERS)) {
                        Surface(
                            onClick = {
                                if (isSelectionMode) {
                                    if (selectedUris.size == filteredVideos.size) onDeselectAll() else onSelectAll()
                                } else {
                                    onSelectAll()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelectionMode) PrimaryContainerCyan else SurfaceContainerHighDark,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    if (isSelectionMode && selectedUris.size == filteredVideos.size) Icons.Default.CheckCircle else Icons.Default.Checklist,
                                    contentDescription = "Select items",
                                    tint = if (isSelectionMode) OnPrimaryCyan else PrimaryCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isSelectionMode) {
                                        if (selectedUris.size == filteredVideos.size) "Deselect All" else "Select All"
                                    } else "Select",
                                    color = if (isSelectionMode) OnPrimaryCyan else OnSurfaceDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    IconButton(onClick = onToggleGridView, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            contentDescription = "Toggle Grid",
                            tint = OnSurfaceVariantDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (selectedFolder == null && selectedFilter == FilterCategory.ALL_FOLDERS) {
            if (isGridView) {
                val chunkedFolders = folders.chunked(2)
                items(chunkedFolders, key = { chunk -> chunk.joinToString { it.name } }) { rowFolders ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (folder in rowFolders) {
                            Box(modifier = Modifier.weight(1f)) {
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            onLockToggleFolder(folder.name)
                                            false
                                        } else false
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        val isDismissing = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                                        if (isDismissing) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(TertiaryContainerAmber)
                                                    .padding(horizontal = 14.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = "Hide folder",
                                                        tint = OnTertiaryAmber,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        "Hide",
                                                        color = OnTertiaryAmber,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    content = {
                                        FolderGridCard(
                                            folder = folder,
                                            imageLoader = imageLoader,
                                            onClick = { onFolderSelected(folder.name) },
                                            onLockFolder = { onLockToggleFolder(folder.name) }
                                        )
                                    }
                                )
                            }
                        }
                        if (rowFolders.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(folders, key = { it.name }) { folder ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onLockToggleFolder(folder.name)
                                false
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        backgroundContent = {
                            val isDismissing = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                            if (isDismissing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(TertiaryContainerAmber)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Hide Folder",
                                            color = OnTertiaryAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Hide folder to safe",
                                            tint = OnTertiaryAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        },
                        content = {
                            FolderListItem(
                                folder = folder,
                                imageLoader = imageLoader,
                                onClick = { onFolderSelected(folder.name) },
                                onLockFolder = { onLockToggleFolder(folder.name) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }
        } else {
            if (filteredVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedFilter == FilterCategory.HIDDEN) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(TertiaryAmber.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TertiaryAmber,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No hidden videos in vault",
                                    color = OnSurfaceDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Swipe left or tap the lock button on any video or folder to move it into your Private Safe.",
                                    color = OnSurfaceVariantDark,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FolderOff, contentDescription = null, tint = OutlineDark, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No videos found", color = OnSurfaceVariantDark, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else if (isGridView) {
                val chunkedVideos = filteredVideos.chunked(2)
                items(chunkedVideos, key = { chunk -> chunk.joinToString { it.id.toString() } }) { rowVideos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (video in rowVideos) {
                            Box(modifier = Modifier.weight(1f)) {
                                val isLocked = hiddenVideoUris.contains(video.uri.toString())
                                val isSelected = selectedUris.contains(video.uri)
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        when (it) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                onDeleteRequested(video)
                                                false
                                            }
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                onLockToggle(video)
                                                false
                                            }
                                            else -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = !isSelectionMode,
                                    enableDismissFromEndToStart = !isSelectionMode,
                                    backgroundContent = {
                                        val isDeleting = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
                                        val isHiding = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart

                                        if (isDeleting) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.errorContainer)
                                                    .padding(horizontal = 14.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete video",
                                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Delete",
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } else if (isHiding) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isLocked) SurfaceContainerHighestDark else TertiaryContainerAmber
                                                    )
                                                    .padding(horizontal = 14.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                                        contentDescription = if (isLocked) "Unhide" else "Hide",
                                                        tint = if (isLocked) PrimaryCyan else OnTertiaryAmber,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (isLocked) "Unhide" else "Hide",
                                                        color = if (isLocked) PrimaryCyan else OnTertiaryAmber,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    content = {
                                        VideoGridCard(
                                            video = video,
                                            imageLoader = imageLoader,
                                            isLocked = isLocked,
                                            isSelectionMode = isSelectionMode,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isSelectionMode) onToggleSelectUri(video.uri)
                                                else onVideoSelected(video)
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) onEnterSelectionMode(video)
                                                else onToggleSelectUri(video.uri)
                                            },
                                            onDeleteClick = { onDeleteRequested(video) },
                                            onInfoClick = { onInfoRequested(video) },
                                            onLockToggle = { onLockToggle(video) }
                                        )
                                    }
                                )
                            }
                        }
                        if (rowVideos.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(filteredVideos, key = { it.id }) { video ->
                    val isLocked = hiddenVideoUris.contains(video.uri.toString())
                    val isSelected = selectedUris.contains(video.uri)
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when (it) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onDeleteRequested(video)
                                    false
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    onLockToggle(video)
                                    false
                                }
                                else -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = !isSelectionMode,
                        enableDismissFromEndToStart = !isSelectionMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        backgroundContent = {
                            val isDeleting = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
                            val isHiding = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart

                            if (isDeleting) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete video",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Delete Video",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else if (isHiding) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isLocked) SurfaceContainerHighestDark else TertiaryContainerAmber
                                        )
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            if (isLocked) "Unhide Video" else "Hide Video",
                                            color = if (isLocked) PrimaryCyan else OnTertiaryAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Icon(
                                            if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = if (isLocked) "Unhide video" else "Hide video to safe",
                                            tint = if (isLocked) PrimaryCyan else OnTertiaryAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        },
                        content = {
                            VideoListItemCard(
                                video = video,
                                imageLoader = imageLoader,
                                isLocked = isLocked,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelectionMode) onToggleSelectUri(video.uri)
                                    else onVideoSelected(video)
                                },
                                onLongClick = {
                                    if (!isSelectionMode) onEnterSelectionMode(video)
                                    else onToggleSelectUri(video.uri)
                                },
                                onDeleteClick = { onDeleteRequested(video) },
                                onInfoClick = { onInfoRequested(video) },
                                onLockToggle = { onLockToggle(video) }
                            )
                        }
                    )
                }
            }
        }
    }
}
