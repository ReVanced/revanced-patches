package app.revanced.patches.instagram.patches.ads.profile.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object HideProfileAdsFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("L", "L", "I"),
    opcodes = listOf(
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.IF_LTZ,
    ),
)
