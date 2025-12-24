package com.example.emergencynow.data.datasource.impl

import com.example.emergencynow.data.datasource.UserDataSource
import com.example.emergencynow.data.service.UserService
import okhttp3.ResponseBody

class UserDataSourceImpl(
    private val userService: UserService
) : UserDataSource {
    override suspend fun getUserRole(userId: String): String {
        val body: ResponseBody = userService.getUserRole(id = userId)
        return body.string().trim()
    }
}
