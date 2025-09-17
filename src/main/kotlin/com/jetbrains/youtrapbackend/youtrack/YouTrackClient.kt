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

    data class CustomField(val name: String?, val value: Any?)

    data class IssueSummary(
        val idReadable: String?,
        val summary: String?,
        val project: Project?,
        val created: Long?,
        val updated: Long?,
        val url: String?,
        val links: List<IssueLink>?,
        val customFields: List<CustomField>?
    ) {
        data class Project(val name: String?)
    }

    data class YouTrackProject(
        val id: String?,
        val name: String?,
        val shortName: String?
    )

    fun getIssueDetails(issueId: String): IssueSummary? {
        require(baseUrl.isNotBlank() && apiToken.isNotBlank()) {
            "YouTrack base URL and API token must be configured."
        }

        val fields = "idReadable,summary,project(name),created,updated," +
                "links(direction,linkType(name),issues(idReadable))," +
                "customFields(name,value(name))"

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

    fun getIssuesForProject(projectName: String): List<IssueSummary> {
        require(baseUrl.isNotBlank() && apiToken.isNotBlank()) {
            "YouTrack base URL and API token must be configured."
        }

        val fields = "idReadable,summary,project(name),created,updated,customFields(name,value(name))"
        val query = "project: {$projectName}"

        val uri = UriComponentsBuilder
            .fromHttpUrl("$baseUrl/api/issues")
            .queryParam("fields", fields)
            .queryParam("query", query)
            .queryParam("\$top", 100)
            .build()
            .toUri()

        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            set(HttpHeaders.AUTHORIZATION, "Bearer $apiToken")
        }

        val requestEntity = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Array<IssueSummary>::class.java)
            response.body?.map { it.copy(url = "$baseUrl/issue/${it.idReadable}") } ?: emptyList()
        } catch (ex: HttpClientErrorException.NotFound) {
            emptyList()
        } catch (ex: HttpClientErrorException.Unauthorized) {
            throw UnauthorizedToYouTrackException("Unauthorized to YouTrack. Check your API token.")
        } catch (ex: HttpClientErrorException.Forbidden) {
            throw UnauthorizedToYouTrackException("Forbidden by YouTrack. Token may lack permissions.")
        }
    }

    fun getProjects(): List<YouTrackProject> {
        require(baseUrl.isNotBlank() && apiToken.isNotBlank()) {
            "YouTrack base URL and API token must be configured."
        }

        val fields = "id,name,shortName"

        val uri = UriComponentsBuilder
            .fromHttpUrl("$baseUrl/api/admin/projects")
            .queryParam("fields", fields)
            .queryParam("\$top", -1)
            .build()
            .toUri()

        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            set(HttpHeaders.AUTHORIZATION, "Bearer $apiToken")
        }

        val requestEntity = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Array<YouTrackProject>::class.java)
            response.body?.toList() ?: emptyList()
        } catch (ex: HttpClientErrorException.Unauthorized) {
            throw UnauthorizedToYouTrackException("Unauthorized to YouTrack. Check your API token.")
        } catch (ex: HttpClientErrorException.Forbidden) {
            throw UnauthorizedToYouTrackException("Forbidden by YouTrack. Token may lack permissions to read projects.")
        }
    }
}

class UnauthorizedToYouTrackException(message: String) : RuntimeException(message)