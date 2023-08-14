package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints


import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object TextComponentConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.CONSTRUCTOR or AccessFlags.PRIVATE,
    strings = listOf("TextComponent")
)