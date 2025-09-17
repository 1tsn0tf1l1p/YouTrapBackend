package com.jetbrains.youtrapbackend.api

import com.jetbrains.youtrapbackend.services.YouTrackService
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

    @GetMapping("/issues/{issueId}/graph")
    fun getIssueGraph(
        @PathVariable issueId: String
    ): ResponseEntity<*> {
        val issueGraph = youTrackService.getIssueDependencyGraph(issueId)
        return ResponseEntity.ok(issueGraph)
    }

    @GetMapping("/projects/{projectName}/issues")
    fun getProjectIssues(
        @PathVariable projectName: String
    ): ResponseEntity<*> {
        val issues = youTrackService.getIssuesForProject(projectName)
        return ResponseEntity.ok(issues)
    }

    @GetMapping("/projects")
    fun getProjects(): ResponseEntity<*> {
        val projects = youTrackService.getAllProjects()
        return ResponseEntity.ok(projects)
    }
}