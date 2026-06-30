package com.applicationrush.elise2027.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Generates the HMAC token expected by the backend.
 * Formula: HMAC-SHA256(phoneId + ":" + YYYYMMDD, VOTE_HMAC_SECRET)
 * phoneId must already be the SHA-256 hash of ANDROID_ID.
 */
fun generateHmacToken(phoneId: String, hmacSecret: String): String {
    val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    val message = "$phoneId:$date"
    val mac = Mac.getInstance("HmacSHA256")
    val keySpec = SecretKeySpec(hmacSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
    mac.init(keySpec)
    return mac.doFinal(message.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
