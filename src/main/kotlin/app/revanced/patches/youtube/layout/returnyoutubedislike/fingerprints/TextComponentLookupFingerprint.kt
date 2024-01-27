package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves against the same class that [TextComponentConstructorFingerprint] resolves to.
 */
internal object TextComponentLookupFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = listOf("L"),
    strings = listOf("…")
)