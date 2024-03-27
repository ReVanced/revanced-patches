package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PivotBarConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf("com.google.android.apps.youtube.app.endpoint.flags")
)