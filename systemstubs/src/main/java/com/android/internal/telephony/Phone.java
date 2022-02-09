package com.android.internal.telephony;

import android.os.Message;

public class Phone implements PhoneInternalInterface {

    @Override
    public String getLine1AlphaTag() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public String getLine1Number() {
        throw new RuntimeException("Stub!");
    }

}
