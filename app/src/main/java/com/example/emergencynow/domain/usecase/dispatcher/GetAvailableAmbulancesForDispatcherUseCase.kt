package com.example.emergencynow.domain.usecase.dispatcher

import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.repository.DispatcherRepository

class GetAvailableAmbulancesForDispatcherUseCase(private val repository: DispatcherRepository) {
    suspend operator fun invoke(): Result<List<DispatcherAmbulanceSummary>> {
        return repository.getAvailableAmbulances()
    }
}
