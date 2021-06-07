package com.aniketbhoite.assume.processor

import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

fun Element.kotlinClassMetadata(): KotlinClassMetadata? {
    val metadataAnnotation = this.getAnnotation(Metadata::class.java)

    return KotlinClassMetadata.read(
        KotlinClassHeader(
            metadataAnnotation.kind,
            metadataAnnotation.metadataVersion,
            metadataAnnotation.data1,
            metadataAnnotation.data2,
            metadataAnnotation.extraString,
            metadataAnnotation.packageName,
            metadataAnnotation.extraInt
        )
    )
}
