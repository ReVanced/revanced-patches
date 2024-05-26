package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val conversionContextFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    parameters()
    strings(
        ", widthConstraint=",
        ", heightConstraint=",
        ", templateLoggerFactory=",
        ", rootDisposableContainer=",
        // 18.37.36 and after this String is: ConversionContext{containerInternal=
        // and before it is: ConversionContext{container=
        // Use a partial string to match both.
        "ConversionContext{container",
    )
}
