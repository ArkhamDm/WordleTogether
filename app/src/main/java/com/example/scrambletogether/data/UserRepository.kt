package com.example.scrambletogether.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val PREFERENCE_NAME = "win_lose_count_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

class UserRepository(
    context: Context
) {
    private val dataStore = context.dataStore

    private companion object {
        val WIN_COUNT = intPreferencesKey("win_count")
        val LOSE_COUNT = intPreferencesKey("lose_count")
        const val TAG = "UserRepository"
    }

    val winLoseCount: Flow<UserRepo> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preference WIN", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { pref ->
            UserRepo(pref[WIN_COUNT] ?: 0, pref[LOSE_COUNT] ?: 0)
        }

    suspend fun incWinCount() {
        dataStore.edit { data ->
            data[WIN_COUNT] = data[WIN_COUNT]?.inc() ?: 1
        }
    }
    suspend fun incLoseCount() {
        dataStore.edit { data ->
            data[LOSE_COUNT] = data[LOSE_COUNT]?.inc() ?: 1
        }
    }
}

data class UserRepo(
    var winCount: Int = 0,
    var loseCount: Int = 0
)
