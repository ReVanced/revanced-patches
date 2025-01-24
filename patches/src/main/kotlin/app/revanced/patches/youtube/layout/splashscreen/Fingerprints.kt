package app.revanced.patches.youtube.layout.splashscreen

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val startUpResourceIdParentFingerprint = fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.DECLARED_SYNCHRONIZED)
    parameters("I", "I")
    strings("early type", "final type")
}

/**
 * Resolves using class found in [startUpResourceIdParentFingerprint].
 */
internal val startUpResourceIdFingerprint = fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("I")
    custom { method, _ ->
        method.containsLiteralInstruction(3) && method.containsLiteralInstruction(4)
    }
}
