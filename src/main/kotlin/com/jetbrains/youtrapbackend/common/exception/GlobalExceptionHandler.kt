package com.jetbrains.youtrapbackend.common.exception

import com.jetbrains.youtrapbackend.api.dto.ErrorResponse
import com.jetbrains.youtrapbackend.youtrack.UnauthorizedToYouTrackException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(ex.message ?: "Invalid request"))

    @ExceptionHandler(UnauthorizedToYouTrackException::class)
    fun handleUnauthorizedToYouTrack(ex: UnauthorizedToYouTrackException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(ex.message ?: "Unauthorized"))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("Internal server error"))
}
