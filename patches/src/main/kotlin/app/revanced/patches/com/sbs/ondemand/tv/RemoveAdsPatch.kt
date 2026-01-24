package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.shared.misc.pairip.license.`Disable Pairip license check`
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Remove ads` by creatingBytecodePatch(
    description = "Removes pre-roll, pause and on-demand advertisements from SBS On Demand TV.",
) {
    compatibleWith("com.sbs.ondemand.tv")

    dependsOn(`Disable Pairip license check`)

    apply {
        shouldShowAdvertisingTVMethod.returnEarly(true)
        shouldShowPauseAdMethod.returnEarly(false)

        // Remove on-demand pre-roll advertisements using exception handling.
        // Exception handling is used instead of returnEarly() because:
        // 1. returnEarly() causes black screen when the app waits for ad content that never comes.
        // 2. SBS app has built-in exception handling in handleProviderFailure().
        // 3. Exception triggers fallbackToAkamaiProvider() which loads actual content.
        // 4. This preserves the intended app flow: first try ads, then fail gracefully, then load content.
        requestAdStreamMethod.addInstructions(
            0,
            """
                new-instance v0, Ljava/lang/RuntimeException;
                const-string v1, "Ad stream disabled"
                invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
                throw v0
            """,
        )
    }
}
