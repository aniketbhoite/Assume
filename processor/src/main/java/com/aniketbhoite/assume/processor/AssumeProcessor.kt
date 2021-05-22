package com.aniketbhoite.assume.processor

import com.aniketbhoite.assume.annotations.Assume
import com.aniketbhoite.assume.annotations.PathIndexes
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class AssumeProcessor : AbstractProcessor(), KotlinProcessingEnvironment {

    private val generatedDir: File? get() = options[KAPT_KOTLIN_GENERATED]?.let(::File)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): Set<String> =
        setOf(Assume::class.java.canonicalName)

    override val processingEnv: ProcessingEnvironment
        get() = super.processingEnv

    private val requestHashMap = hashMapOf<String, Triple<String, Int, MutableList<Int>>>()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val outputDir = generatedDir
        if (outputDir == null) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Cannot find generated output dir."
            )
            return false
        }

        val elements = roundEnv.getElementsAnnotatedWith(Assume::class.java)

        for (element in elements) {

            val interfaceElement = element.enclosingElement
            val metadata = interfaceElement.kotlinClassMetadata()

            if (metadata == null || !interfaceElement.kind.isInterface) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "method Parent must be Interface",
                    element
                )
                continue
            }

            if (element.kind != ElementKind.METHOD) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Annotation must be only on Method",
                    element
                )
                continue
            }

            val assumeAnnotation = element.getAnnotation(Assume::class.java)
            val retrofitMethodAnnotationValue =
                when {
                    element.getAnnotation(GET::class.java) != null ->
                        element.getAnnotation(GET::class.java).value
                    element.getAnnotation(POST::class.java) != null ->
                        element.getAnnotation(POST::class.java).value
                    element.getAnnotation(PUT::class.java) != null ->
                        element.getAnnotation(PUT::class.java).value
                    element.getAnnotation(DELETE::class.java) != null ->
                        element.getAnnotation(DELETE::class.java).value
                    element.getAnnotation(PATCH::class.java) != null ->
                        element.getAnnotation(PATCH::class.java).value
                    else -> null
                }

            if (retrofitMethodAnnotationValue == null) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Cannot find Retrofit `REQUEST METHOD` annotations. check here https://square.github.io/retrofit  "
                )
                return false
            }

            messager.printMessage(
                Diagnostic.Kind.WARNING,
                "AssumeProcessor $retrofitMethodAnnotationValue  ${assumeAnnotation.response} "
            )

            if (retrofitMethodAnnotationValue.isNotBlank() && !assumeAnnotation.ignore) {

                val seg = retrofitMethodAnnotationValue.split("/").toMutableList()
                val pathIndexes = mutableListOf<Int>()

                seg.forEachIndexed { index, s ->
                    if (s.contains("{") && s.contains("}")) {
                        seg[index] = "AS_PATH_AS"
                        pathIndexes.add(index)
                    }
                    seg[index] = getSafeUrlNameForMethod(seg[index])
                }

                requestHashMap += seg.joinToString("SLASH") to Triple(
                    assumeAnnotation.response,
                    assumeAnnotation.responseCode,
                    pathIndexes
                )
            }
        }

        generateClass(outputDir)

        return true
    }

    private fun generateClass(outputDir: File) {

        if (requestHashMap.isEmpty())
            return

        val pairClassName = ClassName("kotlin", "Pair")
        val pairFirstClassName = ClassName("kotlin", "String")
        val pairSecondClassName = ClassName("kotlin", "Int")
        val typeSpec = TypeSpec.companionObjectBuilder()
        val returnType: ParameterizedTypeName =
            pairClassName.parameterizedBy(pairFirstClassName, pairSecondClassName)

        requestHashMap.forEach { (key, value) ->

            val funSpec = FunSpec.builder(
                "get${getSafeUrlNameForMethod(key)}"
            )
                .returns(returnType)
                .addStatement("return (%S to ${value.second})", value.first)

            if (value.third.size > 0)
                funSpec.addAnnotation(
                    AnnotationSpec.builder(PathIndexes::class.java)
                        .addMember("index = ${value.third}")
                        .build()
                )

            typeSpec.addFunction(
                funSpec = funSpec.build()
            )
        }

        val classBuilder = TypeSpec.classBuilder("AssumeClass")
            .addType(
                typeSpec.build()
            )
            .build()

        FileSpec.builder(
            "com.aniketbhoite.assume.mocker",
            "AssumeClass"
        )
            .addType(classBuilder)
            .addComment("This file is generated, DO NOT edit")
            .build()
            .writeTo(outputDir)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED = "kapt.kotlin.generated"

        private fun getUrlWithoutQueryParam(url: String): String {
            var methodName = url
            if (methodName.contains("?")) {
                methodName = methodName.subSequence(0, methodName.indexOf('?')).toString()
            }
            return methodName
        }

        fun getSafeUrlNameForMethod(url: String): String {
            var methodName = getUrlWithoutQueryParam(url)

            methodName = methodName.replace("-", "DASH")
                .replace("/", "SLASH")
                .replace(".", "DOT")

            return methodName
        }
    }
}
