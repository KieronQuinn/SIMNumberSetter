package com.kieronquinn.app.simnumbersetter

import android.content.Intent
import com.kieronquinn.app.simnumbersetter.service.PhoneNumberSetterService
import com.kieronquinn.app.simnumbersetter.utils.extensions.checkSecurity
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Vector-compatible legacy Xposed entry point. Vector intentionally preserves the legacy
 * Xposed API, so the hook does not need a Vector-specific SDK dependency.
 */
class Xposed : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != PHONE_PACKAGE) return

        try {
            XposedHelpers.findAndHookMethod(
                TELEPHONY_DEBUG_SERVICE,
                lpparam.classLoader,
                "onBind",
                Intent::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val intent = param.args.firstOrNull() as? Intent ?: return
                        if (intent.checkSecurity()) {
                            // Replace the platform's null binder only for our authenticated bind.
                            param.result = PhoneNumberSetterService()
                        }
                    }
                }
            )
            XposedBridge.log("SIM Number Setter: hooked $TELEPHONY_DEBUG_SERVICE")
        } catch (t: Throwable) {
            XposedBridge.log("SIM Number Setter: failed to hook $TELEPHONY_DEBUG_SERVICE: $t")
        }
    }

    private companion object {
        const val PHONE_PACKAGE = "com.android.phone"
        const val TELEPHONY_DEBUG_SERVICE = "com.android.phone.TelephonyDebugService"
    }
}
