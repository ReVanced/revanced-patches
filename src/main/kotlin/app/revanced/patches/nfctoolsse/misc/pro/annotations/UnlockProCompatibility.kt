package app.revanced.patches.nfctoolsse.misc.pro.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.wakdev.apps.nfctools.se")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
