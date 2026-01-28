package com.example.emergencynow.domain.usecase.call

import com.example.emergencynow.domain.model.entity.Call
import com.example.emergencynow.domain.repository.CallRepository

class GetUserCallsUseCase(private val repository: Lazy<CallRepository>) {
    suspend operator fun invoke(
        page: Int? = null,
        limit: Int? = null
    ): Result<List<Call>> {
        return repository.value.getMyCalls(page, limit)
    }
}
