package app.revanced.patches.twitter.layout.hideviews.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.twitter.android", arrayOf("9.69.1-release.0", "9.71.0-release.0")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class HideViewsCompatibility