package com.example.projectpp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    vm: FoodViewModel,
    onSaved: () -> Unit,
    categories: List<String>
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(categories.firstOrNull() ?: "") }
    var qty by rememberSaveable { mutableStateOf("1") }
    var note by rememberSaveable { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val initialCal = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }
    }
    var expMillis by rememberSaveable { mutableStateOf(initialCal.timeInMillis) }
    var expLabel by rememberSaveable(expMillis) { mutableStateOf(formatDate(expMillis)) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expMillis,
        yearRange = (initialCal.get(Calendar.YEAR))..(initialCal.get(Calendar.YEAR) + 100)
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
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Detail Produk", style = MaterialTheme.typography.titleMedium, color = Color.Black)

                NameInputField(name = name, onValueChange = { name = it })

                ExposedDropdownMenuBox(
                    expanded = isCategoryExpanded,
                    onExpandedChange = { isCategoryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (category.isBlank()) "Pilih Kategori" else category,
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
                                text = { Text("Belum ada kategori, tambahkan di kelola kategori terlebih dahulu.", color = Color.Gray) },
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

                Box(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = { showDatePicker = true }, role = Role.Button)
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
                        trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Pilih Tanggal", tint = GreenPrimary) }
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    placeholder = { Text("Contoh: Simpan di kulkas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4,
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

                QuantityInputField(qty = qty, onValueChange = { qty = it })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val food = FoodItem(
                    name = name.trim(),
                    category = category,
                    quantity = qty.toInt(),
                    expirationDate = expMillis,
                    note = note.trim()
                )
                vm.insertAndSchedule(food)
                onSaved()
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
        ) {
            Text("Simpan")
        }
    }
}

@Composable
fun NameInputField(name: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = name,
        onValueChange = onValueChange,
        label = { Text("Nama Bahan") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
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
}

@Composable
fun QuantityInputField(qty: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = qty,
        onValueChange = { text ->
            if (text.all { it.isDigit() }) onValueChange(text)
        },
        label = { Text("Jumlah") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
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
}

fun formatDate(millis: Long): String {
    if (millis == 0L) return ""
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    val y = c.get(Calendar.YEAR)
    val m = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val d = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "$y-$m-$d"
}