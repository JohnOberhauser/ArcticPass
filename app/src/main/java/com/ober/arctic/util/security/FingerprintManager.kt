package com.ober.arctic.util.security

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.core.os.CancellationSignal
import com.mtramin.rxfingerprint.EncryptionMethod
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintResult
import io.reactivex.disposables.Disposable
import net.grandcentrix.tray.AppPreferences

interface FingerprintManager {
    fun authenticateAndDecrypt(
        context: Context,
        cancellationSignal: CancellationSignal,
        fingerprintDecryptCallback: FingerprintDecryptCallback
    )

    fun authenticateAndEncrypt(
        context: Context,
        plaintext: String,
        fingerprintEncryptCallback: FingerprintEncryptCallback? = null
    )
}

class FingerprintManagerImpl(
    private var appPreferences: AppPreferences
) : FingerprintManager {

    override fun authenticateAndDecrypt(
        context: Context,
        cancellationSignal: CancellationSignal,
        fingerprintDecryptCallback: FingerprintDecryptCallback
    ) {
        var disposable: Disposable? = null
        appPreferences.getString(ENCRYPTED_DATA)?.let { encryptedString ->
            disposable = RxFingerprint.decrypt(EncryptionMethod.RSA, context, ENCRYPTED_DATA, encryptedString)
                .subscribe({
                    when (it.result) {
                        FingerprintResult.AUTHENTICATED -> {
                            fingerprintDecryptCallback.onSuccess(it.decrypted)
                        }
                        else -> {
                            // nothing for now
                        }
                    }
                }, {
                    if (it is KeyPermanentlyInvalidatedException) {
                        fingerprintDecryptCallback.onInvalid()
                    }
                })
        } ?: run {
            // nothing for now
        }

        cancellationSignal.setOnCancelListener {
            disposable?.dispose()
        }
    }

    @SuppressLint("CheckResult")
    override fun authenticateAndEncrypt(
        context: Context,
        plaintext: String,
        fingerprintEncryptCallback: FingerprintEncryptCallback?
    ) {

        RxFingerprint.encrypt(EncryptionMethod.RSA, context, ENCRYPTED_DATA, plaintext)
            .subscribe({
                when (it.result) {
                    FingerprintResult.AUTHENTICATED -> {
                        appPreferences.put(ENCRYPTED_DATA, it.encrypted)
                        appPreferences.put(FINGERPRINT_ENABLED, true)
                    }
                    else -> {
                        fingerprintEncryptCallback?.onFailure()
                    }
                }
            }, {
                fingerprintEncryptCallback?.onFailure()
            })
    }

    companion object {
        const val ENCRYPTED_DATA = "encrypted_data_unlock_key"
        const val FINGERPRINT_ENABLED = "fingerprint_enabled"
    }
}

interface FingerprintDecryptCallback {
    fun onSuccess(data: String)
    fun onInvalid()
}

interface FingerprintEncryptCallback {
    fun onFailure()
}