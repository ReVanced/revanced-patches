package app.revanced.patches.candylinkvpn.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.candylink.openvpn")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
