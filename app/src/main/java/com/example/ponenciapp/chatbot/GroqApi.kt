package com.example.ponenciapp.chatbot

import retrofit2.Response
import retrofit2.http.*

//Para conectar con la api de groq
interface GroqApi {
    @Headers("Content-Type: application/json")
    @POST("openai/v1/chat/completions")
    suspend fun chat(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}