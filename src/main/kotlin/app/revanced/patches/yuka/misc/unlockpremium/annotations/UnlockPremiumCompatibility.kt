package app.revanced.patches.yuka.misc.unlockpremium.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("io.yuka.android")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockPremiumCompatibility