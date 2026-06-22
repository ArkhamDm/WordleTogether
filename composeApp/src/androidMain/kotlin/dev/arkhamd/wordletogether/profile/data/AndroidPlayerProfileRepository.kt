package dev.arkhamd.wordletogether.profile.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.arkhamd.wordletogether.profile.domain.PlayerProfileRepository
import dev.arkhamd.wordletogether.profile.domain.PlayerStats
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val PREFERENCE_NAME = "win_lose_count_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

class AndroidPlayerProfileRepository(context: Context) : PlayerProfileRepository {
    private val dataStore = context.dataStore

    private companion object {
        val WIN_COUNT = intPreferencesKey("win_count")
        val LOSE_COUNT = intPreferencesKey("lose_count")
        val DRAW_COUNT = intPreferencesKey("draw_count")
        const val TAG = "AndroidPlayerProfileRepository"
    }

    override val stats: Flow<PlayerStats> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preference WIN", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { pref ->
            PlayerStats(
                pref[WIN_COUNT] ?: 0,
                pref[LOSE_COUNT] ?: 0,
                pref[DRAW_COUNT] ?: 0
            )
        }

    override suspend fun incWinCount() {
        dataStore.edit { data ->
            data[WIN_COUNT] = data[WIN_COUNT]?.inc() ?: 1
        }
    }

    override suspend fun incLoseCount() {
        dataStore.edit { data ->
            data[LOSE_COUNT] = data[LOSE_COUNT]?.inc() ?: 1
        }
    }

    override suspend fun incDrawCount() {
        dataStore.edit { data ->
            data[DRAW_COUNT] = data[DRAW_COUNT]?.inc() ?: 1
        }
    }
}
