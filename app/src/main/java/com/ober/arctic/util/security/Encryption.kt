package com.ober.arctic.util.security

import android.content.Context
import android.util.Base64
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
    fun encryptString(data: String?): String?
    fun decryptString(data: String): String?
    fun encrypt(data: ByteArray): String
    fun decrypt(encryptedDataString: String): ByteArray?
    fun generateRandomKey(): String
    fun encryptString(data: String, password: String): EncryptedDataHolder
    fun decryptString(data: String, salt: String, password: String): String
}

class EncryptionImpl(private val context: Context) : Encryption {

    override fun generateRandomKey(): String {
        return Base64.encodeToString(AesCbcWithIntegrity.generateIv(), Base64.DEFAULT)
    }

    override fun encryptString(data: String, password: String): EncryptedDataHolder {
        val salt: String = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt())
        val secretKeys: AesCbcWithIntegrity.SecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(password, salt)
        return EncryptedDataHolder(salt, encrypt(data.toByteArray(), secretKeys))
    }

    override fun decryptString(data: String, salt: String, password: String): String {
        val secretKeys: AesCbcWithIntegrity.SecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(password, salt)
        return AesCbcWithIntegrity.decryptString(AesCbcWithIntegrity.CipherTextIvMac(data), secretKeys)
    }

    override fun encryptString(data: String?): String? {
        var result = ""
        return try {
            if (data != null) {
                result = encrypt(data.toByteArray())
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    override fun decryptString(data: String): String? {
        try {
            val keys = getStoredSecretKeys() ?: return null
            val d = AesCbcWithIntegrity.CipherTextIvMac(data)
            return AesCbcWithIntegrity.decryptString(d, keys)
        } catch (e: Exception) {
            return null
        }
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    override fun encrypt(data: ByteArray): String {
        return encrypt(data, getSecretKeys())
    }

    @Throws(GeneralSecurityException::class)
    private fun encrypt(data: ByteArray, keys: AesCbcWithIntegrity.SecretKeys): String {
        val encryptedData = AesCbcWithIntegrity.encrypt(data, keys)
        return encryptedData.toString()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    override fun decrypt(encryptedDataString: String): ByteArray? {
        val keys = getStoredSecretKeys() ?: return null
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
    private fun getSecretKeys(): AesCbcWithIntegrity.SecretKeys {
        var keys = getStoredSecretKeys()
        if (keys == null) {
            val ks = getKeyStore()
            keys = AesCbcWithIntegrity.generateKey()
            val secretKeyEntry = KeyStore.SecretKeyEntry(keys!!.confidentialityKey)
            val integrityKeyEntry = KeyStore.SecretKeyEntry(keys.integrityKey)
            ks.setEntry(KEYSTORE_SECRET_KEY, secretKeyEntry, null)
            ks.setEntry(KEYSTORE_INTEGRITY_KEY, integrityKeyEntry, null)
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
    private fun getStoredSecretKeys(): AesCbcWithIntegrity.SecretKeys? {
        val ks = getKeyStore()
        val secretKey = ks.getKey(KEYSTORE_SECRET_KEY, null) as SecretKey?
        val integrityKey = ks.getKey(KEYSTORE_INTEGRITY_KEY, null) as SecretKey?
        return if (secretKey != null && integrityKey != null) {
            AesCbcWithIntegrity.SecretKeys(secretKey, integrityKey)
        } else {
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
    }
}
