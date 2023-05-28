package app.revanced.patches.scbeasy.detection.debugging.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.scb.phone")])
@Target(AnnotationTarget.CLASS)
internal annotation class RemoveDebuggingDetectionCompatibility
