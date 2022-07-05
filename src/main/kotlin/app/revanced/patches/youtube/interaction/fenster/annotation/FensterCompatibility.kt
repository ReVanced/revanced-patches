package app.revanced.patches.youtube.interaction.fenster.annotation

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

//TODO the patch may be compatible with more versions, but this is the one i'm testing on right now...
@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.24.34")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class FensterCompatibility