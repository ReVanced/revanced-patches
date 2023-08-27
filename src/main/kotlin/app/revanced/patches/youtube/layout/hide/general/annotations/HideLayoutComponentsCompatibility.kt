package app.revanced.patches.youtube.layout.hide.general.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.youtube", arrayOf("18.32.39"))])
@Target(AnnotationTarget.CLASS)
internal annotation class HideLayoutComponentsCompatibility

