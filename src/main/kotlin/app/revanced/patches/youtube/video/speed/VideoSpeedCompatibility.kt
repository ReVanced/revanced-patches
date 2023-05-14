package app.revanced.patches.youtube.video.speed

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package("com.google.android.youtube", arrayOf("18.08.37", "18.15.40", "18.16.37"))]
)
@Target(AnnotationTarget.CLASS)
internal annotation class VideoSpeedCompatibility
