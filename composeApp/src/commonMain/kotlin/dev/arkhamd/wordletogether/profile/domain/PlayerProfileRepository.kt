package dev.arkhamd.wordletogether.profile.domain

import kotlinx.coroutines.flow.Flow

interface PlayerProfileRepository {
    val stats: Flow<PlayerStats>

    suspend fun incWinCount()
    suspend fun incLoseCount()
    suspend fun incDrawCount()
}
