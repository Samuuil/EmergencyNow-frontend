package com.example.emergencynow.domain.model.response

data class PaginationMeta(
    val itemsPerPage: Int?,
    val totalItems: Int?,
    val currentPage: Int?,
    val totalPages: Int?,
    val sortBy: List<List<String>>?
)

data class PaginationLinks(
    val current: String?,
    val next: String?,
    val previous: String?,
    val first: String?,
    val last: String?
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val meta: PaginationMeta?,
    val links: PaginationLinks?
)
