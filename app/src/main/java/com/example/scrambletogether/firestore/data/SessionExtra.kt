package com.example.scrambletogether.firestore.data

import com.example.scrambletogether.data.Letter
import com.example.scrambletogether.data.enemyGrid

data class SessionExtra(
    var listenGrid: Array<Array<Letter>> = enemyGrid,
    var enemyWord: String = "",
    var selfWord: String = "",
    var isDone: Boolean = false,
    var sessionId: String = "",
    var isWait: Boolean = true,
    var isHost: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionExtra

        if (!listenGrid.contentDeepEquals(other.listenGrid)) return false
        if (enemyWord != other.enemyWord) return false
        if (selfWord != other.selfWord) return false
        if (sessionId != other.sessionId) return false
        if (isWait != other.isWait) return false
        if (isHost != other.isHost) return false

        return true
    }

    override fun hashCode(): Int {
        var result = listenGrid.contentDeepHashCode()
        result = 31 * result + enemyWord.hashCode()
        result = 31 * result + selfWord.hashCode()
        result = 31 * result + isDone.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + isWait.hashCode()
        result = 31 * result + isHost.hashCode()
        return result
    }
}