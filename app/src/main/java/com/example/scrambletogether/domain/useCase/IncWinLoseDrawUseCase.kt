package com.example.scrambletogether.domain.useCase

import com.example.scrambletogether.domain.repository.UserRepository

class IncWinLoseDrawUseCase(private val userRepository: UserRepository) {
    suspend fun win() {
        userRepository.incWinCount()
    }

    suspend fun lose() {
        userRepository.incLoseCount()
    }

    suspend fun draw() {
        userRepository.incDrawCount()
    }
}