package app.revanced.patches.youtube.layout.pivotbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object PivotBarConstructorFingerprint : MethodFingerprint(
    access = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf("com.google.android.apps.youtube.app.endpoint.flags")
)