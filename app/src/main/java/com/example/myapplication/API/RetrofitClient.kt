package com.example.myapplication.API



import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * HTTP Client for making the API calls to the API
 * Includes client for registering a user, logging in, and another client for authenticated requests
 */
class RetrofitClient {
    private var retrofit: Retrofit

    /**
     * Basic client for registering user
     */
    constructor() {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Client used for logging in
     * uses the interceptor to add a token by converting the email and password
     * @param email
     * @param password
     */

    /**
     * Client using the stored token to make an authenticated request
     * @param bearer
     */
     private constructor(bearer: String) {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthenticationInterceptor(bearer))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: API get() = retrofit.create(API::class.java)

    companion object {
        /**
         * CHANGE IP HERE TO THE IP THAT SERVER IS RUNNING ON
         */

        private const val BASE_URL = "http://connor.pythonanywhere.com/"
        private var mInstance: RetrofitClient? = null
        private var mInstanceToken: RetrofitClient? = null

        // basic instance used for registration
        @get:Synchronized
        val instance: RetrofitClient?
            get() {
                if (mInstance == null) {
                    mInstance = RetrofitClient()
                }
                return mInstance
            }


        @Synchronized
        fun getInstanceToken(bearer: String): RetrofitClient? {
            if (mInstanceToken == null) {
                mInstanceToken = RetrofitClient(bearer)
            }
            return mInstanceToken
        }
    }
}

