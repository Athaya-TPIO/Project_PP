package com.example.projectpp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectpp.data.FoodItem
import java.util.Calendar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.example.projectpp.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    vm: FoodViewModel,
    item: FoodItem,
    onSaved: () -> Unit,
    categories: List<String>
) {
    var name by rememberSaveable(item.id) { mutableStateOf(item.name) }
    var category by rememberSaveable(item.id) { mutableStateOf(item.category) }
    var qty by rememberSaveable(item.id) { mutableStateOf(item.quantity.toString()) }
    var note by rememberSaveable(item.id) { mutableStateOf(item.note) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    var expMillis by rememberSaveable(item.id) { mutableStateOf(item.expirationDate) }
    // Menggunakan formatDate dari AddFoodScreen.kt
    var expLabel by rememberSaveable(expMillis) { mutableStateOf(formatDate(expMillis)) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expMillis,
        yearRange = (Calendar.getInstance().get(Calendar.YEAR) - 5)..(Calendar.getInstance().get(Calendar.YEAR) + 100)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            val c = Calendar.getInstance().apply { timeInMillis = it }
                            c.set(Calendar.HOUR_OF_DAY, 7)
                            expMillis = c.timeInMillis
                            expLabel = formatDate(expMillis)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = GreenPrimary)
                ) { Text("Batal") }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = GreenPrimary,
                headlineContentColor = GreenPrimary,
                weekdayContentColor = GreenPrimary,
                subheadContentColor = Color.Black,
                yearContentColor = Color.Black,
                currentYearContentColor = GreenPrimary,
                selectedYearContainerColor = GreenPrimary,
                selectedYearContentColor = Color.White,
                dayContentColor = Color.Black,
                selectedDayContainerColor = GreenPrimary,
                selectedDayContentColor = Color.White,
                todayContentColor = GreenPrimary,
                todayDateBorderColor = GreenPrimary
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = GreenPrimary,
                    headlineContentColor = GreenPrimary,
                    weekdayContentColor = GreenPrimary,
                    subheadContentColor = Color.Black,
                    yearContentColor = Color.Black,
                    currentYearContentColor = GreenPrimary,
                    selectedYearContainerColor = GreenPrimary,
                    selectedYearContentColor = Color.White,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = GreenPrimary,
                    selectedDayContentColor = Color.White,
                    todayContentColor = GreenPrimary,
                    todayDateBorderColor = GreenPrimary
                )
            )
        }
    }

    val canSave = name.isNotBlank() && (qty.toIntOrNull() ?: 0) > 0 && category.isNotBlank()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Produk", style = MaterialTheme.typography.titleMedium, color = Color.Black)

                // Menggunakan NameInputField dari AddFoodScreen.kt
                NameInputField(name = name, onValueChange = { name = it })

                // Dropdown Kategori
                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Kategori") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = GreenPrimary,
                            unfocusedLabelColor = Color.Gray,
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Belum ada kategori.", color = Color.Gray) },
                                onClick = { isCategoryExpanded = false },
                                enabled = false
                            )
                        } else {
                            categories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption, color = Color.Black) },
                                    onClick = {
                                        category = selectionOption
                                        isCategoryExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                // Tanggal Kedaluwarsa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { showDatePicker = true }, role = Role.Button)
                ) {
                    OutlinedTextField(
                        value = expLabel,
                        onValueChange = {},
                        label = { Text("Tanggal kedaluwarsa") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledContainerColor = Color.White,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray,
                            disabledTrailingIconColor = Color.Gray
                        ),
                        trailingIcon = {
                            Icon(Icons.Filled.DateRange, contentDescription = "Pilih Tanggal", tint = GreenPrimary)
                        }
                    )
                }

                // Catatan
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    placeholder = { Text("Contoh: Simpan di kulkas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = GreenPrimary,
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                // Menggunakan QuantityInputField dari AddFoodScreen.kt
                QuantityInputField(qty = qty, onValueChange = { qty = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val updatedFood = item.copy(
                    name = name.trim(),
                    category = category,
                    quantity = qty.toInt(),
                    expirationDate = expMillis,
                    note = note.trim()
                )
                vm.updateAndReschedule(updatedFood)
                onSaved()
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text("Simpan Perubahan")
        }
    }
}

// File ini akan menggunakan definisi yang ada di AddFoodScreen.kt