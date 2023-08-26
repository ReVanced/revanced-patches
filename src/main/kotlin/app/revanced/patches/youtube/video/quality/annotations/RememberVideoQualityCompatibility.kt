package app.revanced.patches.youtube.video.quality.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.youtube", arrayOf("18.19.35", "18.20.39", "18.23.35", "18.29.38", "18.32.39"))])
@Target(AnnotationTarget.CLASS)
internal annotation class RememberVideoQualityCompatibility
