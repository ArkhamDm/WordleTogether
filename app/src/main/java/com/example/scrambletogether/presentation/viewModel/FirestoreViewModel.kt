package com.example.scrambletogether.presentation.viewModel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.scrambletogether.data.model.StartValues.enemyGrid
import com.example.scrambletogether.data.model.UserRepo
import com.example.scrambletogether.data.model.firestore.SessionExtra
import com.example.scrambletogether.data.repository.FirestoreRepositoryImpl
import com.example.scrambletogether.data.repository.UserRepositoryImpl
import com.example.scrambletogether.domain.model.ColorLetter
import com.example.scrambletogether.domain.model.Letter
import com.example.scrambletogether.domain.model.ResultState
import com.example.scrambletogether.domain.model.SessionItem
import com.example.scrambletogether.domain.repository.FirestoreRepository
import com.example.scrambletogether.domain.useCase.IncWinLoseDrawUseCase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.getField
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val HOST = "host "
private const val GUEST = "guest "
private const val GRID = "grid"
private const val CURRENT_WORD = "currentWord"
private const val IS_WAIT = "isWait"

class FirestoreViewModel(
    private val userRepository: UserRepositoryImpl
): ViewModel() {

    private val _session = MutableStateFlow(SessionExtra())
    var session: StateFlow<SessionExtra> = _session.asStateFlow()

    private val _sessionsList = MutableStateFlow(SessionsList())
    var sessionsList: StateFlow<SessionsList> = _sessionsList.asStateFlow()

    private val _dataCounts = MutableStateFlow(UserRepo())
    var dataCounts: StateFlow<UserRepo> = _dataCounts.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.winLoseDrawCount.collect {
                _dataCounts.value = UserRepo(it.winCount, it.loseCount)
            }
        }
    }

    fun send(event: FirestoreEvent) {
        when (event) {
            is FirestoreEvent.ConnectToSession -> {
                connectToSession(event.id)
            }
            is FirestoreEvent.CreateSession -> {
                create(event.sessionItem)
            }
            is FirestoreEvent.DisconnectFromSession -> {
                disconnectFromSession()
            }
            is FirestoreEvent.GetSessions -> {
                getSessions()
            }
            is FirestoreEvent.IncDrawCount -> {
                incDrawCount()
            }
            is FirestoreEvent.IncLoseCount -> {
                incLoseCount()
            }
            is FirestoreEvent.IncWinCount -> {
                incWinCount()
            }
            is FirestoreEvent.Reset -> {
                reset()
            }
            is FirestoreEvent.UpdateWord -> {
                updateWord(event.word)
            }
            is FirestoreEvent.IsDoneSwitch -> {
                isDoneSwitch()
            }
        }
    }

    private fun incWinCount() = viewModelScope.launch {
        IncWinLoseDrawUseCase(userRepository).win()
    }
    private fun incLoseCount() = viewModelScope.launch {
        IncWinLoseDrawUseCase(userRepository).lose()
    }

    private fun incDrawCount() = viewModelScope.launch {
        IncWinLoseDrawUseCase(userRepository).draw()
    }

    private val repo: FirestoreRepository = FirestoreRepositoryImpl()
    lateinit var listener: ListenerRegistration

    var infForViewmodel = ViewModelInfo()

    private fun reset() = viewModelScope.launch {
        repo.reset(session.value.sessionId,session.value.isHost).collect {
            when(it) {
                is ResultState.Success -> {
                    Log.d(TAG, it.data)
                }
                is ResultState.Failure -> {
                    Log.e(TAG, "fail reset ${session.value.sessionId}", it.msg)
                }
                ResultState.Loading -> {}
            }
        }
    }

    init {
        Log.d(TAG, "init!")
    }

    private fun updateWord(word: String) = viewModelScope.launch {
        repo.updateWord(session.value.sessionId, word, session.value.isHost).collect {
            when(it) {
                is ResultState.Success -> {
                    Log.d(TAG, it.data)
                }
                is ResultState.Failure -> {
                    Log.e(TAG, "failed updateWord in ${session.value.sessionId}", it.msg)
                }
                ResultState.Loading -> {}
            }
        }
    }

    private fun create(sessionItem: SessionItem) {
        viewModelScope.launch {
            repo.addSession(sessionItem).collect {
                when (it) {
                    is ResultState.Success -> {
                        infForViewmodel.sessionId = it.data
                        infForViewmodel.isHost = true
                        _session.value = SessionExtra(
                            sessionId = sessionItem.id,
                            isHost = true
                        )
                        listenSession(true)
                        Log.d(TAG, it.data)
                    }

                    is ResultState.Failure -> {
                        Log.e(TAG, "failed create ${sessionItem.id}", it.msg)
                    }

                    ResultState.Loading -> {}
                }
            }
        }
    }

    private fun getSessions() = viewModelScope.launch {
        repo.getAllSessions().collect {
            when(it) {
                is ResultState.Success -> {
                    _sessionsList.value = SessionsList(
                        sessions = it.data,
                        isLoading = false
                    )
                }
                is ResultState.Failure -> {
                    Log.e(TAG, "fail to getSessions", it.msg)
                }
                ResultState.Loading -> {
                    _sessionsList.value = SessionsList(
                        isLoading = true
                    )
                }
            }
        }
    }

    private fun connectToSession(id: String) = viewModelScope.launch {
        repo.connect(id).collect {
            when(it) {
                is ResultState.Success -> {
                    _session.value = _session.value.copy(sessionId = id, isHost = false)
                    infForViewmodel.sessionId = id
                    infForViewmodel.isHost = false
                    Log.d(TAG, it.data)
                    listenSession(true)
                }
                is ResultState.Failure -> {
                    Log.e(TAG, "fail connect", it.msg)
                }
                ResultState.Loading -> {}
            }
        }
    }

    private fun disconnectFromSession() = viewModelScope.launch {

        if (session.value.sessionId.isEmpty()) return@launch

        repo.disconnect(session.value.sessionId, session.value.isHost).collect {
            when(it) {
                is ResultState.Success -> {
                    Log.d(TAG, it.data)
                    listenSession(false)
                }
                is ResultState.Failure -> {
                    Log.e(TAG, "fail disconect host:${session.value.isHost}", it.msg)
                }
                ResultState.Loading -> {}
            }
        }
    }

    private fun listenSession(isListen: Boolean) {
        if (isListen) {
            Log.d(TAG, "start listen")
            listener = Firebase.firestore.collection("sessions")
                .document(session.value.sessionId)
                .addSnapshotListener { document, e ->
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }


                    if (document != null) {
                        val field = if (session.value.isHost) GUEST else HOST
                        val wordleGrid = document.getField<String>(field + GRID)
                            ?.split("|")!!.dropLast(1)
                        val gridNew = convertFromStringsToArray(wordleGrid)

                        val selfWord = document.getString(
                            (if (field == HOST) GUEST else HOST)  + CURRENT_WORD
                        )!!
                        val enemyWord = document.getString(field + CURRENT_WORD)!!
                        val isWait = document.getBoolean(IS_WAIT)!!

                        val gridSize = enemyGrid.size
                        val wordSize = enemyGrid[0].size
                        val isWin =
                            (gridNew.count { line ->
                                line.count { letter ->
                                    letter.color == ColorLetter.Right.color } == wordSize } >= 1)
                        val isLose =
                            (gridNew[gridSize - 1].count {
                                (it.letter != ' ') && (it.color != ColorLetter.None.color)} == wordSize)

                        _session.update {
                            it.copy(
                                isWin = isWin,
                                isLose = isLose,
                                selfWord = selfWord,
                                enemyWord = enemyWord,
                                listenGrid = gridNew,
                                isWait = isWait
                            )
                        }

                    }
                }
        } else {
            Log.d(TAG, "STOP LISTEN")
            listener.remove()
        }
    }

    private fun isDoneSwitch() {
        _session.update {
            it.copy(
                isWin = false
            )
        }
    }

    private fun convertFromStringsToArray(wordleGrid: List<String>): Array<Array<Letter>> {
        // from
        // 6x5 wordleGrid (L - Letter, C - color)
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // LCLCLCLCLC|
        // to
        // [LC, LC, LC, LC, LC] x6
        val gridNew = enemyGrid.map { it.copyOf() }.toTypedArray()
        for (line in wordleGrid.indices) {
            for (letterData in 0 until wordleGrid[0].length / 2) {
                val color = when (wordleGrid[line][letterData * 2 + 1]) {
                    ColorLetter.Right.name[0] -> ColorLetter.Right.color
                    ColorLetter.Almost.name[0] -> ColorLetter.Almost.color
                    ColorLetter.Miss.name[0] -> ColorLetter.Miss.color
                    else -> ColorLetter.None.color
                }

                val letter = wordleGrid[line][letterData * 2]
                gridNew[line][letterData] = Letter(letter, color)

            }
        }

        return gridNew
    }

    companion object {
        class SettingsViewModelFactory(private val prefs: UserRepositoryImpl) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FirestoreViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FirestoreViewModel(prefs) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}

data class SessionsList(
    val sessions: List<SessionItem> = emptyList(),
    val isLoading: Boolean = true
)

data class ViewModelInfo(
    var sessionId : String = "",
    var isHost: Boolean = true
)
