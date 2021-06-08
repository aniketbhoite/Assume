package com.aniketbhoite.assume

import com.aniketbhoite.assume.annotations.PathIndexes
import com.aniketbhoite.assume.processor.AssumeProcessor
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.descriptors.runtime.components.tryLoadClass
import org.junit.Test
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.starProjectedType

class AssumeProcessorTest {

    @Test
    fun `AssumeProcessor invoked when annotated with Assume`() {
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
    fun `AssumeClass is generated when annotated with Assume`() {
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
    fun `function generated with correct name and return type when annotated with Assume`() {
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

        assertThat(kClazzCompanionObject?.functions?.size).isNotEqualTo(0)
        val kFunc = kClazzCompanionObject?.functions?.find { it.name == "getposts" }
        assertThat(kFunc).isNotNull()

        val nonNullStringType = String::class.starProjectedType
        val nonNullIntType = Int::class.starProjectedType

        val stringProjection = KTypeProjection.invariant(nonNullStringType)
        val intProjection = KTypeProjection.invariant(nonNullIntType)

        val pairClass = Pair::class

        val pairReturnType = pairClass.createType(listOf(stringProjection, intProjection))

        assertThat(kFunc?.returnType).isEqualTo(pairReturnType)
    }

    @Test
    fun `generated function returns correct response and response code when annotated with Assume`() {
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

        assertThat(responsePair.first).isInstanceOf(String::class.java)
        assertThat(responsePair.first).isEqualTo("{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}")

        assertThat(responsePair.second is Int).isTrue()
        assertThat(responsePair.second).isEqualTo(200)
    }

    @Test
    fun `compile time error when annotated function is not enclosed in interface`() {
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
                            fun getPostById(): String {
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
    fun `compilation error when Assume annotation is applied to parent class`() {
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
                            fun getPostById(): String {
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
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains(
            "java.lang.NullPointerException\n" +
                "\tat com.aniketbhoite.assume.processor.ProcessorUtilKt.kotlinClassMetadata(ProcessorUtil.kt:12)"
        )
    }

    @Test
    fun `compile time error when Assume annotation is applied on inner child class`() {
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
                                fun getPostById(): String {
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
    fun `compile time error when Assume annotation is applied on variable`() {
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

    @Test
    fun `check method generation & response for all retrofit methods when annotated with Assume`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET
                        import retrofit2.http.POST
                        import retrofit2.http.Query
                        import retrofit2.http.PUT
                        import retrofit2.http.DELETE
                        import retrofit2.http.PATCH

                        interface NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                             @GET("posts")
                            suspend fun getPostById(): String


                            @Assume(
                                response = "[\n" +
                                    "  {\n" +
                                    "    \"postId\": 1,\n" +
                                    "    \"id\": 1,\n" +
                                    "    \"name\": \"John Doe\",\n" +
                                    "    \"email\": \"johndoe@gardner.biz\",\n" +
                                    "    \"body\": \"Comment 3\"\n" +
                                    "  },\n" +
                                    "  {\n" +
                                    "    \"postId\": 1,\n" +
                                    "    \"id\": 2,\n" +
                                    "    \"name\": \"Alice\",\n" +
                                    "    \"email\": \"alice@sydney.com\",\n" +
                                    "    \"body\": \"Comment 2\"\n" +
                                    "  }\n" +
                                    "]"
                            )
                            @POST("comments")
                            suspend fun queryCommentsForPostId(@Query("postId") id: Int): Any


                            @Assume(
                                responseCode = 403,
                                response =
                                "{\"userId\": 2, \"id\": 2, \"title\": \"Test title 2\", \"body\": \"Test title 2-2\"}"
                            )
                            @PUT("posts_put")
                            suspend fun getPost2ById(): Any


                            @Assume(
                                response =
                                "{\"userId\": 3, \"id\": 3, \"title\": \"Test title 3\", \"body\": \"Test title 2-3\"}"
                            )
                            @DELETE("posts-delete")
                            suspend fun getPost3ById(): Any



                            @Assume(
                                response =
                                "{\"userId\": 3, \"id\": 3, \"title\": \"Test title 3\", \"body\": \"Test title 2-3\"}"
                            )
                            @PATCH("posts/patch")
                            suspend fun getPost4ById(): Any
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
        val kClazzCompanionObjectInstance = kClazz.companionObjectInstance
        assertThat(kClazzCompanionObject)

        assertThat(kClazzCompanionObject?.functions?.size == 0).isFalse()

        val kGetFunc = kClazzCompanionObject?.functions?.find { it.name == "getposts" }
        assertThat(kGetFunc).isNotNull()

        val getResponsePair = kGetFunc?.call(kClazzCompanionObjectInstance) as Pair<*, *>

        assertThat(getResponsePair.first).isInstanceOf(String::class.java)
        assertThat(getResponsePair.first).isEqualTo("{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}")

        assertThat(getResponsePair.second is Int).isTrue()
        assertThat(getResponsePair.second).isEqualTo(200)

        val kPostFunc = kClazzCompanionObject.functions.find { it.name == "getcomments" }
        assertThat(kPostFunc).isNotNull()

        val postResponsePair = kPostFunc?.call(kClazzCompanionObjectInstance) as Pair<*, *>

        assertThat(postResponsePair.first).isInstanceOf(String::class.java)
        assertThat(
            (postResponsePair.first as String).trim()
        ).isEqualTo(
            "[\n" +
                "  {\n" +
                "    \"postId\": 1,\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"John Doe\",\n" +
                "    \"email\": \"johndoe@gardner.biz\",\n" +
                "    \"body\": \"Comment 3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"postId\": 1,\n" +
                "    \"id\": 2,\n" +
                "    \"name\": \"Alice\",\n" +
                "    \"email\": \"alice@sydney.com\",\n" +
                "    \"body\": \"Comment 2\"\n" +
                "  }\n" +
                "]"
        )

        assertThat(postResponsePair.second is Int).isTrue()
        assertThat(postResponsePair.second).isEqualTo(200)

        val kPutFunc = kClazzCompanionObject.functions.find { it.name == "getposts_put" }
        assertThat(kPutFunc).isNotNull()

        val putResponsePair = kPutFunc?.call(kClazzCompanionObjectInstance) as Pair<*, *>

        assertThat(putResponsePair.first).isInstanceOf(String::class.java)
        assertThat(putResponsePair.first).isEqualTo("{\"userId\": 2, \"id\": 2, \"title\": \"Test title 2\", \"body\": \"Test title 2-2\"}")

        assertThat(putResponsePair.second is Int).isTrue()
        assertThat(putResponsePair.second).isEqualTo(403)

        val kDeleteFunc = kClazzCompanionObject.functions.find { it.name == "getpostsDASHdelete" }
        assertThat(kDeleteFunc).isNotNull()

        val deleteResponsePair = kDeleteFunc?.call(kClazzCompanionObjectInstance) as Pair<*, *>

        assertThat(deleteResponsePair.first).isInstanceOf(String::class.java)
        assertThat(deleteResponsePair.first).isEqualTo("{\"userId\": 3, \"id\": 3, \"title\": \"Test title 3\", \"body\": \"Test title 2-3\"}")

        assertThat(deleteResponsePair.second is Int).isTrue()
        assertThat(deleteResponsePair.second).isEqualTo(200)

        val kPatchFunc = kClazzCompanionObject.functions.find { it.name == "getpostsSLASHpatch" }
        assertThat(kPatchFunc).isNotNull()

        val patchResponsePair = kPatchFunc?.call(kClazzCompanionObjectInstance) as Pair<*, *>

        assertThat(patchResponsePair.first).isInstanceOf(String::class.java)
        assertThat(patchResponsePair.first).isEqualTo("{\"userId\": 3, \"id\": 3, \"title\": \"Test title 3\", \"body\": \"Test title 2-3\"}")

        assertThat(patchResponsePair.second is Int).isTrue()
        assertThat(patchResponsePair.second).isEqualTo(200)
    }

    @Test
    fun `get 200 as default response code for Assume annotated API method`() {

        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET

                        interface NewsApiService {
                            @Assume(
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

        assertThat(responsePair.second is Int).isTrue()
        assertThat(responsePair.second).isEqualTo(200)
    }

    @Test
    fun `skipped method & class generation for single api with ignored`() {
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
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}",
                                ignore = true
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
            result.classLoader.tryLoadClass("com.aniketbhoite.assume.mocker.AssumeClass")?.kotlin
        assertThat(kClazz).isNull()
    }

    @Test
    fun `skipped method generation when ignored`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET
                        import retrofit2.http.POST
                        import retrofit2.http.Query

                        interface NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                             @GET("posts")
                            suspend fun getPostById(): String


                            @Assume(
                                response = "[\n" +
                                    "  {\n" +
                                    "    \"postId\": 1,\n" +
                                    "    \"id\": 1,\n" +
                                    "    \"name\": \"John Doe\",\n" +
                                    "    \"email\": \"johndoe@gardner.biz\",\n" +
                                    "    \"body\": \"Comment 3\"\n" +
                                    "  },\n" +
                                    "  {\n" +
                                    "    \"postId\": 1,\n" +
                                    "    \"id\": 2,\n" +
                                    "    \"name\": \"Alice\",\n" +
                                    "    \"email\": \"alice@sydney.com\",\n" +
                                    "    \"body\": \"Comment 2\"\n" +
                                    "  }\n" +
                                    "]",
                                    ignore = true
                            )
                            @POST("comments")
                            suspend fun queryCommentsForPostId(@Query("postId") id: Int): Any
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
        assertThat(kClazz).isNotNull()

        val kClazzCompanionObject = kClazz.companionObject
        assertThat(kClazzCompanionObject)

        assertThat(kClazzCompanionObject?.functions?.size == 0).isFalse()

        val kGetFunc = kClazzCompanionObject?.functions?.find { it.name == "getposts" }
        assertThat(kGetFunc).isNotNull()

        val kPostFunc = kClazzCompanionObject?.functions?.find { it.name == "getcomments" }
        assertThat(kPostFunc).isNull()
    }

    @Test
    fun `correct method name, generated annotation & response for API with Path variable`() {
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
                    "ApiService.kt",
                    """
                        import com.aniketbhoite.assume.annotations.Assume
                        import retrofit2.http.GET
                        import retrofit2.http.Path

                        interface NewsApiService {
                            @Assume(
                                responseCode = 200,
                                response =
                                "{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}"
                            )
                            @GET("posts/{id}")
                            suspend fun getPostById(@Path("id") id: Int): String
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
        val kFunc = kClazzCompanionObject?.functions?.find { it.name == "getpostsSLASHAS_PATH_AS" }
        assertThat(kFunc).isNotNull()

        val funcAnnotations = kFunc?.annotations
        assertThat(funcAnnotations).isNotNull()
        assertThat(funcAnnotations?.size).isNotEqualTo(0)

        val pathIndexes = funcAnnotations?.find { it is PathIndexes }
        assertThat(pathIndexes).isNotNull()
        val indexes = (pathIndexes as PathIndexes).index
        assertThat(indexes).isNotNull()
        assertThat(indexes.size).isEqualTo(1)
        assertThat(indexes).isEqualTo(intArrayOf(1))

        val responsePair = kFunc.call(kClazz.companionObjectInstance) as Pair<*, *>

        assertThat(responsePair.first).isInstanceOf(String::class.java)
        assertThat(responsePair.first).isEqualTo("{\"userId\": 1, \"id\": 1, \"title\": \"Test title 1\", \"body\": \"Test title 2\"}")

        assertThat(responsePair.second is Int).isTrue()
        assertThat(responsePair.second).isEqualTo(200)
    }

    @Test
    fun `generated function returns correct response and response code when response is provided as json file`() {
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
                                "/Users/nbt762/Documents/Personal/Assume/processor/src/test/kotlin/com/aniketbhoite/assume/response/response.json"
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

        assertThat(responsePair.first).isInstanceOf(String::class.java)
        assertThat(responsePair.first).isEqualTo(
            "[\n" +
                "  {\n" +
                "    \"postId\": 2,\n" +
                "    \"id\": 6,\n" +
                "    \"name\": \"John Dow\",\n" +
                "    \"email\": \"johndope@random.com\",\n" +
                "    \"body\": \"Random Body\"\n" +
                "  }\n" +
                "]"
        )

        assertThat(responsePair.second is Int).isTrue()
        assertThat(responsePair.second).isEqualTo(200)
    }

    @Test
    fun `compile time error when file not found`() {
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
                                "/Users/nbt762/Documents/Personal/Assume/processor/src/test/kotlin/com/aniketbhoite/assume/response/response1.json"
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

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Something wrong with the file")
    }

    @Test
    fun `compile time error when response is invalid`() {
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
                                "/Users/nbt762/Documents/Personal/Assume/processor/src/test/kotlin/com/aniketbhoite/assume/response/response.csv"
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

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Provided response is not valid. please pass json file path or valid json string")
    }
}
