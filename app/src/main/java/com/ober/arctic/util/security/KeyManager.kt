package com.ober.arctic.util.security

import android.util.Base64
import com.tozny.crypto.android.AesCbcWithIntegrity
import net.grandcentrix.tray.AppPreferences

interface KeyManager {
    fun savePartialRecoveryKey(key: String)
    fun getPartialRecoveryKey(): String?
    fun setMasterKey(key: String, newKey: Boolean = false)
    fun getCombinedKey(): String?
    fun isMasterKeyCorrect(): Boolean
    fun clearKeys()
}

class KeyManagerImpl(
    private var appPreferences: AppPreferences,
    private var encryption: Encryption
) : KeyManager {

    private var partialRecoveryKey: String? = null
    private var masterKey: String? = null
    private var masterKeySalt: String? = null
    private var masterKeyHash: String? = null

    override fun savePartialRecoveryKey(key: String) {
        appPreferences.put(RECOVERY_KEY, encryption.encryptString(key))
        partialRecoveryKey = key
    }

    override fun getPartialRecoveryKey(): String? {
        if (partialRecoveryKey == null) {
            val encryptedKey = appPreferences.getString(RECOVERY_KEY, null)
            if (encryptedKey != null) {
                partialRecoveryKey = encryption.decryptString(encryptedKey)
            }
        }
        return partialRecoveryKey
    }

    private fun generateNewSalt() {
        masterKeySalt = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt())
        appPreferences.put(MASTER_KEY_SALT, encryption.encryptString(masterKeySalt))
    }

    private fun getMasterKeySalt(): String? {
        if (masterKeySalt == null) {
            val encryptedSalt = appPreferences.getString(MASTER_KEY_SALT, null)
            if (encryptedSalt != null) {
                masterKeySalt = encryption.decryptString(encryptedSalt)
            }
        }
        return masterKeySalt
    }

    private fun saveMasterKeyHash() {
        masterKeyHash = Base64.encodeToString(
            AesCbcWithIntegrity.generateKeyFromPassword(
                masterKey,
                getMasterKeySalt()
            ).confidentialityKey.encoded, Base64.DEFAULT
        )
        appPreferences.put(MASTER_KEY_HASH, encryption.encryptString(masterKeyHash))
    }

    private fun getMasterKeyHash(): String? {
        if (masterKeyHash == null) {
            val encryptedHash = appPreferences.getString(MASTER_KEY_HASH, null)
            if (encryptedHash != null) {
                masterKeyHash = encryption.decryptString(encryptedHash)
            }
        }
        return masterKeyHash
    }

    override fun setMasterKey(key: String, newKey: Boolean) {
        masterKey = key
        if (newKey) {
            generateNewSalt()
            saveMasterKeyHash()
        }
    }

    override fun isMasterKeyCorrect(): Boolean {
        if (masterKey == null) {
            return false
        }
        return getMasterKeyHash()!! == Base64.encodeToString(
            AesCbcWithIntegrity.generateKeyFromPassword(
                masterKey,
                getMasterKeySalt()
            ).confidentialityKey.encoded, Base64.DEFAULT
        )
    }

    override fun getCombinedKey(): String? {
        return masterKey + partialRecoveryKey
    }

    override fun clearKeys() {
        partialRecoveryKey = null
        masterKey = null
    }

    companion object {
        const val RECOVERY_KEY = "recovery_key"
        const val MASTER_KEY_SALT = "master_key_salt"
        const val MASTER_KEY_HASH = "master_key_hash"
    }
}