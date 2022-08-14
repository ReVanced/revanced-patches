package app.revanced.patches.youtube.layout.tabletminiplayer.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.26.35", "17.29.34")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class TabletMiniPlayerCompatibility
