package com.example.projectpp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryScreen(
    categories: Map<String, Int>,
    onCategoryClick: (String) -> Unit,
    onCategoryAdd: (String) -> Unit,
    onCategoryRename: (oldName: String, newName: String) -> Unit,
    onCategoryDelete: (String) -> Unit
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name ->
                if (name.isNotBlank()) onCategoryAdd(name)
                showAddCategoryDialog = false
            }
        )
    }

    if (showOptionsDialog && selectedCategory != null) {
        CategoryOptionsDialog(
            categoryName = selectedCategory!!,
            onDismiss = { showOptionsDialog = false; selectedCategory = null },
            onRename = { showOptionsDialog = false; showRenameDialog = true },
            onDelete = { onCategoryDelete(selectedCategory!!); showOptionsDialog = false; selectedCategory = null }
        )
    }

    if (showRenameDialog && selectedCategory != null) {
        RenameCategoryDialog(
            oldName = selectedCategory!!,
            onDismiss = { showRenameDialog = false; selectedCategory = null },
            onRename = { old, new -> onCategoryRename(old, new); showRenameDialog = false; selectedCategory = null }
        )
    }

    Scaffold(
        // Set Transparan agar gambar background MainActivity terlihat
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCategoryDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Kategori")
            }
        }
    ) { paddingValues ->
        val totalItems = categories.values.sum()

        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Belum ada kategori.", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text("Klik tombol + untuk menambah kategori baru.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = paddingValues.let {
                PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = it.calculateBottomPadding() + 80.dp)
            },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // Header Kategori
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Daftar Kategori Produk ($totalItems item)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(
                items = categories.entries.toList(),
                key = { it.key }
            ) { (categoryName, count) ->
                // Setiap item dibungkus Card Putih
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    CategoryItem(
                        categoryName = categoryName,
                        count = count,
                        onClick = {
                            selectedCategory = categoryName
                            showOptionsDialog = true
                        }
                    )
                }
            }
        }
    }
}

// Dialog Helper
@Composable
private fun CategoryOptionsDialog(categoryName: String, onDismiss: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(categoryName, color = Color.Black) },
        text = { Text("Pilih tindakan untuk kategori ini.", color = Color.Black) },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDelete) { Text("Hapus Kategori", color = MaterialTheme.colorScheme.error) }
                Row {
                    TextButton(onClick = onRename) { Text("Ganti Nama") }
                }
            }
        },
        containerColor = Color.White, // Paksa Putih
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}

@Composable
private fun RenameCategoryDialog(oldName: String, onDismiss: () -> Unit, onRename: (oldName: String, newName: String) -> Unit) {
    var newName by rememberSaveable(oldName) { mutableStateOf(oldName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Nama Kategori", color = Color.Black) },
        text = {
            OutlinedTextField(
                value = newName, onValueChange = { newName = it },
                label = { Text("Nama Kategori Baru") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        },
        confirmButton = {
            Button(onClick = { onRename(oldName, newName.trim()) }, enabled = newName.isNotBlank() && newName != oldName) { Text("Ubah") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var categoryName by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kategori Baru", color = Color.Black) },
        text = {
            OutlinedTextField(
                value = categoryName, onValueChange = { categoryName = it },
                label = { Text("Nama Kategori") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
        },
        confirmButton = {
            Button(onClick = { onSave(categoryName.trim()) }, enabled = categoryName.isNotBlank()) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}

@Composable
fun CategoryItem(categoryName: String, count: Int, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(categoryName, color = Color.Black) },
        supportingContent = { Text("$count item", color = Color.Gray) },
        leadingContent = { Icon(Icons.Filled.Category, contentDescription = null, tint = Color.Black) },
        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Black) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.White)
    )
}