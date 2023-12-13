package com.example.scrambletogether.data

lateinit var words: List<String>

val startWordleWords: LettersViewModelDataClass =
    LettersViewModelDataClass(
        tryingWords = Array(6) {
            Array(5) { LetterDataClass() }
        }
    )

val startKeyboard: Array<Array<LetterDataClass>> =
    arrayOf(
        Array("ЙЦУКЕНГШЩЗХЪ".length) {
            val rus = "ЙЦУКЕНГШЩЗХЪ"
            LetterDataClass(letter = rus[it])
        },
        Array("ФЫВАПРОЛДЖЭ".length) {
            val rus = "ФЫВАПРОЛДЖЭ"
            LetterDataClass(letter = rus[it])
        },
        Array("ЯЧСМИТЬБЮ".length) {
            val rus = "ЯЧСМИТЬБЮ"
            LetterDataClass(letter = rus[it])
        }
    )