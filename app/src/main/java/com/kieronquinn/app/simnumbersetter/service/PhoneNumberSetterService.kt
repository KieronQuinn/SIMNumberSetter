package com.kieronquinn.app.simnumbersetter.service

import android.os.Message
import com.android.internal.telephony.Phone
import com.android.internal.telephony.PhoneFactory
import com.kieronquinn.app.simnumbersetter.IPhoneNumberSetter

/**
 *  Proxy Service for calls to [Phone] methods to be called via the [IPhoneNumberSetter] service.
 */
class PhoneNumberSetterService: IPhoneNumberSetter.Stub() {

    private val phone: Phone by lazy {
        PhoneFactory.getDefaultPhone()
    }

    override fun setLine1Number(number: String, onComplete: Message?): Boolean {
        var tag = phone.line1AlphaTag
        if(tag.isNullOrEmpty()){
            tag = "Voice Line 1"
        }
        return phone.setLine1Number(tag, number, onComplete)
    }

    override fun getLine1Number(): String {
        return phone.line1Number
    }

}