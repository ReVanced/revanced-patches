package app.revanced.patches.youtube.misc.videoid.annotation

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.14.35", "17.22.36", "17.26.35")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class VideoIdCompatibility
