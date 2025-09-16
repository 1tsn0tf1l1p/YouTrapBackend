package com.jetbrains.youtrapbackend.security

data class UserPrincipal(
    val email: String,
    val name: String,
    val picture: String
)