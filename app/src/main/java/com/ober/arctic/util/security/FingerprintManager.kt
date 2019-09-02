package com.ober.arctic.util.security

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.core.os.CancellationSignal
import com.mtramin.rxfingerprint.EncryptionMethod
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintResult
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.grandcentrix.tray.AppPreferences
import java.io.IOException
import java.lang.Exception
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException

interface FingerprintManager {
    fun authenticateAndSetUnlockKey(
        context: Context,
        cancellationSignal: CancellationSignal,
        fingerprintAuthenticatedCallback: FingerprintAuthenticatedCallback
    )

    fun enableFingerprint(
        context: Context,
        fingerprintEnabledCallback: FingerprintEnabledCallback? = null
    )

    fun disableFingerprint()
    fun isFingerprintEnabled(): Boolean
}

class FingerprintManagerImpl(
    private var appPreferences: AppPreferences,
    private var keyManager: KeyManager
) : FingerprintManager {

    private var fingerprintEnabled: Boolean? = null

    override fun authenticateAndSetUnlockKey(
        context: Context,
        cancellationSignal: CancellationSignal,
        fingerprintAuthenticatedCallback: FingerprintAuthenticatedCallback
    ) {
        var disposable: Disposable? = null
        appPreferences.getString(ENCRYPTED_UNLOCK_KEY)?.let { encryptedUnlockKey ->
            disposable = RxFingerprint.decrypt(EncryptionMethod.RSA, context, KEY_NAME, encryptedUnlockKey)
                .subscribe({
                    when (it.result) {
                        FingerprintResult.AUTHENTICATED -> {
                            keyManager.unlockKey = it.decrypted
                            fingerprintAuthenticatedCallback.onSuccess()
                        }
                        else -> {
                            // nothing for now
                        }
                    }
                }, {
                    if (it is KeyPermanentlyInvalidatedException || it is InvalidKeyException) {
                        deleteOldKey()
                        fingerprintAuthenticatedCallback.onInvalid()
                    }
                })
        } ?: run {
            fingerprintAuthenticatedCallback.onInvalid()
        }

        cancellationSignal.setOnCancelListener {
            disposable?.dispose()
        }
    }

    @SuppressLint("CheckResult")
    override fun enableFingerprint(
        context: Context,
        fingerprintEnabledCallback: FingerprintEnabledCallback?
    ) {
        GlobalScope.launch {
            keyManager.unlockKey?.let { unlockKey ->
                RxFingerprint.encrypt(EncryptionMethod.RSA, context, KEY_NAME, unlockKey)
                    .subscribe({
                        when (it.result) {
                            FingerprintResult.AUTHENTICATED -> {
                                appPreferences.put(ENCRYPTED_UNLOCK_KEY, it.encrypted)
                                appPreferences.put(FINGERPRINT_ENABLED, true)
                                fingerprintEnabled = true
                            }
                            else -> {
                                fingerprintEnabledCallback?.onFailure()
                            }
                        }
                    }, {
                        fingerprintEnabledCallback?.onFailure()
                    })
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

    private fun deleteOldKey() {  //TODO remove in future versions. only need because i f'd up and need to remove old key
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val keyName = "encrypted_data_unlock_key"
            if (keyExists(keyName, keyStore)) {
                keyStore.deleteEntry(keyName)
            }
        } catch (e: Exception) {
            // do nothing
        }
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
    }
}

interface FingerprintAuthenticatedCallback {
    fun onSuccess()
    fun onInvalid()
}

interface FingerprintEnabledCallback {
    fun onFailure()
}