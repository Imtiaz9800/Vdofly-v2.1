package com.example

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.example.ui.theme.*

enum class BottomNavTab {
    LOCAL, RECENT, NETWORK, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    viewModel: VideoViewModel,
    onVideoSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val permissionGranted by viewModel.permissionGranted.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatchingVideos.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val decoderMode by viewModel.decoderMode.collectAsStateWithLifecycle()
    val recentStreams by viewModel.recentStreams.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val vaultPin by viewModel.vaultPin.collectAsStateWithLifecycle()
    val hiddenVideosCount by viewModel.hiddenVideosCount.collectAsStateWithLifecycle()
    val hiddenVideoUris by viewModel.hiddenVideoUris.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(BottomNavTab.LOCAL) }
    var showSearchField by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }
    var showStreamDialog by remember { mutableStateOf(false) }
    var showPrivateVaultDialog by remember { mutableStateOf(false) }
    var showCastDialog by remember { mutableStateOf(false) }
    var streamUrlInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }
    var isChangingPin by remember { mutableStateOf(false) }
    var videoPendingHide by remember { mutableStateOf<VideoItem?>(null) }
    var folderPendingHide by remember { mutableStateOf<String?>(null) }
    var batchPendingHide by remember { mutableStateOf<Set<Uri>?>(null) }
    var videoToDelete by remember { mutableStateOf<VideoItem?>(null) }
    var videoDetailsToShow by remember { mutableStateOf<VideoItem?>(null) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedUris by remember { mutableStateOf(setOf<Uri>()) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val onToggleSelectUri: (Uri) -> Unit = { uri ->
        selectedUris = if (selectedUris.contains(uri)) {
            val updated = selectedUris - uri
            if (updated.isEmpty()) isSelectionMode = false
            updated
        } else {
            selectedUris + uri
        }
    }

    val onEnterSelectionMode: (VideoItem) -> Unit = { video ->
        isSelectionMode = true
        selectedUris = setOf(video.uri)
    }

    val onSelectAll: () -> Unit = {
        isSelectionMode = true
        selectedUris = filteredVideos.map { it.uri }.toSet()
    }

    val onDeselectAll: () -> Unit = {
        selectedUris = emptySet()
        isSelectionMode = false
    }

    val onBatchHideClicked: () -> Unit = {
        if (selectedFilter == FilterCategory.HIDDEN) {
            viewModel.unhideVideos(selectedUris)
            isSelectionMode = false
            selectedUris = emptySet()
        } else {
            if (vaultPin.isNullOrEmpty()) {
                batchPendingHide = selectedUris
                videoPendingHide = null
                folderPendingHide = null
                pinInput = ""
                pinErrorText = null
                isChangingPin = false
                showPrivateVaultDialog = true
            } else {
                viewModel.hideVideos(selectedUris)
                isSelectionMode = false
                selectedUris = emptySet()
            }
        }
    }

    val onToggleLockVideo: (VideoItem) -> Unit = { video ->
        if (hiddenVideoUris.contains(video.uri.toString())) {
            viewModel.unhideVideo(video.uri)
        } else {
            if (vaultPin.isNullOrEmpty()) {
                videoPendingHide = video
                folderPendingHide = null
                pinInput = ""
                pinErrorText = null
                isChangingPin = false
                showPrivateVaultDialog = true
            } else {
                viewModel.hideVideo(video.uri)
            }
        }
    }

    val onToggleLockFolder: (String) -> Unit = { folderName ->
        if (vaultPin.isNullOrEmpty()) {
            folderPendingHide = folderName
            videoPendingHide = null
            pinInput = ""
            pinErrorText = null
            isChangingPin = false
            showPrivateVaultDialog = true
        } else {
            viewModel.hideFolder(folderName)
        }
    }

    // Back navigation handlers
    BackHandler(enabled = isSelectionMode) {
        isSelectionMode = false
        selectedUris = emptySet()
    }

    BackHandler(enabled = !isSelectionMode && showSearchField) {
        if (searchQuery.isNotEmpty()) {
            viewModel.updateSearchQuery("")
        }
        showSearchField = false
    }

    BackHandler(enabled = !isSelectionMode && !showSearchField && selectedFolder != null) {
        viewModel.selectFolder(null)
    }

    BackHandler(enabled = !isSelectionMode && !showSearchField && selectedFolder == null && selectedFilter == FilterCategory.HIDDEN) {
        viewModel.setFilterCategory(FilterCategory.ALL_FOLDERS)
    }

    BackHandler(enabled = !isSelectionMode && !showSearchField && selectedFolder == null && selectedFilter != FilterCategory.ALL_FOLDERS && selectedFilter != FilterCategory.HIDDEN && currentTab == BottomNavTab.LOCAL) {
        viewModel.setFilterCategory(FilterCategory.ALL_FOLDERS)
    }

    BackHandler(enabled = !isSelectionMode && !showSearchField && selectedFolder == null && currentTab != BottomNavTab.LOCAL) {
        currentTab = BottomNavTab.LOCAL
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissionToRequest)
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedUris = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit selection", tint = OnSurfaceDark)
                        }
                    },
                    title = {
                        Text(
                            "${selectedUris.size} selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OnSurfaceDark
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedUris.size == filteredVideos.size) {
                                onDeselectAll()
                            } else {
                                onSelectAll()
                            }
                        }) {
                            Icon(
                                imageVector = if (selectedUris.size == filteredVideos.size) Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = if (selectedUris.size == filteredVideos.size) "Deselect all" else "Select all",
                                tint = PrimaryCyan
                            )
                        }
                        IconButton(
                            onClick = { onBatchHideClicked() },
                            enabled = selectedUris.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = if (selectedFilter == FilterCategory.HIDDEN) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (selectedFilter == FilterCategory.HIDDEN) "Unhide selected" else "Hide selected",
                                tint = TertiaryAmber
                            )
                        }
                        IconButton(
                            onClick = { showBatchDeleteDialog = true },
                            enabled = selectedUris.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceContainerDark,
                        titleContentColor = OnSurfaceDark
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        if (showSearchField) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Search videos...", color = OnSurfaceVariantDark) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = OnSurfaceDark,
                                    unfocusedTextColor = OnSurfaceDark,
                                    focusedIndicatorColor = PrimaryCyan,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (searchQuery.isNotEmpty()) viewModel.updateSearchQuery("")
                                        else showSearchField = false
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = OnSurfaceVariantDark)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryContainerCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "VDOFLY Logo",
                                        tint = OnPrimaryCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "VDOFLY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = OnSurfaceDark
                                )
                            }
                        }
                    },
                    actions = {
                        if (!showSearchField) {
                            IconButton(onClick = { showSearchField = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = OnSurfaceVariantDark)
                            }
                            IconButton(onClick = {
                                if (isVaultUnlocked) {
                                    if (selectedFilter == FilterCategory.HIDDEN) {
                                        viewModel.lockVault()
                                        viewModel.setFilterCategory(FilterCategory.ALL_FOLDERS)
                                    } else {
                                        viewModel.setFilterCategory(FilterCategory.HIDDEN)
                                    }
                                } else {
                                    pinInput = ""
                                    pinErrorText = null
                                    isChangingPin = false
                                    showPrivateVaultDialog = true
                                }
                            }) {
                                BadgedBox(
                                    badge = {
                                        if (hiddenVideosCount > 0 && !isVaultUnlocked) {
                                            Badge(containerColor = TertiaryAmber) {
                                                Text("$hiddenVideosCount", color = OnTertiaryAmber)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = "Private Safe Folder",
                                        tint = if (isVaultUnlocked) TertiaryAmber else OnSurfaceVariantDark
                                    )
                                }
                            }
                            IconButton(onClick = { showCastDialog = true }) {
                                Icon(Icons.Default.Cast, contentDescription = "Cast", tint = OnSurfaceVariantDark)
                            }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = OnSurfaceVariantDark)
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(SurfaceContainerDark)
                                ) {
                                    Text(
                                        "SORT BY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryCyan,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Date Added", color = OnSurfaceDark) },
                                        onClick = { viewModel.setSortOrder(SortOrder.DATE_ADDED); showSortMenu = false },
                                        leadingIcon = { if (sortOrder == SortOrder.DATE_ADDED) Icon(Icons.Default.Check, null, tint = PrimaryCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Duration", color = OnSurfaceDark) },
                                        onClick = { viewModel.setSortOrder(SortOrder.DURATION); showSortMenu = false },
                                        leadingIcon = { if (sortOrder == SortOrder.DURATION) Icon(Icons.Default.Check, null, tint = PrimaryCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Size", color = OnSurfaceDark) },
                                        onClick = { viewModel.setSortOrder(SortOrder.SIZE); showSortMenu = false },
                                        leadingIcon = { if (sortOrder == SortOrder.SIZE) Icon(Icons.Default.Check, null, tint = PrimaryCyan) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Name", color = OnSurfaceDark) },
                                        onClick = { viewModel.setSortOrder(SortOrder.NAME); showSortMenu = false },
                                        leadingIcon = { if (sortOrder == SortOrder.NAME) Icon(Icons.Default.Check, null, tint = PrimaryCyan) }
                                    )
                                    HorizontalDivider(color = OutlineVariantDark)
                                    DropdownMenuItem(
                                        text = { Text("Rescan Library", color = OnSurfaceDark) },
                                        onClick = { viewModel.refreshLibrary(); showSortMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Sync, null, tint = PrimaryCyan) }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark.copy(alpha = 0.95f),
                        titleContentColor = OnSurfaceDark
                    )
                )
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                Surface(
                    color = SurfaceContainerDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (selectedUris.size == filteredVideos.size) onDeselectAll() else onSelectAll()
                            }
                        ) {
                            Icon(
                                if (selectedUris.size == filteredVideos.size) Icons.Default.RemoveDone else Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = PrimaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (selectedUris.size == filteredVideos.size) "Deselect All" else "Select All (${filteredVideos.size})",
                                color = PrimaryCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onBatchHideClicked() },
                                enabled = selectedUris.isNotEmpty(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = TertiaryContainerAmber,
                                    contentColor = OnTertiaryAmber
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    if (selectedFilter == FilterCategory.HIDDEN) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (selectedFilter == FilterCategory.HIDDEN) "Unhide" else "Hide",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { showBatchDeleteDialog = true },
                                enabled = selectedUris.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete (${selectedUris.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                NavigationBar(
                    containerColor = SurfaceContainerDark.copy(alpha = 0.95f),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.LOCAL,
                        onClick = { currentTab = BottomNavTab.LOCAL },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Local") },
                        label = { Text("Local", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryCyan,
                            selectedTextColor = PrimaryCyan,
                            unselectedIconColor = OnSurfaceVariantDark,
                            unselectedTextColor = OnSurfaceVariantDark,
                            indicatorColor = PrimaryContainerCyan.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.RECENT,
                        onClick = { currentTab = BottomNavTab.RECENT },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Recent") },
                        label = { Text("Recent", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryCyan,
                            selectedTextColor = PrimaryCyan,
                            unselectedIconColor = OnSurfaceVariantDark,
                            unselectedTextColor = OnSurfaceVariantDark,
                            indicatorColor = PrimaryContainerCyan.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.NETWORK,
                        onClick = { currentTab = BottomNavTab.NETWORK },
                        icon = { Icon(Icons.Default.Language, contentDescription = "Network") },
                        label = { Text("Network", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryCyan,
                            selectedTextColor = PrimaryCyan,
                            unselectedIconColor = OnSurfaceVariantDark,
                            unselectedTextColor = OnSurfaceVariantDark,
                            indicatorColor = PrimaryContainerCyan.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.SETTINGS,
                        onClick = { currentTab = BottomNavTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryCyan,
                            selectedTextColor = PrimaryCyan,
                            unselectedIconColor = OnSurfaceVariantDark,
                            unselectedTextColor = OnSurfaceVariantDark,
                            indicatorColor = PrimaryContainerCyan.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (currentTab) {
                BottomNavTab.LOCAL -> {
                    LocalLibraryContent(
                        viewModel = viewModel,
                        videos = videos,
                        filteredVideos = filteredVideos,
                        continueWatching = continueWatching,
                        folders = folders,
                        selectedFilter = selectedFilter,
                        selectedFolder = selectedFolder,
                        isGridView = isGridView,
                        imageLoader = imageLoader,
                        isVaultUnlocked = isVaultUnlocked,
                        hiddenVideosCount = hiddenVideosCount,
                        hiddenVideoUris = hiddenVideoUris,
                        isSelectionMode = isSelectionMode,
                        selectedUris = selectedUris,
                        onToggleSelectUri = onToggleSelectUri,
                        onEnterSelectionMode = onEnterSelectionMode,
                        onSelectAll = onSelectAll,
                        onDeselectAll = onDeselectAll,
                        onUnlockVaultRequested = {
                            pinInput = ""
                            pinErrorText = null
                            isChangingPin = false
                            showPrivateVaultDialog = true
                        },
                        onChangePinRequested = {
                            pinInput = ""
                            pinErrorText = null
                            isChangingPin = true
                            showPrivateVaultDialog = true
                        },
                        onLockToggle = onToggleLockVideo,
                        onLockToggleFolder = onToggleLockFolder,
                        onToggleGridView = { isGridView = !isGridView },
                        onVideoSelected = { video ->
                            val index = videos.indexOfFirst { it.id == video.id }
                            if (index != -1) onVideoSelected(index)
                        },
                        onVideoLongClick = { video ->
                            onEnterSelectionMode(video)
                        },
                        onDeleteRequested = { video ->
                            videoToDelete = video
                        },
                        onInfoRequested = { video ->
                            videoDetailsToShow = video
                        },
                        onFolderSelected = { folderName ->
                            viewModel.selectFolder(if (selectedFolder == folderName) null else folderName)
                        },
                        onNavigateToRecent = { currentTab = BottomNavTab.RECENT }
                    )
                }
                BottomNavTab.RECENT -> {
                    RecentWatchHistoryContent(
                        continueWatching = continueWatching,
                        videos = videos,
                        imageLoader = imageLoader,
                        hiddenVideoUris = hiddenVideoUris,
                        onClearHistory = { viewModel.clearPlaybackHistory() },
                        onVideoSelected = { video ->
                            val index = videos.indexOfFirst { it.id == video.id }
                            if (index != -1) onVideoSelected(index)
                        },
                        onVideoLongClick = { video ->
                            videoToDelete = video
                        },
                        onDeleteRequested = { video ->
                            videoToDelete = video
                        },
                        onInfoRequested = { video ->
                            videoDetailsToShow = video
                        },
                        onLockToggle = onToggleLockVideo
                    )
                }
                BottomNavTab.NETWORK -> {
                    NetworkStreamContent(
                        recentStreams = recentStreams,
                        onPlayStream = { url ->
                            viewModel.addNetworkStream(url)
                            viewModel.showToast("Streaming $url")
                            val index = videos.indexOfFirst { it.uri.toString() == url }
                            if (index != -1) onVideoSelected(index)
                            else if (videos.isNotEmpty()) onVideoSelected(0)
                        }
                    )
                }
                BottomNavTab.SETTINGS -> {
                    SettingsContent(
                        viewModel = viewModel,
                        storageStats = storageStats,
                        totalVideosCount = videos.size,
                        onChangePin = {
                            pinInput = ""
                            pinErrorText = null
                            isChangingPin = true
                            showPrivateVaultDialog = true
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceContainerHighestDark,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(toastMessage ?: "", color = OnSurfaceDark, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showStreamDialog) {
        AlertDialog(
            onDismissRequest = { showStreamDialog = false },
            containerColor = SurfaceContainerDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Network Stream", color = OnSurfaceDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Enter any HTTP, HTTPS, HLS, or RTSP video link:", color = OnSurfaceVariantDark, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = streamUrlInput,
                        onValueChange = { streamUrlInput = it },
                        placeholder = { Text("https://example.com/video.mp4", color = OutlineDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = OutlineVariantDark,
                            focusedTextColor = OnSurfaceDark,
                            unfocusedTextColor = OnSurfaceDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (streamUrlInput.isNotBlank()) {
                            viewModel.addNetworkStream(streamUrlInput)
                            showStreamDialog = false
                            viewModel.showToast("Opening stream...")
                            if (videos.isNotEmpty()) onVideoSelected(0)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = OnPrimaryCyan)
                ) {
                    Text("Play Stream")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStreamDialog = false }) {
                    Text("Cancel", color = OnSurfaceVariantDark)
                }
            }
        )
    }

    if (showPrivateVaultDialog) {
        val isSettingPin = vaultPin.isNullOrEmpty() || isChangingPin
        val pendingTargetDesc = when {
            batchPendingHide != null -> "${batchPendingHide!!.size} selected videos"
            folderPendingHide != null -> "folder '$folderPendingHide'"
            videoPendingHide != null -> "video '${videoPendingHide!!.name}'"
            else -> null
        }
        AlertDialog(
            onDismissRequest = {
                showPrivateVaultDialog = false
                videoPendingHide = null
                folderPendingHide = null
                batchPendingHide = null
                pinErrorText = null
                isChangingPin = false
                pinInput = ""
            },
            containerColor = SurfaceContainerDark,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(TertiaryAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isSettingPin) Icons.Default.Key else Icons.Default.Lock,
                        contentDescription = null,
                        tint = TertiaryAmber,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = when {
                        isChangingPin -> "Change Security PIN"
                        isSettingPin -> "Create Safe Folder PIN"
                        else -> "Unlock Private Safe"
                    },
                    color = OnSurfaceDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = when {
                            isSettingPin && pendingTargetDesc != null ->
                                "Please set a 4-digit PIN first to hide and secure $pendingTargetDesc in your Private Safe."
                            isSettingPin ->
                                "Set a 4-digit PIN to encrypt and hide videos and folders in your Private Safe."
                            pendingTargetDesc != null ->
                                "Enter your 4-digit PIN to confirm moving $pendingTargetDesc to Private Safe:"
                            else ->
                                "Enter your 4-digit security PIN to access protected video files:"
                        },
                        color = OnSurfaceVariantDark,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            val digitsOnly = it.filter { ch -> ch.isDigit() }
                            if (digitsOnly.length <= 4) {
                                pinInput = digitsOnly
                                pinErrorText = null
                            }
                        },
                        placeholder = { Text("••••", color = OutlineDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinErrorText != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TertiaryAmber,
                            unfocusedBorderColor = OutlineVariantDark,
                            focusedTextColor = OnSurfaceDark,
                            unfocusedTextColor = OnSurfaceDark,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorTextColor = MaterialTheme.colorScheme.error
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (pinErrorText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pinErrorText!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinErrorText = "PIN must be exactly 4 digits"
                            return@Button
                        }
                        if (isSettingPin) {
                            viewModel.setVaultPin(pinInput)
                            isChangingPin = false
                            if (batchPendingHide != null) {
                                viewModel.hideVideos(batchPendingHide!!)
                                batchPendingHide = null
                                isSelectionMode = false
                                selectedUris = emptySet()
                            }
                            if (folderPendingHide != null) {
                                viewModel.hideFolder(folderPendingHide!!)
                                folderPendingHide = null
                            }
                            if (videoPendingHide != null) {
                                viewModel.hideVideo(videoPendingHide!!.uri)
                                videoPendingHide = null
                            }
                            showPrivateVaultDialog = false
                            pinInput = ""
                            pinErrorText = null
                        } else {
                            if (viewModel.verifyVaultPin(pinInput)) {
                                if (batchPendingHide != null) {
                                    viewModel.hideVideos(batchPendingHide!!)
                                    batchPendingHide = null
                                    isSelectionMode = false
                                    selectedUris = emptySet()
                                }
                                if (folderPendingHide != null) {
                                    viewModel.hideFolder(folderPendingHide!!)
                                    folderPendingHide = null
                                }
                                if (videoPendingHide != null) {
                                    viewModel.hideVideo(videoPendingHide!!.uri)
                                    videoPendingHide = null
                                } else if (batchPendingHide == null && folderPendingHide == null) {
                                    viewModel.setFilterCategory(FilterCategory.HIDDEN)
                                }
                                showPrivateVaultDialog = false
                                pinInput = ""
                                pinErrorText = null
                            } else {
                                pinErrorText = "Incorrect PIN. Please try again."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TertiaryAmber,
                        contentColor = OnTertiaryAmber
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        when {
                            isSettingPin && pendingTargetDesc != null -> "Save PIN & Hide"
                            isSettingPin -> "Save PIN"
                            pendingTargetDesc != null -> "Confirm & Hide"
                            else -> "Unlock"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPrivateVaultDialog = false
                        videoPendingHide = null
                        folderPendingHide = null
                        batchPendingHide = null
                        pinErrorText = null
                        isChangingPin = false
                        pinInput = ""
                    }
                ) {
                    Text("Cancel", color = OnSurfaceVariantDark)
                }
            }
        )
    }

    if (showBatchDeleteDialog) {
        val selectedVideos = filteredVideos.filter { selectedUris.contains(it.uri) }
        BatchDeleteVideosDialog(
            videos = selectedVideos,
            imageLoader = imageLoader,
            onDismiss = { showBatchDeleteDialog = false },
            onConfirm = {
                viewModel.deleteVideos(selectedUris)
                showBatchDeleteDialog = false
                isSelectionMode = false
                selectedUris = emptySet()
            }
        )
    }

    if (showCastDialog) {
        AlertDialog(
            onDismissRequest = { showCastDialog = false },
            containerColor = SurfaceContainerDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cast, contentDescription = null, tint = PrimaryCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect to Cast Device", color = OnSurfaceDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    CircularProgressIndicator(color = PrimaryCyan, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Searching for Chromecast and Smart TVs on Wi-Fi...", color = OnSurfaceVariantDark, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCastDialog = false }) {
                    Text("Close", color = PrimaryCyan)
                }
            }
        )
    }

    if (videoToDelete != null) {
        DeleteVideoDialog(
            video = videoToDelete!!,
            imageLoader = imageLoader,
            onDismiss = { videoToDelete = null },
            onConfirm = {
                val toDelete = videoToDelete
                if (toDelete != null) {
                    viewModel.deleteVideo(toDelete.uri)
                }
                videoToDelete = null
            }
        )
    }

    if (videoDetailsToShow != null) {
        val detailsVideo = videoDetailsToShow!!
        VideoDetailsDialog(
            video = detailsVideo,
            isLocked = hiddenVideoUris.contains(detailsVideo.uri.toString()),
            onLockToggle = { onToggleLockVideo(detailsVideo) },
            onDismiss = { videoDetailsToShow = null }
        )
    }
}
