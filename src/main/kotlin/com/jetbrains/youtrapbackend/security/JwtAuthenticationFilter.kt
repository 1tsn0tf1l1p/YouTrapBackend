package com.jetbrains.youtrapbackend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwt = getJwtFromRequest(request)

        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            val claims = jwtTokenProvider.getAllClaimsFromToken(jwt)
            val email = claims.subject
            val name = claims.get("name", String::class.java)
            val picture = claims.get("picture", String::class.java)

            val userPrincipal = UserPrincipal(email, name, picture)
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

            val authentication = UsernamePasswordAuthenticationToken(userPrincipal, null, authorities)

            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization") ?: return null
        return if (bearerToken.startsWith("Bearer ", ignoreCase = true)) {
            bearerToken.substring(7)
        } else null
    }
}