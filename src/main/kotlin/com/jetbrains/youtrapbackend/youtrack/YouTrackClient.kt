package com.jetbrains.youtrapbackend.youtrack

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class YouTrackClient(
    @Value("\${youtrack.base-url}") private val baseUrl: String,
    @Value("\${youtrack.api.token}") private val apiToken: String,
) {
    private val restTemplate: RestTemplate = RestTemplate()

    data class LinkedIssue(val idReadable: String)

    data class LinkType(val name: String)

    data class IssueLink(
        val direction: String,
        val linkType: LinkType,
        val issues: List<LinkedIssue>
    )

    data class IssueSummary(
        val idReadable: String?,
        val summary: String?,
        val project: Project?,
        val created: Long?,
        val updated: Long?,
        val url: String?,
        val links: List<IssueLink>?
    ) {
        data class Project(val name: String?)
    }

    fun getIssueDetails(issueId: String): IssueSummary? {
        require(baseUrl.isNotBlank() && apiToken.isNotBlank()) {
            "YouTrack base URL and API token must be configured."
        }

        val fields = "idReadable,summary,project(name),created,updated," +
                "links(direction,linkType(name),issues(idReadable))"

        val uri = UriComponentsBuilder
            .fromHttpUrl("$baseUrl/api/issues/$issueId")
            .queryParam("fields", fields)
            .build()
            .toUri()

        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            set(HttpHeaders.AUTHORIZATION, "Bearer $apiToken")
        }

        val requestEntity = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, IssueSummary::class.java)
            response.body?.copy(url = "$baseUrl/issue/$issueId")
        } catch (ex: HttpClientErrorException.NotFound) {
            null
        } catch (ex: HttpClientErrorException.Unauthorized) {
            throw UnauthorizedToYouTrackException("Unauthorized to YouTrack. Check your API token.")
        } catch (ex: HttpClientErrorException.Forbidden) {
            throw UnauthorizedToYouTrackException("Forbidden by YouTrack. Token may lack permissions.")
        }
    }
}

class UnauthorizedToYouTrackException(message: String) : RuntimeException(message)