package com.ober.arctic.util.security

import android.content.Context
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ober.arcticpass.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.grandcentrix.tray.AppPreferences
import java.nio.charset.Charset
import javax.crypto.Cipher

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
            .setTitle(context.getString(R.string.scan_biometrics_to_log_in))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .setConfirmationRequired(true)
            .build()

        val cryptoObject = BiometricPrompt.CryptoObject(BiometricEncryption.getCipherForDecryption())
        biometricPrompt.authenticate(biometricPromptInfo, cryptoObject)
    }

    override fun enableFingerprint2(
        context: Context,
        fingerprintEnabledCallback: FingerprintEnabledCallback?
    ) {
        GlobalScope.launch {
            keyManager.unlockKey?.let { unlockKey ->
                val cipher: Cipher = BiometricEncryption.cipherForEncryption()
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

    companion object {
        const val ENCRYPTED_UNLOCK_KEY = "encrypted_data_unlock_key"
        const val FINGERPRINT_ENABLED = "fingerprint_enabled"
    }
}

interface FingerprintAuthenticatedCallback {
    fun onSuccess()
    fun onInvalid()
}

interface FingerprintEnabledCallback {
    fun onFailure()
}