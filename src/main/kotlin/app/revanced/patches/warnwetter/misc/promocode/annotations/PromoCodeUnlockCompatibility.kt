package app.revanced.patches.warnwetter.misc.promocode.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "de.dwd.warnapp", arrayOf()
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class PromoCodeUnlockCompatibility
