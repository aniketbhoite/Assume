package com.aniketbhoite.assume.interceptor

import com.aniketbhoite.assume.annotations.PathIndexes
import com.aniketbhoite.assume.interceptor.AssumeInterceptorHelper.Companion.cachedAssumeResponse
import com.aniketbhoite.assume.interceptor.AssumeInterceptorHelper.Companion.kClassCompanionObjectInstance
import com.aniketbhoite.assume.interceptor.AssumeInterceptorHelper.Companion.nonPathFunctionsMap
import com.aniketbhoite.assume.interceptor.AssumeInterceptorHelper.Companion.pathFunctions
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class AssumeInterceptor(baseUrl: String = "") : Interceptor {

    private val baseHttpUrl = HttpUrl.parse(baseUrl)?.newBuilder()?.build()

    private val encodedPathToRemove: String =
        baseHttpUrl?.encodedPath() ?: ""

    private val encodePathSegmentsToRemove =
        baseHttpUrl?.encodedPathSegments() ?: emptyList()

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        val url = request.url()

        var mockResponse = ""
        var mockResponseCode = 200

        try {
            val methodName = "get${
            getSafeUrlNameForMethod(url.encodedPath().replaceFirst(encodedPathToRemove, ""))
            }"

            val responsePair = if (cachedAssumeResponse.containsKey(methodName)) {
                cachedAssumeResponse[methodName]
            } else {

                var expectedFunction =
                    nonPathFunctionsMap?.get(methodName)

                if (expectedFunction == null) {
                    pathFunctions?.forEach main@{ kFunc ->
                        kFunc.annotations.forEach {
                            if (it is PathIndexes) {
                                val kFuncName = "get${
                                getPathSafeURLNameForMethod(
                                    it.index,
                                    url.encodedPathSegments().removeBaseUrlSegments(
                                        encodePathSegmentsToRemove
                                    )
                                )
                                }"
                                if (kFunc.name == kFuncName) {
                                    expectedFunction = kFunc
                                    return@main
                                }
                            }
                        }
                    }
                }

                val localResponsePair =
                    expectedFunction?.call(kClassCompanionObjectInstance)
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

                val response = chain.proceed(chain.request())
                val contentType = response.body()?.contentType()

                return response
                    .newBuilder()
                    .code(mockResponseCode)
                    .message(mockResponse)
                    .body(
                        ResponseBody.create(
                            contentType,
                            mockResponse
                        )
                    )
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
                .replace(".", "DOT")
        }

        fun getPathSafeURLNameForMethod(indexes: IntArray, segments: List<String>): String {
            val pathSegments = segments.toMutableList()
            indexes.forEach {
                pathSegments[it] = "AS_PATH_AS"
            }

            return getSafeUrlNameForMethod(pathSegments.joinToString("/"))
        }

        fun List<String>.removeBaseUrlSegments(segments: List<String>): List<String> {
            val cleanUrlSegments = this.toMutableList()
            cleanUrlSegments.removeAll(segments)
            return cleanUrlSegments
        }
    }
}
