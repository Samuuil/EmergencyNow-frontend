package com.example.emergencynow.domain.usecase.user

import com.example.emergencynow.domain.repository.UserRepository

class GetUserRoleUseCase(private val repository: Lazy<UserRepository>) {
    suspend operator fun invoke(userId: String): Result<String> {
        return repository.value.getUserRole(userId)
    }
}
