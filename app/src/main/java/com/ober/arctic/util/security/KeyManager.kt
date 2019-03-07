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

class KeyManagerImpl(
    private var appPreferences: AppPreferences,
    private var encryption: Encryption
) : KeyManager {

    private var encryptionKey: String? = null
    private var unlockKey: String? = null
    private var unlockKeySalt: String? = null
    private var unlockKeyHash: String? = null

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

    private fun generateNewUnlockKeySalt() {
        unlockKeySalt = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt())
        appPreferences.put(UNLOCK_KEY_SALT, encryption.encryptString(unlockKeySalt, unlockKey!!))
    }

    private fun getUnlockKeySalt(): String? {
        if (unlockKeySalt == null) {
            val encryptedSalt = appPreferences.getString(UNLOCK_KEY_SALT, null)
            if (encryptedSalt != null) {
                unlockKeySalt = encryption.decryptString(encryptedSalt, unlockKey!!)
            }
        }
        return unlockKeySalt
    }

    private fun saveUnlockKeyHash() {
        unlockKeyHash = Base64.encodeToString(
            AesCbcWithIntegrity.generateKeyFromPassword(
                unlockKey,
                getUnlockKeySalt()
            ).confidentialityKey.encoded, Base64.DEFAULT
        )
        appPreferences.put(UNLOCK_KEY_HASH, encryption.encryptString(unlockKeyHash, unlockKey!!))
    }

    private fun getUnlockKeyHash(): String? {
        if (unlockKeyHash == null) {
            val encryptedHash = appPreferences.getString(UNLOCK_KEY_HASH, null)
            if (encryptedHash != null) {
                unlockKeyHash = encryption.decryptString(encryptedHash, unlockKey!!)
            }
        }
        return unlockKeyHash
    }

    override fun setUnlockKey(key: String, newKey: Boolean) {
        unlockKey = key
        if (newKey) {
            generateNewUnlockKeySalt()
            saveUnlockKeyHash()
        }
    }

    override fun isUnlockKeyCorrect(): Boolean {
        if (unlockKey == null) {
            return false
        }
        return getUnlockKeyHash()!! == Base64.encodeToString(
            AesCbcWithIntegrity.generateKeyFromPassword(
                unlockKey,
                getUnlockKeySalt()
            ).confidentialityKey.encoded, Base64.DEFAULT
        )
    }

    override fun clearKeys() {
        encryptionKey = null
        unlockKey = null
    }

    companion object {
        const val ENCRYPTION_KEY = "encryption_key"
        const val UNLOCK_KEY_SALT = "unlock_key_salt"
        const val UNLOCK_KEY_HASH = "unlock_key_hash"
    }
}