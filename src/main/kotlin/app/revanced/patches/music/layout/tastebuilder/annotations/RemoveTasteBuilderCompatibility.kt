package app.revanced.patches.music.layout.tastebuilder.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.apps.youtube.music", arrayOf("5.03.50")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RemoveTasteBuilderCompatibility

