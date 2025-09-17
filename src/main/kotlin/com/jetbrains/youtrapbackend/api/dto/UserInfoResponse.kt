package com.jetbrains.youtrapbackend.api.dto

data class UserInfoResponse(
    val email: String,
    val name: String,
    val picture: String,
    val authorities: List<String>,
)
