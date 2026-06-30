package com.applicationrush.elise2027.util

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

fun getPhoneId(context: Context): String {
    val androidId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID,
    ) ?: "unknown"
    return sha256Hex(androidId)
}

fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
