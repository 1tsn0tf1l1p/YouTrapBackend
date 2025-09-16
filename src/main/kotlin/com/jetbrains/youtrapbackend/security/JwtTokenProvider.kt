package com.jetbrains.youtrapbackend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expiration-ms}") private val jwtExpirationMs: Long,
) {

    private fun signingKey() = Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(authentication: Authentication): String {
        val email = extractEmail(authentication)
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getEmailFromToken(token: String): String =
        Jwts.parserBuilder()
            .setSigningKey(signingKey())
            .build()
            .parseClaimsJws(token)
            .body
            .subject

    fun validateToken(token: String): Boolean = try {
        val claimsJws = Jwts.parserBuilder()
            .setSigningKey(signingKey())
            .build()
            .parseClaimsJws(token)
        claimsJws.body.expiration.after(Date())
    } catch (ex: Exception) {
        false
    }

    private fun extractEmail(authentication: Authentication): String {
        if (authentication is OAuth2AuthenticationToken) {
            val principal = authentication.principal
            if (principal is OidcUser) {
                principal.email?.let { return it }
                principal.userInfo?.email?.let { return it }
                (principal.attributes["email"] as? String)?.let { return it }
                principal.name?.let { return it }
            } else {
                val attrs = principal.attributes
                (attrs["email"] as? String)?.let { return it }
                (attrs["preferred_username"] as? String)?.let { return it }
                (attrs["sub"] as? String)?.let { return it }
                principal.name?.let { return it }
            }
        }
        return authentication.name
    }
}
