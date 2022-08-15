package app.revanced.patches.ecmwf.misc.subscription.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.garzotto.pflotsh.ecmwf_a", arrayOf("3.5.4")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class SubscriptionUnlockCompatibility
