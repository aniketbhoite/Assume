package com.aniketbhoite.assume.interceptor

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

internal class AssumeInterceptorHelper {

    companion object {


        val cachedAssumeResponse: HashMap<String, Pair<String, Int>> = hashMapOf()

        private val kClass: KClass<*> by lazy {
            Class.forName("com.aniketbhoite.assume.mocker.AssumeClass").kotlin
        }

        private val kFunctions: Collection<KFunction<*>>? by lazy {
            kClass.companionObject?.functions
        }

        val nonPathFunctionsMap: Map<String, KFunction<*>>? by lazy {
            kFunctions?.filter { !it.name.contains("AS_PATH_AS") }?.associate {
                it.name to it
            }
        }

        val pathFunctions: Collection<KFunction<*>>? by lazy {
            kFunctions?.filter { it.name.contains("AS_PATH_AS") }
        }

        val kClassCompanionObjectInstance: Any? by lazy {
            kClass.companionObjectInstance
        }
    }

}