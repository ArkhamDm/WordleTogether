package dev.arkhamd.wordletogether.wordle.domain

class ScoreGuessUseCase {

    operator fun invoke(guess: String, answer: String): List<LetterFeedback> {
        require(guess.length == answer.length) {
            "Guess and answer must have the same length"
        }

        val feedback = MutableList(guess.length) { LetterFeedback.Miss }
        val remainingAnswerLetters = mutableMapOf<Char, Int>()

        for (index in guess.indices) {
            if (guess[index] == answer[index]) {
                feedback[index] = LetterFeedback.Right
            } else {
                remainingAnswerLetters[answer[index]] =
                    (remainingAnswerLetters[answer[index]] ?: 0) + 1
            }
        }

        for (index in guess.indices) {
            if (feedback[index] == LetterFeedback.Right) continue

            val guessLetter = guess[index]
            val remainingCount = remainingAnswerLetters[guessLetter] ?: 0
            if (remainingCount > 0) {
                feedback[index] = LetterFeedback.Almost
                remainingAnswerLetters[guessLetter] = remainingCount - 1
            }
        }

        return feedback
    }
}
