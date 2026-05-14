package com.example.emergencynow.data.repository

import com.example.emergencynow.data.datasource.DispatcherDataSource
import com.example.emergencynow.data.extensions.safeApiCall
import com.example.emergencynow.domain.model.entity.DispatcherAmbulanceSummary
import com.example.emergencynow.domain.model.entity.DispatcherCallOffer
import com.example.emergencynow.domain.model.mapper.toDomain
import com.example.emergencynow.domain.repository.DispatcherRepository

class DispatcherRepositoryImpl(
    private val dataSource: DispatcherDataSource,
) : DispatcherRepository {

    override suspend fun getMyCalls(): Result<List<DispatcherCallOffer>> = safeApiCall {
        dataSource.getMyCalls().map { it.toDomain() }
    }

    override suspend fun getAvailableAmbulances(): Result<List<DispatcherAmbulanceSummary>> = safeApiCall {
        dataSource.getAvailableAmbulances().map { it.toDomain() }
    }

    override suspend fun assignAmbulance(callId: String, ambulanceId: String): Result<Unit> = safeApiCall {
        dataSource.assignAmbulance(callId, ambulanceId)
    }
}
