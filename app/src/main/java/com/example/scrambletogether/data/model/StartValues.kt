package com.example.scrambletogether.data.model

import com.example.scrambletogether.domain.model.Letter

object StartValues {
    var words: List<String> = emptyList()

    val startWordleWords: LettersViewModelDataClass =
        LettersViewModelDataClass(
            tryingWords = Array(6) {
                Array(5) { Letter() }
            }
        )

    val enemyGrid = Array(6) {
        Array(5) { Letter() }
    }

    val startKeyboard: Array<Array<Letter>> =
        arrayOf(
            Array("ЙЦУКЕНГШЩЗХЪ".length) {
                val rus = "ЙЦУКЕНГШЩЗХЪ"
                Letter(letter = rus[it])
            },
            Array("ФЫВАПРОЛДЖЭ".length) {
                val rus = "ФЫВАПРОЛДЖЭ"
                Letter(letter = rus[it])
            },
            Array("ЯЧСМИТЬБЮ".length) {
                val rus = "ЯЧСМИТЬБЮ"
                Letter(letter = rus[it])
            }
        )
}