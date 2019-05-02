package com.ober.arctic.util.security

import com.ober.arctic.data.model.EncryptedDataHolder
import java.security.*

interface Encryption {
    fun generateRandomKey(length: Int): String
    fun encryptStringData(data: String, password: String, pbeIterations: Int = 30000): EncryptedDataHolder
    fun decryptStringData(data: String, salt: String, password: String, pbeIterations: Int = 30000): String
}

class EncryptionImpl : Encryption {

    override fun generateRandomKey(length: Int): String {
        Crypt.fixPrng()
        val keyBuilder = StringBuilder()
        val secureRandom = SecureRandom()
        for (i in 1..length) {
            keyBuilder.append(CHARACTERS[secureRandom.nextInt(CHARACTERS.length)])
        }
        return keyBuilder.toString()
    }

    override fun encryptStringData(data: String, password: String, pbeIterations: Int): EncryptedDataHolder {
        val salt: String = Crypt.saltString(Crypt.generateSalt())
        val secretKeys: Crypt.SecretKeys =
            Crypt.generateKeyFromPassword(password, salt, pbeIterations)
        return EncryptedDataHolder(salt, encrypt(data.toByteArray(), secretKeys))
    }

    override fun decryptStringData(data: String, salt: String, password: String, pbeIterations: Int): String {
        val secretKeys: Crypt.SecretKeys =
            Crypt.generateKeyFromPassword(password, salt, pbeIterations)
        return Crypt.decryptString(Crypt.CipherTextIvMac(data), secretKeys)
    }

    @Throws(GeneralSecurityException::class)
    private fun encrypt(data: ByteArray, keys: Crypt.SecretKeys): String {
        val encryptedData = Crypt.encrypt(data, keys)
        return encryptedData.toString()
    }

    companion object {
        private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+;:<>/?"
    }
}
