package com.aniketbhoite.assume.processor


import com.aniketbhoite.assume.annotations.Assume
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import retrofit2.http.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class AssumeProcessor : AbstractProcessor(), KotlinProcessingEnvironment {

    val generatedDir: File? get() = options[KAPT_KOTLIN_GENERATED]?.let(::File)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): Set<String> =
        setOf(Assume::class.java.canonicalName)


    override val processingEnv: ProcessingEnvironment
        get() = super.processingEnv


    val hashmap = hashMapOf<String, Pair<String, Int>>()


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
                    "Cannot find Retrofit `REQUEST METHOD` annotations output dir. check here https://square.github.io/retrofit/"
                )
                return false
            }



            messager.printMessage(
                Diagnostic.Kind.WARNING,
                "AssumeProcessor $retrofitMethodAnnotationValue  ${assumeAnnotation.response} "
            )

            hashmap += retrofitMethodAnnotationValue to (assumeAnnotation.response to assumeAnnotation.responseCode)
        }


        generateClass(outputDir)




        return true
    }


    private fun generateClass(outputDir: File) {

        val pairClassName = ClassName("kotlin", "Pair")
        val pairFirstClassName = ClassName("kotlin", "String")
        val pairSecondClassName = ClassName("kotlin", "Int")
        val typeSpec = TypeSpec.companionObjectBuilder()
        val returnType: ParameterizedTypeName =
            pairClassName.parameterizedBy(pairFirstClassName, pairSecondClassName)

        messager.printMessage(
            Diagnostic.Kind.WARNING,
            "myhashmap ${hashmap}  ${hashmap.size} "
        )

        hashmap.forEach { (key, value) ->

            typeSpec.addFunction(
                FunSpec.builder(
                    "get${getSafeUrlNameForMethod(key)}"
                )
                    .returns(returnType)
                    .addStatement("return (%S to ${value.second})", value.first)
                    .build()
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

        fun getSafeUrlNameForMethod(url: String): String {
            var methodName = url
            if (methodName.contains("?")) {
                methodName = methodName.subSequence(0, methodName.indexOf('?')).toString()
            }
            methodName = methodName.replace("-", "DASH")
                .replace("/", "SLASH")
                .replace(".","DOT")

            return methodName
        }
    }

}