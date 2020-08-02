package com.example.myapplication.Models

import com.google.gson.annotations.SerializedName

data class loginResponse(@SerializedName("token")
                         var token: String,
                         @SerializedName("id")
                         var id: String)