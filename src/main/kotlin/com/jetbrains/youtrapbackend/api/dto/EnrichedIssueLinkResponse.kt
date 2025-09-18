package com.jetbrains.youtrapbackend.api.dto

import com.jetbrains.youtrapbackend.youtrack.YouTrackClient

data class EnrichedIssueLinkResponse(
    val direction: String,
    val linkType: YouTrackClient.LinkType,
    val issues: List<IssueDetailsResponse>
)