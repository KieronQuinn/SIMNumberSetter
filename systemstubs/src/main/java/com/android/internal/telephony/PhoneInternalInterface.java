package com.android.internal.telephony;

import android.os.Message;

public interface PhoneInternalInterface {

    /**
     * Returns the alpha tag associated with the msisdn number.
     * If there is no alpha tag associated or the record is not yet available,
     * returns a default localized string. <p>
     */
    String getLine1AlphaTag();

    /**
     * Sets the MSISDN phone number in the SIM card.
     *
     * @param alphaTag the alpha tag associated with the MSISDN phone number
     *        (see getMsisdnAlphaTag)
     * @param number the new MSISDN phone number to be set on the SIM.
     * @param onComplete a callback message when the action is completed.
     *
     * @return true if req is sent, false otherwise. If req is not sent there will be no response,
     * that is, onComplete will never be sent.
     */
    boolean setLine1Number(String alphaTag, String number, Message onComplete);

    /**
     * Get the line 1 phone number (MSISDN). For CDMA phones, the MDN is returned
     * and {#getMsisdn()} will return the MSISDN on CDMA/LTE phones.<p>
     *
     * @return phone number. May return null if not
     * available or the SIM is not ready
     */
    String getLine1Number();

}
