package com.ober.arctic.util.security

import net.grandcentrix.tray.AppPreferences

interface KeyManager {
    var unlockKey: String?
    fun saveEncryptionKey(key: String)
    fun getEncryptionKey(): String?
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
    override var unlockKey: String? = null

    override fun saveEncryptionKey(key: String) {
        val encryptedDataHolder = encryption.encryptStringData(key, unlockKey!!, PBE_ITERATIONS)
        appPreferences.put(ENCRYPTION_KEY, encryptedDataHolder.encryptedData)
        appPreferences.put(ENCRYPTION_KEY_SALT, encryptedDataHolder.salt)
        encryptionKey = key
    }

    override fun getEncryptionKey(): String? {
        if (encryptionKey == null) {
            val encryptedKey = appPreferences.getString(ENCRYPTION_KEY, null)
            val salt = appPreferences.getString(ENCRYPTION_KEY_SALT, null)
            if (encryptedKey != null && salt != null) {
                encryptionKey = encryption.decryptStringData(encryptedKey, salt, unlockKey!!, PBE_ITERATIONS)
            }
        }
        return encryptionKey
    }

    override fun doesRecoveryKeyExist(): Boolean {
        return appPreferences.getString(ENCRYPTION_KEY, null) != null
    }

    override fun isUnlockKeyCorrect(): Boolean {
        return try {
            getEncryptionKey()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun clearKeys() {
        encryptionKey = null
        unlockKey = null
    }

    companion object {
        const val ENCRYPTION_KEY = "encryption_key"
        const val ENCRYPTION_KEY_SALT = "encryption_key_salt"
        const val PBE_ITERATIONS = 100000
    }
}