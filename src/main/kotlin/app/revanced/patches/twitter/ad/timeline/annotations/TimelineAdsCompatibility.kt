package app.revanced.patches.twitter.ad.timeline.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.twitter.android", arrayOf("9.65.3-release.0")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class TimelineAdsCompatibility