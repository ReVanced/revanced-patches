package app.revanced.patches.youtube.layout.buttons.player.hide.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf()
    )]
)
@Target(AnnotationTarget.CLASS)
internal annotation class HidePlayerButtonsCompatibility