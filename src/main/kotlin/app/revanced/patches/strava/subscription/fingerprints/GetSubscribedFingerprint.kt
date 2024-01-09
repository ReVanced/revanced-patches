package app.revanced.patches.strava.subscription.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object GetSubscribedFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.IGET_BOOLEAN),
    customFingerprint = { methodDef, classDef ->
        classDef.type.endsWith("/SubscriptionDetailResponse;") && methodDef.name == "getSubscribed"
    }
)
