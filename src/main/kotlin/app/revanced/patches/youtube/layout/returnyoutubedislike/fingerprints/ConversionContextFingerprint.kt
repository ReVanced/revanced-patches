package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ConversionContextFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf(
        ", widthConstraint=",
        ", heightConstraint=",
        ", templateLoggerFactory=",
        ", rootDisposableContainer=",
        // 18.37.36 and after this String is: ConversionContext{containerInternal=
        // and before it is: ConversionContext{container=
        // Use a partial string to match both.
        "ConversionContext{container"
    )
)