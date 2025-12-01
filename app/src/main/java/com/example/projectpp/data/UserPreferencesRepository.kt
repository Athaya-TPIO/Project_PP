package com.example.projectpp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_DESCRIPTION = stringPreferencesKey("user_description")
        val IS_TEST_MODE = booleanPreferencesKey("is_test_mode") // BARU
    }

    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "Alexander Dennis"
        }

    val userDescription: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_DESCRIPTION] ?: "CF Player"
        }

    val isTestMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_TEST_MODE] ?: false
        }

    suspend fun saveProfile(name: String, description: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_DESCRIPTION] = description
        }
    }

    suspend fun saveTestMode(isTest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TEST_MODE] = isTest
        }
    }
}