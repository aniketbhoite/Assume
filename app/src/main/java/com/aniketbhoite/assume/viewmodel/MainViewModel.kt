package com.aniketbhoite.assume.viewmodel

import com.aniketbhoite.assume.ApiService
import com.aniketbhoite.assume.data.CommentModel
import com.aniketbhoite.assume.data.PostListModelItem

/*
 * If you are using Android architecture components in your app then you should extend the ViewModel.
 */
class MainViewModel(private val apiService: ApiService) {

    suspend fun getPost(): List<PostListModelItem> {
        return apiService.getPosts()
    }

    suspend fun getPostById(id: Int): PostListModelItem {
        return apiService.getPostById(id)
    }

    suspend fun getCommentsForPostId(id: Int): List<CommentModel> {
        return apiService.getCommentsForPostId(id)
    }

    suspend fun queryCommentsForPostId(id: Int): List<CommentModel> {
        return apiService.queryCommentsForPostId(id)
    }
}
