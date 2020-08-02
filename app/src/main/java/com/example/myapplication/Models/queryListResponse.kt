package com.example.myapplication.Models

import com.google.gson.annotations.SerializedName

data class queryListResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("queries") val queries: List<queryResponse>)


//data class queriesList(
//    @SerializedName("initialText")val initialText: String,
//    @SerializedName("language")val language: String,
//    @SerializedName("translatedText")val translatedText: String,
//    @SerializedName("date_created")val date_created: String,
//    @SerializedName("owner")val owner: String,
//    @SerializedName("id")val id: String)