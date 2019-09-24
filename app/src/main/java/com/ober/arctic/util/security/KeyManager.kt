package com.ober.arctic.util.security

import net.grandcentrix.tray.AppPreferences

interface KeyManager {
    var unlockKey: String?
    fun saveEncryptionKey(key: String): Boolean
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

    /**
     * return true if successful
     */
    override fun saveEncryptionKey(key: String): Boolean {
        unlockKey?.let {
            val encryptedDataHolder = encryption.encryptStringData(key, it, PBE_ITERATIONS)
            appPreferences.put(ENCRYPTION_KEY, encryptedDataHolder.encryptedData)
            appPreferences.put(ENCRYPTION_KEY_SALT, encryptedDataHolder.salt)
            encryptionKey = key
            return true
        } ?: return false
    }

    override fun getEncryptionKey(): String? {
        if (encryptionKey == null) {
            unlockKey?.let {
                val encryptedKey = appPreferences.getString(ENCRYPTION_KEY, null)
                val salt = appPreferences.getString(ENCRYPTION_KEY_SALT, null)
                if (encryptedKey != null && salt != null) {
                    encryptionKey = encryption.decryptStringData(encryptedKey, salt, it, PBE_ITERATIONS)
                }
            }
        }
        return encryptionKey
    }

    override fun doesRecoveryKeyExist(): Boolean {
        return appPreferences.getString(ENCRYPTION_KEY, null) != null
    }

    override fun isUnlockKeyCorrect(): Boolean {
        return try {
            getEncryptionKey() != null
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