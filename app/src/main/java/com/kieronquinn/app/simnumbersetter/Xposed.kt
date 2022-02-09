package com.kieronquinn.app.simnumbersetter

import android.content.Intent
import com.kieronquinn.app.simnumbersetter.service.PhoneNumberSetterService
import com.kieronquinn.app.simnumbersetter.utils.extensions.checkSecurity
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 *  Xposed hook to allow binding to TelephonyDebugService (an exported, but protected service in
 *  com.android.phone), returning [IPhoneNumberSetter]. This can then be accessed via the UI
 *  and used to query and set the number.
 */
class Xposed: IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if(lpparam.packageName != "com.android.phone") return
        XposedHelpers.findAndHookMethod(
            "com.android.phone.TelephonyDebugService",
            lpparam.classLoader,
            "onBind",
            Intent::class.java,
            object: XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                    param.onBind(param.args[0] as Intent)
                }
            }
        )
    }

    private fun XC_MethodHook.MethodHookParam.onBind(intent: Intent) {
        if(intent.checkSecurity()){
            result = PhoneNumberSetterService()
        }
    }

}