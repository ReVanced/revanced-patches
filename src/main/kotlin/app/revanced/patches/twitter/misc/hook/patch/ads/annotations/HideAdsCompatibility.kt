package app.revanced.patches.twitter.misc.hook.patch.ads.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.twitter.android"
    )]
)
@Target(AnnotationTarget.CLASS)
internal annotation class HideAdsCompatibility