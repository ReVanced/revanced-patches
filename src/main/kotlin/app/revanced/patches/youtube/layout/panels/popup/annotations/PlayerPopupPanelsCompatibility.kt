package app.revanced.patches.youtube.layout.panels.popup.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.youtube", arrayOf("18.16.37", "18.19.35", "18.20.39", "18.23.35", "18.29.38"))])
@Target(AnnotationTarget.CLASS)
internal annotation class PlayerPopupPanelsCompatibility
