package app.revanced.patches.youtube.misc.customplaybackspeed.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.24.34", "17.25.34", "17.26.35", "17.27.39", "17.28.34", "17.29.34", "17.32.35", "17.33.42", "17.34.35", "17.34.36", "17.36.37")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class CustomPlaybackSpeedCompatibility
