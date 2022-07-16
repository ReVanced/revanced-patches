package app.revanced.patches.youtube.layout.returnyoutubedislikes.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.14.35", "17.26.35")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RYDCompatibility