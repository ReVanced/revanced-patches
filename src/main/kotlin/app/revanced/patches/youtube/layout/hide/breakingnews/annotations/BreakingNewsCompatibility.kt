package app.revanced.patches.youtube.layout.hide.breakingnews.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.youtube", arrayOf("18.16.37", "18.19.35"))])
@Target(AnnotationTarget.CLASS)
internal annotation class BreakingNewsCompatibility
