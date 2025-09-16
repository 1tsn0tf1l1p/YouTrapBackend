package com.jetbrains.youtrapbackend.youtrack

import org.springframework.stereotype.Service
import java.util.LinkedList
import java.util.Queue

@Service
class YouTrackService(
    private val youTrackClient: YouTrackClient
) {
    /**
     * Fetches an issue and recursively traverses its "Depends on" and "Is required for" links
     * to build a complete graph of all related issues.
     *
     * @param startIssueId The ID of the issue to start the traversal from (e.g., "BE-4").
     * @return A list of all unique issues found in the dependency graph.
     */
    fun getIssueDependencyGraph(startIssueId: String): List<YouTrackClient.IssueSummary> {
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
        return allIssuesFound.values.toList()
    }
}