package com.jetbrains.youtrapbackend.services

import com.jetbrains.youtrapbackend.api.dto.EnrichedIssueLinkResponse
import com.jetbrains.youtrapbackend.api.dto.IssueDetailsResponse
import com.jetbrains.youtrapbackend.youtrack.YouTrackClient
import org.springframework.stereotype.Service
import java.util.*

@Service
class YouTrackService(
    private val youTrackClient: YouTrackClient
) {
    fun getIssueDependencyGraph(startIssueId: String): List<IssueDetailsResponse> {
        val allIssuesInGraph = fetchFullGraph(startIssueId)

        val responseCache = mutableMapOf<String, IssueDetailsResponse>()

        return allIssuesInGraph.values.map { issueSummary ->
            buildRecursiveResponse(issueSummary, allIssuesInGraph, responseCache)
        }
    }

    private fun fetchFullGraph(startIssueId: String): Map<String, YouTrackClient.IssueSummary> {
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
        return allIssuesFound
    }

    private fun buildRecursiveResponse(
        currentIssue: YouTrackClient.IssueSummary,
        allIssuesInGraph: Map<String, YouTrackClient.IssueSummary>,
        cache: MutableMap<String, IssueDetailsResponse>
    ): IssueDetailsResponse {
        val currentId = currentIssue.idReadable!!
        if (cache.containsKey(currentId)) return cache.getValue(currentId)

        val placeholder = currentIssue.toStubResponse()
        cache[currentId] = placeholder

        val enrichedLinks = currentIssue.links?.map { link ->
            val linkedIssuesDetails = link.issues.mapNotNull { stub ->
                allIssuesInGraph[stub.idReadable]?.let { fullLinkedIssue ->
                    buildRecursiveResponse(fullLinkedIssue, allIssuesInGraph, cache)
                }
            }
            EnrichedIssueLinkResponse(link.direction, link.linkType, linkedIssuesDetails)
        }

        val finalResponse = currentIssue.toFullResponse(enrichedLinks)
        cache[currentId] = finalResponse
        return finalResponse
    }

    fun getIssuesForProject(projectName: String): List<IssueDetailsResponse> {
        val rawIssues = youTrackClient.getIssuesForProject(projectName)
        return rawIssues.map { it.toFullResponse(null) }
    }

    fun getAllProjects(): List<YouTrackClient.YouTrackProject> {
        return youTrackClient.getProjects()
    }

    private fun YouTrackClient.IssueSummary.toFullResponse(enrichedLinks: List<EnrichedIssueLinkResponse>?): IssueDetailsResponse {
        val stateName = this.customFields?.find { it.name == "State" }?.let { extractStateName(it.value) }

        val finalLinks = enrichedLinks ?: this.links?.map { link ->
            val stubIssues = link.issues.map { stub ->
                YouTrackClient.IssueSummary(stub.idReadable, null, null, null, null, null, null, null).toStubResponse()
            }
            EnrichedIssueLinkResponse(link.direction, link.linkType, stubIssues)
        }

        return IssueDetailsResponse(
            idReadable = this.idReadable,
            summary = this.summary,
            project = this.project,
            created = this.created,
            updated = this.updated,
            url = this.url,
            links = finalLinks,
            state = stateName
        )
    }

    private fun YouTrackClient.IssueSummary.toStubResponse(): IssueDetailsResponse {
        val stateName = this.customFields?.find { it.name == "State" }?.let { extractStateName(it.value) }
        return IssueDetailsResponse(this.idReadable, this.summary, this.project, this.created, this.updated, this.url, null, stateName)
    }

    private fun extractStateName(value: Any?): String? {
        return when (value) {
            is Map<*, *> -> value["name"] as? String
            is List<*> -> (value.firstOrNull() as? Map<*, *>)?.get("name") as? String
            else -> null
        }
    }
}