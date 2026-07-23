package com.example.data.network

import com.google.gson.annotations.SerializedName

data class JsonBinResponse<T>(
    @SerializedName("record") val record: T? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null
)
