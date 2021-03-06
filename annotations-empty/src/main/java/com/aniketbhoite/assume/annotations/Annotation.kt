package com.aniketbhoite.assume.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Assume(
    val response: String,
    val responseCode: Int = 200,
    val ignore: Boolean = false
)

@Target(AnnotationTarget.FUNCTION)
annotation class PathIndexes(
    vararg val index: Int
)
