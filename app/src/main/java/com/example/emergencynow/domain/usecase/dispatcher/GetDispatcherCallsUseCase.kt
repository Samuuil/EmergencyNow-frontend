package com.example.emergencynow.domain.usecase.dispatcher

import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.repository.DispatcherRepository

class GetDispatcherCallsUseCase(private val repository: DispatcherRepository) {
    suspend operator fun invoke(): Result<List<DispatcherCallOffer>> {
        return repository.getMyCalls()
    }
}
