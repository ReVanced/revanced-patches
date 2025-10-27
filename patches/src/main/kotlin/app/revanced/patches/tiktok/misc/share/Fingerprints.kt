package app.revanced.patches.tiktok.misc.share

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val urlShorteningFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("LX/")
    parameters(
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;"
    )
    opcodes(Opcode.RETURN_OBJECT)

    // Same Kotlin intrinsics literal on both variants.
    strings("getShortShareUrlObservab\u2026ongUrl, subBizSceneValue)")

    custom { method, _ ->
        // LIZLLL is obfuscated by ProGuard/R8, but stable across both TikTok and Musically.
        method.name == "LIZLLL"
    }
}
