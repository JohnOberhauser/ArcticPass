package com.ober.arctic.util.security

import android.content.Context
import com.ober.arctic.data.model.EncryptedDataHolder
import com.ober.arcticpass.BuildConfig
import com.tozny.crypto.android.AesCbcWithIntegrity
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.SecretKey

interface Encryption {
    fun encryptString(data: String?, password: String): String?
    fun decryptString(data: String, password: String): String?
    fun encrypt(data: ByteArray, password: String): String
    fun decrypt(encryptedDataString: String, password: String): ByteArray?
    fun generateRandomKey(length: Int): String
    fun encryptStringData(data: String, password: String): EncryptedDataHolder
    fun decryptStringData(data: String, salt: String, password: String): String
    fun getStoredSecretKeys(password: String): AesCbcWithIntegrity.SecretKeys?
}

class EncryptionImpl(private val context: Context) : Encryption {

    override fun generateRandomKey(length: Int): String {
        AesCbcWithIntegrity.generateIv() // calling this just to fix prng
        val keyBuilder = StringBuilder()
        val secureRandom = SecureRandom()
        for (i in 1..length) {
            keyBuilder.append(CHARACTERS[secureRandom.nextInt(CHARACTERS.length)])
        }
        return keyBuilder.toString()
    }

    override fun encryptStringData(data: String, password: String): EncryptedDataHolder {
        val salt: String = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt())
        val secretKeys: AesCbcWithIntegrity.SecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(password, salt, iterationCount)
        return EncryptedDataHolder(salt, encrypt(data.toByteArray(), secretKeys))
    }

    override fun decryptStringData(data: String, salt: String, password: String): String {
        val secretKeys: AesCbcWithIntegrity.SecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(password, salt, iterationCount)
        return AesCbcWithIntegrity.decryptString(AesCbcWithIntegrity.CipherTextIvMac(data), secretKeys)
    }

    override fun encryptString(data: String?, password: String): String? {
        var result = ""
        return try {
            if (data != null) {
                result = encrypt(data.toByteArray(), password)
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    override fun decryptString(data: String, password: String): String? {
        try {
            val keys = getStoredSecretKeys(password) ?: return null
            val d = AesCbcWithIntegrity.CipherTextIvMac(data)
            return AesCbcWithIntegrity.decryptString(d, keys)
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    override fun encrypt(data: ByteArray, password: String): String {
        return encrypt(data, getSecretKeys(password))
    }

    @Throws(GeneralSecurityException::class)
    private fun encrypt(data: ByteArray, keys: AesCbcWithIntegrity.SecretKeys): String {
        val encryptedData = AesCbcWithIntegrity.encrypt(data, keys)
        return encryptedData.toString()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    override fun decrypt(encryptedDataString: String, password: String): ByteArray? {
        val keys = getStoredSecretKeys(password) ?: return null
        return decrypt(encryptedDataString, keys)
    }

    @Throws(GeneralSecurityException::class)
    private fun decrypt(
        encryptedDataString: String,
        keys: AesCbcWithIntegrity.SecretKeys
    ): ByteArray {
        val encryptedData = AesCbcWithIntegrity.CipherTextIvMac(encryptedDataString)
        return AesCbcWithIntegrity.decrypt(encryptedData, keys)
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun getSecretKeys(password: String): AesCbcWithIntegrity.SecretKeys {
        var keys = getStoredSecretKeys(password)
        if (keys == null) {
            keys = createSecretKeys(password)
        }
        return keys
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun createSecretKeys(password: String): AesCbcWithIntegrity.SecretKeys {
        val ks = getKeyStore()
        val keys = AesCbcWithIntegrity.generateKey()
        val secretKeyEntry = KeyStore.SecretKeyEntry(keys!!.confidentialityKey)
        val integrityKeyEntry = KeyStore.SecretKeyEntry(keys.integrityKey)
        val passwordProtection = KeyStore.PasswordProtection(password.toCharArray())
        ks.setEntry(KEYSTORE_SECRET_KEY, secretKeyEntry, passwordProtection)
        ks.setEntry(KEYSTORE_INTEGRITY_KEY, integrityKeyEntry, passwordProtection)
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(KEYSTORE_FILENAME, Context.MODE_PRIVATE)
            ks.store(fos, null)
        } catch (e: FileNotFoundException) {
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: Exception) {
                }

            }
        }
        return keys
    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        UnrecoverableKeyException::class
    )
    override fun getStoredSecretKeys(password: String): AesCbcWithIntegrity.SecretKeys? {
        return try {
            val ks = getKeyStore()
            val secretKey = ks.getKey(KEYSTORE_SECRET_KEY, password.toCharArray()) as SecretKey?
            val integrityKey = ks.getKey(KEYSTORE_INTEGRITY_KEY, password.toCharArray()) as SecretKey?
            if (secretKey != null && integrityKey != null) {
                AesCbcWithIntegrity.SecretKeys(secretKey, integrityKey)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class
    )
    private fun getKeyStore(): KeyStore {
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        var fis: FileInputStream? = null
        try {
            fis = context.openFileInput(KEYSTORE_FILENAME)
            ks.load(fis, null)
        } catch (e: FileNotFoundException) {
            ks.load(null)
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: Exception) {
                }

            }
        }
        return ks
    }

    companion object {
        private const val KEYSTORE_SECRET_KEY = BuildConfig.APPLICATION_ID + ":secret"
        private const val KEYSTORE_INTEGRITY_KEY = BuildConfig.APPLICATION_ID + ":integrity"
        private const val KEYSTORE_FILENAME = BuildConfig.APPLICATION_ID + ":file"
        private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[{]}\\;:<>/?"
        const val iterationCount = 100000
    }
}
