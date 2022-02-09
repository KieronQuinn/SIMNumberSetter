package com.kieronquinn.app.simnumbersetter;

import android.os.Message;

interface IPhoneNumberSetter {

    boolean setLine1Number(String number, in Message onComplete);
    String getLine1Number();

}