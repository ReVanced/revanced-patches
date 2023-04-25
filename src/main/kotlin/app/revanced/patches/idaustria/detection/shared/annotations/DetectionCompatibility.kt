package app.revanced.patches.idaustria.detection.shared.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("at.gv.oe.app")])
@Target(AnnotationTarget.CLASS)
internal annotation class DetectionCompatibility