package app.revanced.patches.finanzonline.detection.bootloader.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object BootStateFingerprint : MethodFingerprint(
    "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    strings = listOf("Boot state of device: %s"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/AttestationHelper;")
    }
)
