package com.ober.arctic.util.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.*
import java.security.spec.KeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object BiometricEncryption {

    private const val KEY_NAME = "arctic_pass_fingerprint_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

    fun getCipherForDecryption(): Cipher {
        val cipher: Cipher = createCipher()
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        return cipher
    }

    fun cipherForEncryption(): Cipher {
        val keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE)
        keyGenerator.initialize(getKeyGenParameterSpec())
        keyGenerator.generateKeyPair()
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val cipher: Cipher = createCipher()

        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(keyFactory))
        return cipher
    }

    private fun getKeyGenParameterSpec(): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }
        return builder.build()
    }

    private fun createCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA + "/"
                    + KeyProperties.BLOCK_MODE_ECB + "/"
                    + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
        )
    }

    private fun getPrivateKey(): PrivateKey? {
        return getKeyStore().getKey(KEY_NAME, null) as PrivateKey
    }

    private fun getPublicKey(
        keyFactory: KeyFactory
    ): PublicKey? {
        val publicKey = getKeyStore().getCertificate(KEY_NAME).publicKey
        val spec: KeySpec = X509EncodedKeySpec(publicKey.encoded)
        return keyFactory.generatePublic(spec)
    }

    private fun getKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore
    }
}