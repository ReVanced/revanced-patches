package app.revanced.patches.youtube.misc.litho.filter.annotation

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.36.37", "17.41.37", "17.42.35", "17.43.36")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class LithoFilterCompatibility

