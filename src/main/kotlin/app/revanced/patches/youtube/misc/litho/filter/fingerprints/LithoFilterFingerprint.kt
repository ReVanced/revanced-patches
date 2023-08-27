package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object LithoFilterFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.CONSTRUCTOR,
    returnType = "V",
    customFingerprint = { _, classDef ->
        classDef.type.endsWith("LithoFilterPatch;")
    }
)