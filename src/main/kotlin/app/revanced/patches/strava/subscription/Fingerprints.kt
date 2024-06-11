package app.revanced.patches.strava.subscription

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint.methodFingerprint

internal val getSubscribedFingerprint = methodFingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { methodDef, classDef ->
        classDef.endsWith("/SubscriptionDetailResponse;") && methodDef.name == "getSubscribed"
    }
}
