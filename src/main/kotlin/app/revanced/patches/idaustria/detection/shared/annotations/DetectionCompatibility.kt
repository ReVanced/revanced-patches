package app.revanced.patches.idaustria.detection.shared.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("at.gv.oe.app", arrayOf("2.5.2", "2.6.0"))])
@Target(AnnotationTarget.CLASS)
internal annotation class DetectionCompatibility