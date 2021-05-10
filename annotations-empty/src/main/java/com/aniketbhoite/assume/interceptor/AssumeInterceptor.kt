package com.aniketbhoite.assume.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AssumeInterceptor(baseUrl: String = "") : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}