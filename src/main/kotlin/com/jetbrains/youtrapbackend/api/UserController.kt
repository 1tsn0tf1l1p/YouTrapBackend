package com.jetbrains.youtrapbackend.api

import com.jetbrains.youtrapbackend.security.UserPrincipal // Import the new class
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController {
    data class UserInfoResponse(
        val email: String,
        val name: String,
        val picture: String,
        val authorities: List<String>,
    )

    @GetMapping("/me")
    fun me(authentication: Authentication): UserInfoResponse {
        val userPrincipal = authentication.principal as UserPrincipal
        val authorities = authentication.authorities.map { it.authority }

        return UserInfoResponse(
            email = userPrincipal.email,
            name = userPrincipal.name,
            picture = userPrincipal.picture,
            authorities = authorities
        )
    }
}