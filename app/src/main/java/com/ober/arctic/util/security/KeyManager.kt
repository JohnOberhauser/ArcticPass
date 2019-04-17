package com.ober.arctic.util.security

import android.util.Base64
import com.tozny.crypto.android.AesCbcWithIntegrity
import net.grandcentrix.tray.AppPreferences

interface KeyManager {
    fun saveEncryptionKey(key: String)
    fun getEncyptionKey(): String?
    fun setUnlockKey(key: String, newKey: Boolean = false)
    fun isUnlockKeyCorrect(): Boolean
    fun clearKeys()
    fun doesRecoveryKeyExist(): Boolean
}

/**
 * unlock key only unlocks the encryption key (the backup key)
 */
class KeyManagerImpl(
    private var appPreferences: AppPreferences,
    private var encryption: Encryption
) : KeyManager {

    private var encryptionKey: String? = null
    private var unlockKey: String? = null

    override fun saveEncryptionKey(key: String) {
        appPreferences.put(ENCRYPTION_KEY, encryption.encryptString(key, unlockKey!!))
        encryptionKey = key
    }

    override fun getEncyptionKey(): String? {
        if (encryptionKey == null) {
            val encryptedKey = appPreferences.getString(ENCRYPTION_KEY, null)
            if (encryptedKey != null) {
                encryptionKey = encryption.decryptString(encryptedKey, unlockKey!!)
            }
        }
        return encryptionKey
    }

    override fun doesRecoveryKeyExist(): Boolean {
        return appPreferences.getString(ENCRYPTION_KEY, null) != null
    }

    override fun setUnlockKey(key: String, newKey: Boolean) {
        unlockKey = key
    }

    override fun isUnlockKeyCorrect(): Boolean {
        if (unlockKey == null) {
            return false
        }
        return encryption.getStoredSecretKeys(unlockKey!!) != null
    }

    override fun clearKeys() {
        encryptionKey = null
        unlockKey = null
    }

    companion object {
        const val ENCRYPTION_KEY = "encryption_key"
    }
}