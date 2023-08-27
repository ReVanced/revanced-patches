package app.revanced.patches.youtube.layout.hide.watermark.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object HideWatermarkFingerprint : MethodFingerprint (
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L", "L")
)