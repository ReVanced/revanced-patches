package app.revanced.patches.youtube.ad.video.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.14.35", "17.17.34", "17.19.36", "17.20.37", "17.22.36", "17.23.35", "17.23.36", "17.24.34", "17.24.35", "17.25.34", "17.26.35", "17.27.39", "17.28.34")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class VideoAdsCompatibility

