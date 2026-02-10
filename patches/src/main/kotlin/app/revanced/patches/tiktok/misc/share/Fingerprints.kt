package app.revanced.patches.tiktok.misc.share

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val urlShorteningFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters(
        "L",
        "Ljava/lang/String;",
        "Ljava/util/List;",
        "Ljava/lang/String;",
        "Z",
        "I"
    )
    opcodes(Opcode.RETURN_VOID)

    // Same Kotlin intrinsics literal on both variants.
    strings("share_link_id", "invitation_scene")

    custom { method, _ ->
        method.parameterTypes.size == 6
    }
}
