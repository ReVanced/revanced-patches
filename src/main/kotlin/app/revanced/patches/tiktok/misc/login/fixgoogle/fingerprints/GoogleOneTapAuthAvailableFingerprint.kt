package app.revanced.patches.tiktok.misc.login.fixgoogle.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object GoogleOneTapAuthAvailableFingerprint : MethodFingerprint(
    returnType = "Z",
    access = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/bytedance/lobby/google/GoogleOneTapAuth;"
    }
)