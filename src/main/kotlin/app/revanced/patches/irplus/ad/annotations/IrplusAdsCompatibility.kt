package app.revanced.patches.irplus.ad.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("net.binarymode.android.irplus")])
@Target(AnnotationTarget.CLASS)
internal annotation class IrplusAdsCompatibility