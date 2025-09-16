package com.jetbrains.youtrapbackend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
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
        val userPrincipal = authentication.principal as OAuth2User
        val email = userPrincipal.attributes["email"] as? String ?: "N/A"
        val name = userPrincipal.attributes["name"] as? String ?: "N/A"
        val picture = userPrincipal.attributes["picture"] as? String ?: ""

        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(email)
            .claim("name", name)
            .claim("picture", picture)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun getAllClaimsFromToken(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(signingKey())
            .build()
            .parseClaimsJws(token)
            .body

    fun validateToken(token: String): Boolean = try {
        val claims = getAllClaimsFromToken(token)
        claims.expiration.after(Date())
    } catch (ex: Exception) {
        false
    }
}