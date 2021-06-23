package com.github.kittinunf.cored.app.repository

import com.github.kittinunf.cored.app.api.User
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.http.URLProtocol

interface UserRepository {

    suspend fun getUsers(): List<User>?
}

class UserRepositoryImpl(private val client: HttpClient = createHttpClient()) : UserRepository {
   
    override suspend fun getUsers(): List<User>? {
        return try {
            client.get<List<User>>("/users")
        } catch (e: Exception) {
            println(e.stackTraceToString())
            null
        }
    }
}

private fun createHttpClient() = HttpClient {
    Json {
        serializer = KotlinxSerializer()
    }
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "jsonplaceholder.typicode.com"
        }
    }
}
