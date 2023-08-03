package app.revanced.patches.lightroom.misc.login.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.adobe.lrmobile",)])
@Target(AnnotationTarget.CLASS)
internal annotation class DisableMandatoryLoginCompatibility
