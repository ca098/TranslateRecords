package com.example.myapplication.API

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.Models.loginResponse
import com.example.myapplication.R

/**
 * This class saves the user id and the auth token of the current user
 * Every time a user logs in these values will be saved for the specific user and these values
 * will then be deleted when the user logs out.
 *
 * The token value is required to be sent to the django api for all calls after logging in
 *
 * The users id will also be used in api calls to retrieve data about the current user.
 */
class SharedPrefManager private constructor(private val mCtx: Context) {

        fun saveAuthToken(token: String) {
            val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("token", token)
            editor.apply()
    }

    fun saveID(id: String) {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("id", id)
        editor.apply()
    }

    fun getID(): String? {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("id", null)
    }


    fun fetchAuthToken(): String? {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }



    fun clear() {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private val SHARED_PREF_NAME = "my_shared_preff"
        private var mInstance: SharedPrefManager? = null
        @Synchronized
        fun getInstance(mCtx: Context): SharedPrefManager {
            if (mInstance == null) {
                mInstance = SharedPrefManager(mCtx)
            }
            return mInstance as SharedPrefManager
        }
    }

}


//class SharedPrefManager (context: Context) {
//    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
//    private var mInstance: SharedPrefManager? = null
//
//    companion object {
//        const val USER_TOKEN = "user_token"
//    }
//
//    /**
//     * Function to save auth token
//     */
//    fun saveAuthToken(token: String) {
//        val editor = prefs.edit()
//        editor.putString(USER_TOKEN, token)
//        editor.apply()
//    }
//
//    /**
//     * Function to fetch auth token
//     */
//    fun fetchAuthToken(): String? {
//        return prefs.getString(USER_TOKEN, null)
//    }
//
//
//    fun clear() {
//        val editor = prefs.edit()
//        editor.clear()
//        editor.apply()
//    }
//
//    @Synchronized
//    fun getInstance(context: Context): SharedPrefManager? {
//        if (mInstance == null) {
//            mInstance = SharedPrefManager(context)
//        }
//        return mInstance
//    }
//
//}