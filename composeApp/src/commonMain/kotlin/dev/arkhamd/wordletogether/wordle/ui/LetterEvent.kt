package dev.arkhamd.wordletogether.wordle.ui

import dev.arkhamd.wordletogether.session.domain.SessionHandle

sealed class LetterEvent {
    class AddLetter(val letter: Char, val isHost: Boolean? = null) : LetterEvent()
    class DeleteLetter(val isHost: Boolean? = null) : LetterEvent()
    class CheckAnswer(val isHost: Boolean? = null) : LetterEvent()
    class Restart(val sessionHandle: SessionHandle? = null) : LetterEvent()
    class SetSessionHandle(val handle: SessionHandle) : LetterEvent()
    class SetCurrentWord(val word: String) : LetterEvent()
    object CloseLine : LetterEvent()
    object IsDoneSwitch : LetterEvent()
}
