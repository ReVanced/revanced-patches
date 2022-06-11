package app.revanced.patches.youtube.layout.oldqualitylayout.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.17.34", "17.19.36", "17.20.37", "17.22.36")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class OldQualityLayoutCompatibility