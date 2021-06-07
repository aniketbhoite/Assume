package com.aniketbhoite.assume.di

import com.aniketbhoite.assume.ApiService
import com.aniketbhoite.assume.interceptor.AssumeInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
 * This is just a sample app to show how to use Assume.
 * We don't encourage to use dependency injection this way so don't take offense.
 */

object DI {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    @JvmStatic
    fun provideApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @JvmStatic
    private fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(AssumeInterceptor(BASE_URL))
            .addInterceptor(logging)
            .build()
    }
}
