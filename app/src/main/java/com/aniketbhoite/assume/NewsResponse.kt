package com.aniketbhoite.assume

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class NewsResponse(
    var status: String,

    val totalResults: Int
)
