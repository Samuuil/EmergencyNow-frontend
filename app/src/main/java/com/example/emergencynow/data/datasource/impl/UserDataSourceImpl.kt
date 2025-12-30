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
    
    override suspend fun getUserEgn(userId: String): String {
        val response = userService.getUserEgn(id = userId)
        return response.egn
    }
    
    override suspend fun getMyEgn(): String {
        val response = userService.getMyEgn()
        return response.egn
    }
}
