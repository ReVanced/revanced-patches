package app.revanced.patches.ticktick.misc.themeunlock.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.ticktick.task")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockThemesCompatibility
