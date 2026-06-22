package dev.arkhamd.wordletogether.profile.domain

class IncWinLoseDrawUseCase(private val playerProfileRepository: PlayerProfileRepository) {
    suspend fun win() {
        playerProfileRepository.incWinCount()
    }

    suspend fun lose() {
        playerProfileRepository.incLoseCount()
    }

    suspend fun draw() {
        playerProfileRepository.incDrawCount()
    }
}
