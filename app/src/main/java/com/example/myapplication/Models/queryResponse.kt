package com.example.myapplication.Models

import com.google.gson.annotations.SerializedName

data class queryResponse(
    @SerializedName("initialText")val initialText: String,
    @SerializedName("language")val language: String,
    @SerializedName("translatedText")val translatedText: String,
    @SerializedName("date_created")val date_created: String,
    @SerializedName("owner")val owner: String,
    @SerializedName("id")val id: String) {


    override fun toString(): String {
        return "queryResponse(initialText='$initialText', language='$language'," +
                " translatedText='$translatedText', date_created='$date_created'," +
                "owner='$owner', id='$id')"

    }

}