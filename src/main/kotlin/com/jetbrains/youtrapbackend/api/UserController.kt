package com.jetbrains.youtrapbackend.api

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController {

    data class UserInfoResponse(
        val email: String,
        val authorities: List<String>,
    )

    @GetMapping("/me")
    fun me(authentication: Authentication): UserInfoResponse {
        val email = authentication.name
        val authorities = authentication.authorities.map(GrantedAuthority::getAuthority)
        return UserInfoResponse(email, authorities)
    }
}
