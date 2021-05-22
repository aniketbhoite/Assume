package com.aniketbhoite.assume.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostListModelItem(
    @Json(name = "body")
    var body: String?,
    @Json(name = "id")
    var id: Int?,
    @Json(name = "title")
    var title: String?,
    @Json(name = "userId")
    var userId: Int?
)
