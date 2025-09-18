package com.jetbrains.youtrapbackend.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.jetbrains.youtrapbackend.youtrack.YouTrackClient


@JsonInclude(JsonInclude.Include.NON_NULL)
data class IssueDetailsResponse(
    val idReadable: String?,
    val summary: String?,
    val project: YouTrackClient.IssueSummary.Project?,
    val created: Long?,
    val updated: Long?,
    val url: String?,
    val links: List<EnrichedIssueLinkResponse>?,
    val state: String?
)