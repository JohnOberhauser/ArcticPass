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
        appPreferences.getString(ENCRYPTED_DATA)?.let { encryptedString ->
            disposable = RxFingerprint.decrypt(EncryptionMethod.RSA, context, ENCRYPTED_DATA, encryptedString)
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
                    if (it is KeyPermanentlyInvalidatedException) {
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
                RxFingerprint.encrypt(EncryptionMethod.RSA, context, ENCRYPTED_DATA, unlockKey)
                    .subscribe({
                        when (it.result) {
                            FingerprintResult.AUTHENTICATED -> {
                                appPreferences.put(ENCRYPTED_DATA, it.encrypted)
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
        appPreferences.put(ENCRYPTED_DATA, null)
    }

    override fun isFingerprintEnabled(): Boolean {
        fingerprintEnabled?.let {
            return it
        }
        fingerprintEnabled = appPreferences.getBoolean(FINGERPRINT_ENABLED, false)
        return fingerprintEnabled ?: false
    }

    companion object {
        const val ENCRYPTED_DATA = "encrypted_data_unlock_key"
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