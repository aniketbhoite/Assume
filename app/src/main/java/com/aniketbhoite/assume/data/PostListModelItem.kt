package com.aniketbhoite.assume.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PostListModelItem(
    var body: String,
    var id: Int?,
    var title: String,
    var userId: Int?
)
