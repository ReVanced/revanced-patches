package app.revanced.patches.hexeditor.ad.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.myprog.hexedit")
    ]
)
@Target(AnnotationTarget.CLASS)
internal annotation class HexEditorAdsCompatibility
