package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.UserDataSource
import com.example.emergencynow.data.service.UserService

class UserDataSourceImpl(
    private val userService: UserService
) : UserDataSource {
    override suspend fun getUserEgn(userId: String): String {
        return userService.getUserEgn(id = userId).egn
    }

    override suspend fun getMyEgn(): String {
        return userService.getMyEgn().egn
    }
}
