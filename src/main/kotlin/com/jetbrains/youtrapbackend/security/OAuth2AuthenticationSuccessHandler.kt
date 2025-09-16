package com.jetbrains.youtrapbackend.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${app.oauth2.redirect-uri}") private val redirectUri: String,
    private val objectMapper: ObjectMapper
) : AuthenticationSuccessHandler {

    data class LoginResponse(
        val token: String,
        val email: String,
        val name: String,
        val picture: String,
        val redirectUrl: String
    )

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val token = jwtTokenProvider.generateToken(authentication)
        val userPrincipal = authentication.principal as OAuth2User
        val email = userPrincipal.attributes["email"] as? String ?: "N/A"
        val name = userPrincipal.attributes["name"] as? String ?: "N/A"
        val picture = userPrincipal.attributes["picture"] as? String ?: ""

        val targetUrl = "$redirectUri?token=$token"

        val loginResponse = LoginResponse(token, email, name, picture, targetUrl)

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(loginResponse))
    }
}