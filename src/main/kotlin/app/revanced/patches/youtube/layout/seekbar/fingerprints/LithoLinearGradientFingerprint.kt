package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object LithoLinearGradientFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.STATIC.value,
    returnType = "Landroid/graphics/LinearGradient;",
    parameters = listOf("F", "F", "F", "F", "[I", "[F"),
)