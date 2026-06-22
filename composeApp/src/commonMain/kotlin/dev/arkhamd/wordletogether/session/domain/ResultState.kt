package dev.arkhamd.wordletogether.session.domain

sealed class ResultState<out T> {
    data class Success<out R>(val data: R) : ResultState<R>()
    data class Failure(val error: SessionGatewayError) : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
}

sealed class SessionGatewayError(val message: String) {
    data object InvalidSessionId : SessionGatewayError("Session id is blank")

    data class AlreadyExists(val sessionId: String) : SessionGatewayError("Session already exists: $sessionId")

    data class NotFound(val sessionId: String) : SessionGatewayError("Session not found: $sessionId")

    data class Full(val sessionId: String) : SessionGatewayError("Session is already full: $sessionId")

    data class Unexpected(val details: String) : SessionGatewayError(details)
}
