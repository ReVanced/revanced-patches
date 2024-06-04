package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val deletedMessageClickableSpanCtorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("DeletedMessageClickableSpan;")
    }
}
