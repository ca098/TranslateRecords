package com.example.myapplication.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Retrofit client that does not use a token
 * only used by the log in and register activities
 */
class loginClient {

    object RetrofitClient {


        private const val BASE_URL = "http://connor.pythonanywhere.com/"


        val instance: API by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())

                .build()
            retrofit.create(API::class.java)

        }
    }
}

