package com.example.scrambletogether.presentation.viewModel

sealed class LetterEvent {
    class AddLetter(val letter: Char, val isHost: Boolean? = null): LetterEvent()
    class DeleteLetter(val isHost: Boolean? = null): LetterEvent()
    class CheckAnswer(val isHost: Boolean? = null): LetterEvent()
    class Restart(val id: String? = null): LetterEvent()
    object CloseLine : LetterEvent()
    object IsDoneSwitch: LetterEvent()
}
