package com.jetbrains.youtrapbackend.youtrack

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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

    data class IssueSummary(
        val idReadable: String?,
        val summary: String?,
        val project: Project?,
        val created: Long?,
        val updated: Long?,
        val url: String?,
    ) {
        data class Project(val name: String?)
    }

    fun getAllIssues(): List<IssueSummary> {
        require(baseUrl.isNotBlank() && apiToken.isNotBlank()) {
            "YouTrack base URL and API token must be configured in your environment variables."
        }

        val fields = "idReadable,summary,project(name),created,updated"

        val query = "project: ADM"

        val uri = UriComponentsBuilder
            .fromHttpUrl(baseUrl.removeSuffix("/") + "/api/issues")
            .queryParam("query", query)
            .queryParam("fields", fields)
            .build()
            .toUri()

        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            set(HttpHeaders.AUTHORIZATION, "Bearer $apiToken")
        }

        val requestEntity = HttpEntity<Void>(headers)

        return try {
            val response: ResponseEntity<Array<IssueSummary>> = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                Array<IssueSummary>::class.java
            )
            val list = response.body?.toList().orEmpty()
            list.map { issue ->
                issue.copy(url = if (!issue.idReadable.isNullOrBlank()) baseUrl.removeSuffix("/") + "/issue/" + issue.idReadable else null)
            }
        } catch (ex: HttpClientErrorException.Unauthorized) {
            throw UnauthorizedToYouTrackException("Unauthorized to YouTrack. Check your API token.")
        } catch (ex: HttpClientErrorException.Forbidden) {
            throw UnauthorizedToYouTrackException("Forbidden by YouTrack. Your token may lack the required permissions.")
        }
    }
}

class UnauthorizedToYouTrackException(message: String): RuntimeException(message)