package app.revanced.patches.tiktok.feedfilter.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.feedfilter.annotations.FeedFilterCompatibility
import org.jf.dexlib2.AccessFlags

@Name("feed-api-service-fingerprint")
@MatchingMethod(
    "Lcom/ss/android/ugc/aweme/feed/FeedApiService;",
    "LIZ",
)
@FeedFilterCompatibility
@Version("0.0.1")
object FeedApiServiceLIZFingerprint : MethodFingerprint(
    access = AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.FINAL or AccessFlags.SYNTHETIC,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/FeedApiService;") && methodDef.name == "LIZ"
    }
)