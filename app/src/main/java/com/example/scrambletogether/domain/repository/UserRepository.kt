package com.example.scrambletogether.domain.repository

interface UserRepository {
    suspend fun incWinCount()
    suspend fun incLoseCount()
    suspend fun incDrawCount()
}