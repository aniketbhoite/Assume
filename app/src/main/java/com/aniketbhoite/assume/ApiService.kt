package com.aniketbhoite.assume

import com.aniketbhoite.assume.annotations.Assume
import com.aniketbhoite.assume.data.CommentModel
import com.aniketbhoite.assume.data.PostListModelItem
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @Assume(
        responseCode = 200,
        response = "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
    )
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostListModelItem

    @Assume(
        response = "[\n" +
                "  {\n" +
                "    \"userId\": 1,\n" +
                "    \"id\": 1,\n" +
                "    \"title\": \"Test title 1\",\n" +
                "    \"body\": \"Test title 1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"userId\": 2,\n" +
                "    \"id\": 2,\n" +
                "    \"title\": \"Test title 2\",\n" +
                "    \"body\": \"Test title 2\"\n" +
                "  }\n" +
                "]"
    )
    @GET("posts")
    suspend fun getPosts(): List<PostListModelItem>

    @Assume(
        responseCode = 200,
        response = "[\n" +
                "  {\n" +
                "    \"postId\": 1,\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"John Doe\",\n" +
                "    \"email\": \"johndoe@gardner.biz\",\n" +
                "    \"body\": \"Comment 1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"postId\": 1,\n" +
                "    \"id\": 2,\n" +
                "    \"name\": \"Alice\",\n" +
                "    \"email\": \"alice@sydney.com\",\n" +
                "    \"body\": \"Comment 2\"\n" +
                "  }\n" +
                "]",
        ignore = false
    )
    @GET("post/{id}/comments")
    suspend fun getCommentsForPostId(@Path("id") id: Int): List<CommentModel>

    @Assume(
        response = "/Users/nbt762/Documents/Personal/Assume/processor/src/test/kotlin/com/aniketbhoite/assume/response/response.json"
    )
    @GET("comments")
    suspend fun queryCommentsForPostId(@Query("postId") id: Int): List<CommentModel>
}