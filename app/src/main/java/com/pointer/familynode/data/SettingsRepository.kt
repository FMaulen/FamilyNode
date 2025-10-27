package com.pointer.familynode.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val SELECTED_GROUP_ID = stringPreferencesKey("selected_group_id")

    val selectedGroupIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_GROUP_ID]
        }

    suspend fun saveSelectedGroupId(groupId: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_GROUP_ID] = groupId
        }
    }
}