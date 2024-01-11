package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object TextComponentConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.CONSTRUCTOR or AccessFlags.PRIVATE,
    strings = listOf("TextComponent")
)