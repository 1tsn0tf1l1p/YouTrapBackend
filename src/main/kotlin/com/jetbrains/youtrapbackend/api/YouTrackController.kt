package com.jetbrains.youtrapbackend.youtrack

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/youtrack")
class YouTrackController(
    private val youTrackService: YouTrackService
) {
    data class ErrorResponse(val message: String)

    @GetMapping("/issues/{issueId}/graph")
    fun getIssueGraph(
        @PathVariable issueId: String
    ): ResponseEntity<*> {
        return try {
            val issueGraph = youTrackService.getIssueDependencyGraph(issueId)
            ResponseEntity.ok(issueGraph)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(e.message ?: "Invalid request"))
        } catch (e: UnauthorizedToYouTrackException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(e.message ?: "Unauthorized"))
        }
    }

    @GetMapping("/projects/{projectName}/issues")
    fun getProjectIssues(
        @PathVariable projectName: String
    ): ResponseEntity<*> {
        return try {
            val issues = youTrackService.getIssuesForProject(projectName)
            ResponseEntity.ok(issues)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(e.message ?: "Invalid request"))
        } catch (e: UnauthorizedToYouTrackException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(e.message ?: "Unauthorized"))
        }
    }
}