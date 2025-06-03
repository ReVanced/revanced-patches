package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val colorSpaceUtilsClassFingerprint by fingerprint {
    strings("The specified color must be encoded in an RGB color space.") // Partial string match.
}

internal val convertArgbToRgbaFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("J")
    parameters("J")
}

internal val parseLottieJsonFingerprint by fingerprint {
    strings("Unsupported matte type: ")
}

internal val parseAnimatedColorFingerprint by fingerprint {
    parameters("L", "F")
    returns("Ljava/lang/Object;")
    custom { method, _ ->
        method.containsLiteralInstruction(255.0) &&
                method.containsLiteralInstruction(1.0)
    }
}
