package com.jetbrains.youtrapbackend.api.dto

import com.jetbrains.youtrapbackend.youtrack.YouTrackClient

data class IssueDetailsResponse(
    val idReadable: String?,
    val summary: String?,
    val project: YouTrackClient.IssueSummary.Project?,
    val created: Long?,
    val updated: Long?,
    val url: String?,
    val links: List<YouTrackClient.IssueLink>?,
    val state: String?
)