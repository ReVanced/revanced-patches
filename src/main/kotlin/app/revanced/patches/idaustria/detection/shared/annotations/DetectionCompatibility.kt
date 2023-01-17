package app.revanced.patches.idaustria.detection.shared.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("at.gv.oe.app", arrayOf("2.5.2"))])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class DetectionCompatibility