package app.revanced.patches.tiktok.feedfilter.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object FeedApiServiceLIZFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.SYNTHETIC,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/FeedApiService;") && methodDef.name == "LIZ"
    }
)