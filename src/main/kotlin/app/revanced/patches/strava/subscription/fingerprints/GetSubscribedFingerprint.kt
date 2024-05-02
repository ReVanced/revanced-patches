package app.revanced.patches.strava.subscription.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val getSubscribedFingerprint = methodFingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { methodDef, classDef ->
        classDef.type.endsWith("/SubscriptionDetailResponse;") && methodDef.name == "getSubscribed"
    }
}
