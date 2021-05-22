package com.aniketbhoite.assume.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentModel(
    @Json(name = "body")
    var body: String?,
    @Json(name = "email")
    var email: String?,
    @Json(name = "id")
    var id: Int?,
    @Json(name = "name")
    var name: String?,
    @Json(name = "postId")
    var postId: Int?
)
