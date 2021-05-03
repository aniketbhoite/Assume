package com.aniketbhoite.assume.interceptor

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

class AssumeInterceptor(private val baseUrl: String = "") : Interceptor {

    private val encodedPathToRemove: String =
        baseUrl.toHttpUrlOrNull()?.newBuilder()?.build()?.encodedPath ?: ""

    private val cachedAssumeResponse: HashMap<String, Pair<String, Int>> = hashMapOf()

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val url = request.url

        var mockResponse: String = ""
        var mockResponseCode = 200

        try {
            val methodName = "get${
                getSafeUrlNameForMethod(url.encodedPath.replace(encodedPathToRemove, ""))
            }"

            val responsePair = if (cachedAssumeResponse.containsKey(methodName)) {
                cachedAssumeResponse[methodName]
            } else {
                val kClass =
                    Class.forName("com.aniketbhoite.assume.mocker.AssumeClass")

                val expectedFunction =
                    kClass.kotlin.companionObject?.functions?.find { it.name == methodName }
                val localResponsePair =
                    expectedFunction?.call(kClass.kotlin.companionObjectInstance)
                if (localResponsePair is Pair<*, *> && localResponsePair.first is String && localResponsePair.second is Int)
                    cachedAssumeResponse[methodName] = localResponsePair as Pair<String, Int>

                localResponsePair
            }






            if (responsePair is Pair<*, *> && responsePair.first is String && responsePair.second is Int) {
                mockResponse = responsePair.first as String
                mockResponseCode = responsePair.second as Int
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (mockResponse.isNotEmpty()) {
                return chain.proceed(chain.request())
                    .newBuilder()
                    .code(mockResponseCode)
                    .protocol(Protocol.HTTP_2)
                    .message(mockResponse)
                    .body(
                        ResponseBody.create(
                            "application/json".toMediaTypeOrNull(),
                            mockResponse.toByteArray()
                        )
                    )
                    .addHeader("content-type", "application/json")
                    .build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }




        return chain.proceed(chain.request())
    }

    companion object {
        fun getSafeUrlNameForMethod(url: String): String {

            return url.replace("-", "DASH")
                .replace("/", "SLASH")
        }
    }
}