package com.ce46.connectme.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Student ID + password live as AES-GCM ciphertext in app-private storage.
 * The AES key never leaves Android Keystore. There is no plaintext fallback.
 */
class CredentialStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(studentId: String, password: String) {
        val payload = JSONObject()
            .put("id", studentId)
            .put("pw", password)
            .toString()
            .toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(payload)
        prefs.edit()
            .putString(KEY_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .putString(KEY_BLOB, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun load(): Credentials? {
        val ivB64 = prefs.getString(KEY_IV, null) ?: return null
        val blobB64 = prefs.getString(KEY_BLOB, null) ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, Base64.decode(ivB64, Base64.NO_WRAP))
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
            val json = JSONObject(String(cipher.doFinal(Base64.decode(blobB64, Base64.NO_WRAP)), Charsets.UTF_8))
            val id = json.optString("id")
            val pw = json.optString("pw")
            if (id.isBlank() || pw.isBlank()) null else Credentials(id, pw)
        } catch (e: Exception) {
            Log.w(TAG, "decrypt failed: ${e.message}")
            null
        }
    }

    fun hasCredentials(): Boolean = load() != null

    fun studentIdMasked(): String {
        val id = load()?.studentId ?: return ""
        if (id.length <= 3) return id
        return id.take(2) + "•".repeat((id.length - 3).coerceAtMost(6)) + id.takeLast(1)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val TAG = "CredentialStore"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "connectme_portal_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFS = "connectme_creds_enc"
        private const val KEY_IV = "iv"
        private const val KEY_BLOB = "blob"
    }
}
