package app.revanced.patches.nfctoolsse.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object IsLicenseRegisteredFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    strings = listOf("kLicenseCheck")
)