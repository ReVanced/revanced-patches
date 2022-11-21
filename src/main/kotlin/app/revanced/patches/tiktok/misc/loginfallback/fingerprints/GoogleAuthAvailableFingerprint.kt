package app.revanced.patches.tiktok.misc.loginfallback.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.loginfallback.annotations.TikTokWebLoginCompatibility
import org.jf.dexlib2.AccessFlags

@Name("google-one-tap-auth-available-fingerprint")
@TikTokWebLoginCompatibility
@Version("0.0.1")
object GoogleAuthAvailableFingerprint : MethodFingerprint(
    returnType = "Z",
    access = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/bytedance/lobby/google/GoogleAuth;"
    }
)