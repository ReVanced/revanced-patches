package app.revanced.patches.strava.subscription

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val getSubscribedFingerprint = fingerprint {
    opcodes(Opcode.IGET_BOOLEAN)
    custom { method, classDef ->
        classDef.endsWith("/SubscriptionDetailResponse;") && method.name == "getSubscribed"
    }
}
