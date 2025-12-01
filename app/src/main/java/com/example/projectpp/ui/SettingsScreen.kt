package com.example.projectpp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projectpp.ui.theme.GreenPrimary

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    isTestMode: Boolean,
    onTestModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Pengaturan Aplikasi",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            color = Color.White,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("Profil Pengguna", color = Color.Black) },
                    supportingContent = { Text("Lihat dan edit informasi dasar Anda.", color = Color.Black) },
                    leadingContent = {
                        Icon(Icons.Filled.Person, contentDescription = "Profil", tint = Color.Black)
                    },
                    modifier = Modifier.clickable(onClick = onProfileClick),
                    colors = ListItemDefaults.colors(containerColor = Color.White)
                )

                Divider(color = Color.LightGray)

                ListItem(
                    headlineContent = { Text("Bahasa", color = Color.Black) },
                    supportingContent = { Text("Ubah bahasa aplikasi.", color = Color.Black) },
                    leadingContent = {
                        Icon(Icons.Filled.Language, contentDescription = "Bahasa", tint = Color.Black)
                    },
                    modifier = Modifier.clickable(onClick = onLanguageClick),
                    colors = ListItemDefaults.colors(containerColor = Color.White)
                )

                Divider(color = Color.LightGray)

                ListItem(
                    headlineContent = { Text("Test Notifikasi Cepat", color = Color.Black) },
                    supportingContent = { Text("Notifikasi muncul 10 detik setelah simpan (untuk testing).", color = Color.DarkGray) },
                    leadingContent = {
                        Icon(Icons.Filled.Notifications, contentDescription = "Test Notif", tint = Color.Black)
                    },
                    trailingContent = {
                        Switch(
                            checked = isTestMode,
                            onCheckedChange = onTestModeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GreenPrimary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White
                            )
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.White)
                )
            }
        }
    }
}