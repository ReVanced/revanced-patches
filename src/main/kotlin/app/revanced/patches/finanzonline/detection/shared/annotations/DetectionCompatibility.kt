package app.revanced.patches.finanzonline.detection.shared.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("at.gv.bmf.bmf2go")])
@Target(AnnotationTarget.CLASS)
internal annotation class DetectionCompatibility
