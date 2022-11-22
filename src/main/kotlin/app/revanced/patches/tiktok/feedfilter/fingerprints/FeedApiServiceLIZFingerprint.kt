package app.revanced.patches.tiktok.feedfilter.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object FeedApiServiceLIZFingerprint : MethodFingerprint(
    access = AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.FINAL or AccessFlags.SYNTHETIC,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/FeedApiService;") && methodDef.name == "LIZ"
    }
)