package com.jetbrains.youtrapbackend.api

import com.jetbrains.youtrapbackend.youtrack.UnauthorizedToYouTrackException
import com.jetbrains.youtrapbackend.youtrack.YouTrackClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/youtrack")
class YouTrackController(
    private val youTrackClient: YouTrackClient
) {
    data class ErrorResponse(val message: String)

    @GetMapping("/issues")
    fun getAllIssues(): ResponseEntity<*> {
        return try {
            val issues = youTrackClient.getAllIssues()
            ResponseEntity.ok(issues)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(e.message ?: "Invalid request"))
        } catch (e: UnauthorizedToYouTrackException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(e.message ?: "Unauthorized"))
        }
    }
}