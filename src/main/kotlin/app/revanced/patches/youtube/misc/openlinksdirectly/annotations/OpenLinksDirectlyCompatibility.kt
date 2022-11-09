package app.revanced.patches.youtube.misc.openlinksdirectly.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.34.36", "17.36.39", "17.38.36", "17.39.35" , "17.40.41", "17.42.35", "17.43.36")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class OpenLinksDirectlyCompatibility
