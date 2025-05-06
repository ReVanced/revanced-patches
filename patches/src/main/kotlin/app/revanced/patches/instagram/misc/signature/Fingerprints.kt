package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val isValidSignatureClassFingerprint = fingerprint {
    strings("The provider for uri '", "' is not trusted: ")
}

internal val isValidSignatureMethodFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("L", "Z")
    returns("Z")
}