package dev.arkhamd.wordletogether.session.domain

import dev.arkhamd.wordletogether.wordle.domain.Letter

fun List<List<Letter>>.toSessionGrid(): List<List<SessionLetter>> = map { line ->
    line.map { letter ->
        SessionLetter(
            letter = letter.letter,
            feedback = letter.feedback
        )
    }
}

fun List<List<SessionLetter>>.toWordleGrid(): List<List<Letter>> = map { line ->
    line.map { sessionLetter ->
        Letter(
            letter = sessionLetter.letter,
            feedback = sessionLetter.feedback
        )
    }
}
