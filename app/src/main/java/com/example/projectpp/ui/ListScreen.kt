package com.example.projectpp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpp.data.FoodItem

private val colorExpired = Color(0xFFCC2B2B)
private val colorWarning = Color(0xFFFFA000)
private val colorSafe = Color(0xFF49D17C)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    uiState: FoodUiState,
    onEdit: (FoodItem) -> Unit,
    onDelete: (FoodItem) -> Unit,
    onClear: () -> Unit
) {
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Untuk Hapus Item
    if (itemToDelete != null) {
        val item = itemToDelete!!
        DeleteConfirmationDialog(
            title = "Hapus Produk",
            text = "Anda yakin ingin menghapus '${item.name}'?",
            onConfirm = {
                onDelete(item)
                itemToDelete = null
            },
            onDismiss = {
                itemToDelete = null
            }
        )
    }

    // Untuk Hapus Semua
    if (showDeleteAllDialog) {
        DeleteConfirmationDialog(
            title = "Hapus Semua Produk",
            text = "Anda yakin ingin menghapus SEMUA produk? Tindakan ini tidak dapat dibatalkan.",
            onConfirm = {
                onClear()
                showDeleteAllDialog = false
            },
            onDismiss = {
                showDeleteAllDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState) {
            FoodUiState.Loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Memuat data...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            FoodUiState.Empty -> {
                // Ditangani HomeHost
            }

            is FoodUiState.Success -> {
                val groups = uiState.groupedFoods

                val expiredList = groups[ExpirationStatus.EXPIRED] ?: emptyList()
                val soonList = groups[ExpirationStatus.EXPIRING_SOON] ?: emptyList()
                val safeList = groups[ExpirationStatus.SAFE] ?: emptyList()

                // BAGIAN KADALUWARSA (Merah)
                if (expiredList.isNotEmpty()) {
                    stickyHeader {
                        GroupHeader(title = ExpirationStatus.EXPIRED.title)
                    }
                    items(items = expiredList, key = { it.item.id }) { itemUi ->
                        FoodCard(
                            itemUi = itemUi,
                            onEdit = { onEdit(itemUi.item) },
                            onDelete = { itemToDelete = itemUi.item },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // BAGIAN AKAN KADALUWARSA (Kuning)
                if (soonList.isNotEmpty()) {
                    stickyHeader {
                        GroupHeader(title = ExpirationStatus.EXPIRING_SOON.title)
                    }
                    items(items = soonList, key = { it.item.id }) { itemUi ->
                        FoodCard(
                            itemUi = itemUi,
                            onEdit = { onEdit(itemUi.item) },
                            onDelete = { itemToDelete = itemUi.item },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // BAGIAN AMAN (Hijau)
                if (safeList.isNotEmpty()) {
                    stickyHeader {
                        GroupHeader(title = ExpirationStatus.SAFE.title)
                    }
                    items(items = safeList, key = { it.item.id }) { itemUi ->
                        FoodCard(
                            itemUi = itemUi,
                            onEdit = { onEdit(itemUi.item) },
                            onDelete = { itemToDelete = itemUi.item },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Tombol Hapus Semua
                item {
                    OutlinedButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Hapus Semua")
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 1.dp
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FoodCard(
    itemUi: FoodItemUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysLeft = itemUi.daysLeft

    val tone = when {
        daysLeft <= 0 -> colorExpired
        daysLeft <= 3 -> colorWarning
        else -> colorSafe
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = tone),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolom Kiri: Nama Produk & Sisa Hari
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = itemUi.item.name,
                    style = MaterialTheme.typography.titleMedium, // Sedikit diperbesar juga biar seimbang
                    fontWeight = FontWeight.Bold
                )
                Text("Sisa hari: $daysLeft")
            }

            // Kolom Kanan: Kategori & Ikon Hapus
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Tampilkan Kategori di atas
                Text(
                    text = itemUi.item.category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Tombol Hapus
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Ya, Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tidak")
            }
        }
    )
}