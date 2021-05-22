package com.aniketbhoite.assume.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommentModel(
    var body: String,
    var email: String?,
    var id: Int?,
    var name: String?,
    var postId: Int?
)
