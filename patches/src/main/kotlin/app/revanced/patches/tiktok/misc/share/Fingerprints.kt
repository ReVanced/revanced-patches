package app.revanced.patches.tiktok.misc.share

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.urlShorteningMethod by gettingFirstMethodDeclaratively(
    "getShortShareUrlObservab\u2026ongUrl, subBizSceneValue)" // Same Kotlin intrinsics literal on both variants.
) {
    name("LIZLLL") // LIZLLL is obfuscated by ProGuard/R8, but stable across both TikTok and Musically.
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returnType("LX/")
    parameterTypes(
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;"
    )
    opcodes(Opcode.RETURN_OBJECT)
}