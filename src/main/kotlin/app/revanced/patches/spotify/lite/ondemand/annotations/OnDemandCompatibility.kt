package app.revanced.patches.spotify.lite.ondemand.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package("com.spotify.lite")]
)
@Target(AnnotationTarget.CLASS)
internal annotation class OnDemandCompatibility