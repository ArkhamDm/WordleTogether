package dev.arkhamd.wordletogether.wordle.data

import android.content.Context
import dev.arkhamd.wordletogether.wordle.domain.WordDictionary

class AndroidAssetWordDictionary(context: Context) : WordDictionary {
    private val delegate = context.assets.open("rus_5letters.json")
        .bufferedReader()
        .useLines { lines ->
            buildBundledWordDictionary(lines)
        }

    override fun isValid(word: String): Boolean = delegate.isValid(word)

    override fun randomWordOrNull(): String? = delegate.randomWordOrNull()
}
