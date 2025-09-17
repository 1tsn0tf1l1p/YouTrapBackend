package com.jetbrains.youtrapbackend.services

import com.jetbrains.youtrapbackend.api.dto.IssueDetailsResponse
import com.jetbrains.youtrapbackend.youtrack.YouTrackClient
import org.springframework.stereotype.Service
import java.util.*

@Service
class YouTrackService(
    private val youTrackClient: YouTrackClient
) {
    fun getIssueDependencyGraph(startIssueId: String): List<IssueDetailsResponse> {
        val issuesToVisit: Queue<String> = LinkedList()
        val visitedIssueIds = mutableSetOf<String>()
        val allIssuesFound = mutableMapOf<String, YouTrackClient.IssueSummary>()

        issuesToVisit.add(startIssueId)
        visitedIssueIds.add(startIssueId)

        while (issuesToVisit.isNotEmpty()) {
            val currentIssueId = issuesToVisit.poll()
            val issueDetails = youTrackClient.getIssueDetails(currentIssueId) ?: continue

            allIssuesFound[currentIssueId] = issueDetails

            issueDetails.links?.forEach { link ->
                if (link.linkType.name == "Depend") {
                    link.issues.forEach { linkedIssue ->
                        if (!visitedIssueIds.contains(linkedIssue.idReadable)) {
                            visitedIssueIds.add(linkedIssue.idReadable)
                            issuesToVisit.add(linkedIssue.idReadable)
                        }
                    }
                }
            }
        }
        return allIssuesFound.values.map { it.toCleanResponse() }
    }

    fun getIssuesForProject(projectName: String): List<IssueDetailsResponse> {
        val rawIssues = youTrackClient.getIssuesForProject(projectName)
        return rawIssues.map { it.toCleanResponse() }
    }

    fun getAllProjects(): List<YouTrackClient.YouTrackProject> {
        return youTrackClient.getProjects()
    }

    private fun YouTrackClient.IssueSummary.toCleanResponse(): IssueDetailsResponse {
        val stateField = this.customFields?.find { it.name == "State" }

        val stateName = when (val value = stateField?.value) {
            is Map<*, *> -> value["name"] as? String
            is List<*> -> (value.firstOrNull() as? Map<*, *>)?.get("name") as? String
            else -> null
        }

        return IssueDetailsResponse(
            idReadable = this.idReadable,
            summary = this.summary,
            project = this.project,
            created = this.created,
            updated = this.updated,
            url = this.url,
            links = this.links,
            state = stateName
        )
    }
}