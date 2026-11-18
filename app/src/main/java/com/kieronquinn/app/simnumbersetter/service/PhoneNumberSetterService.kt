package com.kieronquinn.app.simnumbersetter.service

import android.os.AsyncResult
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.telephony.SubscriptionManager
import com.android.internal.telephony.Phone
import com.android.internal.telephony.PhoneFactory
import com.kieronquinn.app.simnumbersetter.IPhoneNumberSetter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Writes the MSISDN to EF_MSISDN through the active UICC IccRecords implementation.
 * Android 16/LineageOS 23.2 no longer exposes Phone.setLine1Number(); IccRecords keeps
 * the actual SIM-record write API.
 */
class PhoneNumberSetterService : IPhoneNumberSetter.Stub() {

    override fun setLine1Number(number: String, onComplete: Message?): Boolean {
        if (number.length > MAX_NUMBER_LENGTH || number.any { !(it.isDigit() || it == '+') }) {
            return false
        }
        val phone = getTargetPhone() ?: return false
        val records = phone.iccRecords ?: return false
        val alphaTag = records.msisdnAlphaTag?.takeIf { it.isNotBlank() } ?: DEFAULT_ALPHA_TAG

        val completed = CountDownLatch(1)
        val successful = AtomicBoolean(false)
        val callbackThread = HandlerThread("SIMNumberSetter-Callback").apply { start() }

        return try {
            val callback = Handler(callbackThread.looper) { message ->
                val result = message.obj as? AsyncResult
                // AsyncResult is supplied by the telephony framework. A missing result is
                // treated as failure; it must never be interpreted as a successful SIM write.
                successful.set(result != null && result.exception == null)
                completed.countDown()
                true
            }
            records.setMsisdnNumber(alphaTag, number, callback.obtainMessage(MSG_SET_MSISDN))
            completed.await(SET_TIMEOUT_SECONDS, TimeUnit.SECONDS) && successful.get()
        } catch (_: Throwable) {
            false
        } finally {
            callbackThread.quitSafely()
        }
    }

    override fun getLine1Number(): String {
        return try {
            getTargetPhone()?.iccRecords?.msisdnNumber.orEmpty()
        } catch (_: Throwable) {
            ""
        }
    }

    /** Prefer the system's selected default voice SIM; fall back to slot 0. */
    private fun getTargetPhone(): Phone? {
        val subscriptionId = SubscriptionManager.getDefaultVoiceSubscriptionId()
        if (SubscriptionManager.isValidSubscriptionId(subscriptionId)) {
            val phoneId = SubscriptionManager.getSlotIndex(subscriptionId)
            if (phoneId >= 0) {
                PhoneFactory.getPhone(phoneId)?.let { return it }
            }
        }
        return PhoneFactory.getDefaultPhone()
    }

    private companion object {
        const val DEFAULT_ALPHA_TAG = "Voice Line 1"
        const val MSG_SET_MSISDN = 1
        const val SET_TIMEOUT_SECONDS = 30L
        const val MAX_NUMBER_LENGTH = 20
    }
}
