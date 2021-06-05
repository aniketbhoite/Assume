package com.aniketbhoite.assume

import com.aniketbhoite.assume.processor.AssumeProcessor
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.Test
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.starProjectedType

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
    fun `Assume function created with correct name & returnType test`() {
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

        val kClazzCompanionObject = kClazz.companionObject
        assertThat(kClazzCompanionObject)

        assertThat(kClazzCompanionObject?.functions?.size == 0).isFalse()
        val kFunc = kClazzCompanionObject?.functions?.find { it.name == "getposts" }
        assertThat(kFunc).isNotNull()

        val nonNullStringType = String::class.starProjectedType
        val nonNullIntType = Int::class.starProjectedType

        val stringProjection = KTypeProjection.invariant(nonNullStringType)
        val intProjection = KTypeProjection.invariant(nonNullIntType)

        val pairClass = Pair::class

        val pairReturnType = pairClass.createType(listOf(stringProjection, intProjection))

        assertThat(kFunc?.returnType == pairReturnType).isTrue()
    }

    @Test
    fun `Assume generated response check test`() {
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

        val kClazzCompanionObject = kClazz.companionObject
        assertThat(kClazzCompanionObject)

        assertThat(kClazzCompanionObject?.functions?.size == 0).isFalse()
        val kFunc = kClazzCompanionObject?.functions?.find { it.name == "getposts" }
        assertThat(kFunc).isNotNull()

        val responsePair = kFunc?.call(kClazz.companionObjectInstance) as Pair<*, *>

        assertThat(responsePair.first is String).isTrue()
        assertThat(responsePair.first == "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}").isTrue()

        assertThat(responsePair.second is Int).isTrue()
        assertThat(responsePair.second == 200).isTrue()
    }
}
