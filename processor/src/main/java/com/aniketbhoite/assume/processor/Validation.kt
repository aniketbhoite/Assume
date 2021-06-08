package com.aniketbhoite.assume.processor

/**
 * Created by Akash on 08/06/21
 */
fun String.isValidResponse(): Boolean {
    return this.endsWith(".json") ||
        (this.startsWith("{") && this.endsWith("}")) ||
        (this.startsWith("[") && this.endsWith("]"))
}
