package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints


import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object TextComponentConstructorFingerprint : MethodFingerprint(
    access = AccessFlags.CONSTRUCTOR or AccessFlags.PRIVATE,
    strings = listOf("TextComponent")
)