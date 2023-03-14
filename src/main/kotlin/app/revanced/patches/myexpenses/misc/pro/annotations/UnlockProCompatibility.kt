package app.revanced.patches.myexpenses.misc.pro.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("org.totschnig.myexpenses", arrayOf("3.4.9"))])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
