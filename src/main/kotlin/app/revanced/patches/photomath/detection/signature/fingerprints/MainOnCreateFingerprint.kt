package app.revanced.patches.photomath.detection.signature.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object MainOnCreateFingerprint : MethodFingerprint(
    returnType = "V",
    access = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/microblink/photomath/PhotoMath;" && methodDef.name == "onCreate"
    }
)