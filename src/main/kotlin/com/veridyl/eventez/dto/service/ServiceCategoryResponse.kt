package com.veridyl.eventez.dto.service

data class ServiceCategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val icon: String?,
    val description: String?
)