package com.ober.arctic.util.security

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.grandcentrix.tray.AppPreferences
import java.io.IOException
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.KeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException

interface FingerprintManager {

    fun authenticateAndSetUnlockKey2(
        context: Context,
        fragment: Fragment,
        fingerprintAuthenticatedCallback: FingerprintAuthenticatedCallback
    )

    fun enableFingerprint2(
        context: Context,
        fingerprintEnabledCallback: FingerprintEnabledCallback? = null
    )

    fun disableFingerprint()
    fun isFingerprintEnabled(): Boolean
    fun isBiometricsAvailable(context: Context): Boolean
}

class FingerprintManagerImpl(
    private var appPreferences: AppPreferences,
    private var keyManager: KeyManager
) : FingerprintManager {

    private var fingerprintEnabled: Boolean? = null

    override fun authenticateAndSetUnlockKey2(
        context: Context,
        fragment: Fragment,
        fingerprintAuthenticatedCallback: FingerprintAuthenticatedCallback
    ) {
        val biometricPrompt = BiometricPrompt(fragment, ContextCompat.getMainExecutor(context), object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                fingerprintAuthenticatedCallback.onInvalid()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                result.cryptoObject?.cipher?.let { cipher ->
                    appPreferences.getString(ENCRYPTED_UNLOCK_KEY)?.let { encryptedUnlockKey ->
                        val bytes: ByteArray = cipher.doFinal(Base64.decode(encryptedUnlockKey, Base64.DEFAULT))
                        val decrypted = String(bytes, Charset.forName("UTF-8"))
                        keyManager.unlockKey = decrypted
                        fingerprintAuthenticatedCallback.onSuccess()
                    }
                }

            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                fingerprintAuthenticatedCallback.onInvalid()
            }
        })

        val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("test")
            .setSubtitle("test")
            .setNegativeButtonText("test")
            .setConfirmationRequired(false)
            .build()


        val cryptoObject = BiometricPrompt.CryptoObject(getCipherForDecryption())
        biometricPrompt.authenticate(biometricPromptInfo, cryptoObject)
    }

    private fun getCipherForDecryption(): Cipher {
        val cipher: Cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA + "/"
                    + KeyProperties.BLOCK_MODE_ECB + "/"
                    + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
        )

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)

        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(keyStore, KEY_NAME))
        return cipher
    }

    private fun getPrivateKey(keyStore: KeyStore, keyAlias: String): PrivateKey? {
        return keyStore.getKey(keyAlias, null) as PrivateKey
    }

    @SuppressLint("CheckResult")
    override fun enableFingerprint2(
        context: Context,
        fingerprintEnabledCallback: FingerprintEnabledCallback?
    ) {
        GlobalScope.launch {
            keyManager.unlockKey?.let { unlockKey ->
                val cipher: Cipher = cipherForEncryption()
                val encryptedBytes = cipher.doFinal(unlockKey.toByteArray(charset("UTF-8")))
                val encryptedString: String = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

                appPreferences.put(ENCRYPTED_UNLOCK_KEY, encryptedString)
                appPreferences.put(FINGERPRINT_ENABLED, true)
                fingerprintEnabled = true
            } ?: run {
                fingerprintEnabledCallback?.onFailure()
            }
        }
    }

    @Throws(GeneralSecurityException::class)
    private fun getPublicKey(
        keyFactory: KeyFactory,
        keyStore: KeyStore
    ): PublicKey? {
        val publicKey = keyStore.getCertificate(KEY_NAME).publicKey
        val spec: KeySpec = X509EncodedKeySpec(publicKey.encoded)
        return keyFactory.generatePublic(spec)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun getKeyGenParameterSpecBuilder(
        blockModes: String?,
        encryptionPaddings: String?
    ): KeyGenParameterSpec.Builder {
        val builder = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(blockModes)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(encryptionPaddings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }
        return builder
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Throws(GeneralSecurityException::class, IOException::class)
    fun cipherForEncryption(): Cipher {
        val keyGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE)
        keyGenerator.initialize(
            getKeyGenParameterSpecBuilder(
                KeyProperties.BLOCK_MODE_ECB,
                KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
            ).build()
        )
        keyGenerator.generateKeyPair()
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val cipher: Cipher = createCipher()

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)

        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(keyFactory, keyStore))
        return cipher
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
    fun createCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA + "/"
                    + KeyProperties.BLOCK_MODE_ECB + "/"
                    + KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
        )
    }

    override fun disableFingerprint() {
        appPreferences.put(FINGERPRINT_ENABLED, false)
        fingerprintEnabled = false
        appPreferences.put(ENCRYPTED_UNLOCK_KEY, null)
    }

    override fun isFingerprintEnabled(): Boolean {
        fingerprintEnabled?.let {
            return it
        }
        fingerprintEnabled = appPreferences.getBoolean(FINGERPRINT_ENABLED, false)
        return fingerprintEnabled ?: false
    }

    override fun isBiometricsAvailable(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    fun keyExists(keyName: String, keyStore: KeyStore): Boolean {

        val aliases = keyStore.aliases()

        while (aliases.hasMoreElements()) {
            if (keyName == aliases.nextElement()) {
                return true
            }
        }

        return false
    }

    companion object {
        const val ENCRYPTED_UNLOCK_KEY = "encrypted_data_unlock_key"
        const val FINGERPRINT_ENABLED = "fingerprint_enabled"
        const val KEY_NAME = "arctic_pass_fingerprint_key"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}

interface FingerprintAuthenticatedCallback {
    fun onSuccess()
    fun onInvalid()
}

interface FingerprintEnabledCallback {
    fun onFailure()
}