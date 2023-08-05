@file:JvmName("ReflectExtensions")
package com.getkeepsafe.taptargetview

import kotlin.jvm.Throws

@Throws(NoSuchFieldException::class, IllegalAccessException::class)
fun Any?.getPrivateField(fieldName: String?): Any? {
    if (fieldName == null || this == null) return null
    val field = this.javaClass.getDeclaredField(fieldName)
    field.isAccessible = true
    return field.get(this)
}