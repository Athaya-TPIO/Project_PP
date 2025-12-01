package com.example.projectpp

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectpp.data.FoodItem
import com.example.projectpp.ui.* import com.example.projectpp.ui.theme.ProjectPPTheme
import com.example.projectpp.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val reqNotif = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            reqNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            var screen by remember { mutableStateOf("splash") }
            var selected by remember { mutableStateOf<FoodItem?>(null) }
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            val vm: FoodViewModel = viewModel()

            val userName by vm.userName.collectAsStateWithLifecycle()
            val userDescription by vm.userDescription.collectAsStateWithLifecycle()
            val categoriesMap by vm.categories.collectAsStateWithLifecycle()
            val categoryList = categoriesMap.keys.toList()
            val selectedCategory by vm.selectedCategory.collectAsStateWithLifecycle()
            val isTestMode by vm.isTestMode.collectAsStateWithLifecycle()
            val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()

            val isBackHandlerEnabled = drawerState.isOpen || screen != "home"

            BackHandler(enabled = isBackHandlerEnabled) {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (screen == "profile") {
                    screen = "settings"
                } else if (screen == "settings" || screen == "kategori") {
                    screen = "home"
                    scope.launch { drawerState.open() }
                } else {
                    screen = "home"
                }
            }

            ProjectPPTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = (screen == "home"),
                    drawerContent = {
                        AppDrawer(
                            userName = userName,
                            userDescription = userDescription,
                            categories = categoriesMap,
                            selectedCategory = selectedCategory,
                            onClose = { scope.launch { drawerState.close() } },
                            onSemuaClicked = {
                                vm.selectCategory(null)
                                screen = "home"
                                scope.launch { drawerState.close() }
                            },
                            onKategoriManageClicked = {
                                screen = "kategori"
                                scope.launch { drawerState.close() }
                            },
                            onCategoryFilterClicked = { categoryName ->
                                vm.selectCategory(categoryName)
                                screen = "home"
                                scope.launch { drawerState.close() }
                            },
                            onPengaturanClicked = {
                                screen = "settings"
                                scope.launch { drawerState.close() }
                            },
                            onLayananClicked = {
                                scope.launch {
                                    drawerState.close()
                                    snackbarHostState.showSnackbar("Layanan (coming soon)")
                                }
                            }
                        )
                    }
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            if (screen != "splash") {
                                TopBar(
                                    isHome = (screen == "home"),
                                    title = when (screen) {
                                        "add" -> "Tambah Produk"
                                        "edit" -> "Edit Produk"
                                        "settings" -> "Pengaturan"
                                        "kategori" -> "Kelola Kategori"
                                        "profile" -> "Profil Pengguna"
                                        else -> selectedCategory ?: "Semua"
                                    },
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { vm.onSearchQueryChange(it) },
                                    onMenu = { scope.launch { drawerState.open() } },
                                    onBack = {
                                        if (screen == "profile") {
                                            screen = "settings"
                                        } else if (screen == "settings" || screen == "kategori") {
                                            screen = "home"
                                            scope.launch { drawerState.open() }
                                        } else {
                                            screen = "home"
                                        }
                                    }
                                )
                            }
                        },
                        floatingActionButton = {
                            if (screen == "home") {
                                FloatingActionButton(onClick = { screen = "add" }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Tambah")
                                }
                            }
                        },
                        contentWindowInsets = WindowInsets(0)
                    ) { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(inner)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.background_app),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            when (screen) {
                                "splash" -> SplashScreen(
                                    onTimeout = { screen = "home" }
                                )

                                "add" -> AddFoodScreen(
                                    vm = vm,
                                    onSaved = {
                                        screen = "home"
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Produk tersimpan")
                                        }
                                    },
                                    categories = categoryList
                                )

                                "edit" -> selected?.let { item ->
                                    EditFoodScreen(
                                        vm = vm,
                                        item = item,
                                        onSaved = {
                                            screen = "home"
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Perubahan tersimpan")
                                            }
                                        },
                                        categories = categoryList
                                    )
                                }

                                "settings" -> SettingsScreen(
                                    onProfileClick = {
                                        screen = "profile"
                                    },
                                    onLanguageClick = {
                                        scope.launch { snackbarHostState.showSnackbar("Bahasa (Coming Soon)") }
                                    },
                                    isTestMode = isTestMode,
                                    onTestModeChange = { vm.setTestMode(it) }
                                )

                                "profile" -> ProfileScreen(
                                    currentName = userName,
                                    currentDescription = userDescription,
                                    onSave = { name, desc ->
                                        vm.saveProfile(name, desc)
                                        screen = "settings"
                                        scope.launch { snackbarHostState.showSnackbar("Profil disimpan!") }
                                    }
                                )

                                "kategori" -> CategoryScreen(
                                    categories = categoriesMap,
                                    onCategoryClick = { categoryName ->
                                        vm.selectCategory(categoryName)
                                        screen = "home"
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Menampilkan kategori: $categoryName")
                                        }
                                    },
                                    onCategoryAdd = { categoryName ->
                                        val dummyItem = FoodItem(
                                            name = "Kategori: $categoryName",
                                            category = categoryName,
                                            quantity = 0,
                                            expirationDate = 0L
                                        )
                                        vm.insertAndSchedule(dummyItem)
                                        scope.launch { snackbarHostState.showSnackbar("Kategori '$categoryName' ditambahkan!") }
                                    },
                                    onCategoryRename = { oldName, newName ->
                                        vm.renameCategory(oldName, newName)
                                        scope.launch { snackbarHostState.showSnackbar("Kategori diubah") }
                                    },
                                    onCategoryDelete = { categoryName ->
                                        vm.deleteCategory(categoryName)
                                        scope.launch { snackbarHostState.showSnackbar("Kategori '$categoryName' dihapus") }
                                    }
                                )

                                else -> HomeHost(
                                    vm = vm,
                                    onAdd = { screen = "add" },
                                    onEdit = { item ->
                                        selected = item
                                        screen = "edit"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(true) {
        delay(2000)
        onTimeout()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo Aplikasi",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    isHome: Boolean,
    title: String,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onMenu: () -> Unit,
    onBack: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    if (isSearchActive && isHome) {
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Cari produk...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    isSearchActive = false
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tutup Cari")
                }
            },
            actions = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Text("X", fontWeight = FontWeight.Bold)
                    }
                }
            },
            colors = topAppBarColors(
                containerColor = Color.White.copy(alpha = 0.9f),
                scrolledContainerColor = Color.White.copy(alpha = 0.9f)
            )
        )
    } else {
        TopAppBar(
            title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                if (isHome) {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                } else {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            },
            actions = {
                if (isHome) {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(Icons.Filled.Search, contentDescription = "Cari")
                    }
                }
            },
            colors = topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun AppDrawer(
    userName: String,
    userDescription: String,
    categories: Map<String, Int>,
    selectedCategory: String?,
    onClose: () -> Unit,
    onSemuaClicked: () -> Unit,
    onCategoryFilterClicked: (String) -> Unit,
    onKategoriManageClicked: () -> Unit,
    onPengaturanClicked: () -> Unit,
    onLayananClicked: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.8f),
        drawerContainerColor = Color(0xFF121212),
        drawerContentColor = Color.White
    ) {
        // Header Profil
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "P",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(userName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White)
                Text(userDescription, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)
            }
        }


        Divider(color = Color.Gray)

        // Item Filter "Semua"
        NavigationDrawerItem(
            label = { Text("Semua") },
            selected = (selectedCategory == null),
            onClick = onSemuaClicked,
            icon = { Icon(Icons.Filled.Apps, contentDescription = "Semua") },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedTextColor = Color.White,
                unselectedIconColor = Color.White,
                selectedContainerColor = GreenPrimary, // Hijau saat aktif
                selectedTextColor = Color.White,
                selectedIconColor = Color.White
            )
        )

        categories.entries.forEach { (categoryName, count) ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = "- $categoryName ($count)",
                        modifier = Modifier.padding(start = 36.dp)
                    )
                },
                selected = (selectedCategory == categoryName),
                onClick = { onCategoryFilterClicked(categoryName) },
                icon = null,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = Color.White,
                    selectedContainerColor = GreenPrimary,
                    selectedTextColor = Color.White
                )
            )
        }

        Divider(color = Color.Gray)

        // Item Navigasi (Kelola, Pengaturan, Layanan)
        val itemColors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = Color.White,
            unselectedIconColor = Color.White,
            selectedContainerColor = Color.Transparent,
            selectedTextColor = Color.White,
            selectedIconColor = Color.White
        )

        NavigationDrawerItem(
            label = { Text("Kelola Kategori") },
            selected = false,
            onClick = onKategoriManageClicked,
            icon = { Icon(Icons.Filled.Category, contentDescription = null) },
            colors = itemColors
        )

        NavigationDrawerItem(
            label = { Text("Pengaturan") },
            selected = false,
            onClick = onPengaturanClicked,
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            colors = itemColors
        )
        NavigationDrawerItem(
            label = { Text("Layanan") },
            selected = false,
            onClick = onLayananClicked,
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            colors = itemColors
        )

        Spacer(Modifier.height(24.dp))
    }
}

// (HomeHost, EmptyState, ActionCard, PlaceholderScreen)
@Composable
private fun HomeHost(
    vm: FoodViewModel,
    onAdd: () -> Unit,
    onEdit: (FoodItem) -> Unit
) {
    val uiState by vm.foods.collectAsStateWithLifecycle()

    when (uiState) {
        FoodUiState.Loading -> {
            EmptyState(onAdd)
        }
        FoodUiState.Empty -> {
            EmptyState(onAdd)
        }
        is FoodUiState.Success -> {
            ListScreen(
                uiState = uiState,
                onEdit = onEdit,
                onDelete = { vm.deleteAndCancel(it) },
                onClear = { vm.clearAll() }
            )
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionCard(
            iconRes = R.drawable.app_logo,
            title = "Tambahkan Produk",
            subtitle = "Tambahkan produk dan dapatkan pengingat sebelum kedaluwarsa.",
            onClick = onAdd
        )
    }
}

@Composable
private fun ActionCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.90f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAEAEA)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}