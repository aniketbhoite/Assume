package com.aniketbhoite.assume

import com.aniketbhoite.assume.processor.AssumeProcessor
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Test
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions

class AssumeProcessorTest {

    @Test
    fun `AssumeProcessor exits test`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        interface NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                             @GET("posts")
                            suspend fun getPostById(): String
                }
            """
                )
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        // Test diagnostic output of compiler
        assertThat(result.messages).contains("Assume annotation processor was called")
    }

    @Test
    fun `AssumeClass class created test`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        interface NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                             @GET("posts")
                            suspend fun getPostById(): String
                }
            """
                )
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        assertThat(result.messages).contains("Assume annotation processor was called")

        val kClazz =
            result.classLoader.loadClass("com.aniketbhoite.assume.mocker.AssumeClass").kotlin
        assertThat(kClazz)
    }

    @Test
    fun `Assume function created test`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        interface NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                             @GET("posts")
                            suspend fun getPostById(): String
                }
            """)
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        assertThat(result.messages).contains("Assume annotation processor was called")

        val kClazz =
            result.classLoader.loadClass("com.aniketbhoite.assume.mocker.AssumeClass").kotlin
        assertThat(kClazz)

        val kClazzCompanionObject = kClazz.companionObject
        assertThat(kClazzCompanionObject)

        assertThat(kClazzCompanionObject?.functions?.size == 0).isFalse()
        val kFunc = kClazzCompanionObject?.functions?.find { it.name == "getposts" }
        assertThat(kFunc).isNotNull()
    }

}
