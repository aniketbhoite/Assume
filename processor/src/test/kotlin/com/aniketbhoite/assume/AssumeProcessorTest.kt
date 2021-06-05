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

    @Test
    fun `AssumeProcessor failed because Assume annotation containing class was not interface`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        class NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                             @GET("posts")
                            suspend fun getPostById(): String {
                               return ""
                            }
                }
            """
                )
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("error: method Parent must be Interface")
    }

    @Test
    fun `AssumeProcessor will failed because Assume annotation applied on top level parent class`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                        class NewsApiService {

                             @GET("posts")
                            suspend fun getPostById(): String {
                               return ""
                            }
                }
            """
                )
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        /**
         * The exit code for this will be KotlinCompilation.ExitCode.INTERNAL_ERROR because it fail while creating kotlinClassMetadata
         * In above test case Class with Assume class does not have enclosing Element(means parent class)
         * val metadata = interfaceElement.kotlinClassMetadata()
         */
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.INTERNAL_ERROR)
        assertThat(result.messages).contains(
            "java.lang.NullPointerException\n" +
                "\tat com.aniketbhoite.assume.processor.ProcessorUtilKt.kotlinClassMetadata(ProcessorUtil.kt:12)"
        )
    }

    @Test
    fun `AssumeProcessor will failed because Assume annotation applied on inner child class`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        class ParentClass {

                            @Assume(
                                    responseCode = 200,
                                    response =
                                    "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                                )
                            class NewsApiService {

                                 @GET("posts")
                                suspend fun getPostById(): String {
                                   return ""
                                }
                            }
                        }
            """
                )
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("error: method Parent must be Interface")
    }

    @Test
    fun `AssumeProcessor will failed because Assume annotation applied variable`() {
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
                            val variable = ""

                            @GET("posts")
                            suspend fun getPostById(): String {
                               return ""
                            }
                }
            """
                )
            )

            annotationProcessors = listOf(AssumeProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }
}
