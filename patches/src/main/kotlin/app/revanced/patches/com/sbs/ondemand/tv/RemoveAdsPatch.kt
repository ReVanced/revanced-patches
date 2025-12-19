package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes pre-roll, pause and on-demand advertisements from SBS On Demand TV.",
) {
    compatibleWith("com.sbs.ondemand.tv")

    execute {
        // Remove live TV advertisements
        shouldShowAdvertisingTVFingerprint.method.returnEarly(true)

        // Remove pause screen advertisements
        shouldShowPauseAdFingerprint.method.returnEarly(false)

        // Remove on-demand pre-roll advertisements using exception handling
        // Exception handling is used instead of returnEarly() because:
        // 1. returnEarly() causes black screen - app waits for ad content that never comes
        // 2. SBS app has built-in exception handling in handleProviderFailure()
        // 3. Exception triggers fallbackToAkamaiProvider() which loads actual content
        // 4. This preserves the intended app flow: try ads → fail gracefully → load content
        requestAdStreamFingerprint.method.addInstructions(
            0, """
                new-instance v0, Ljava/lang/RuntimeException;
                const-string v1, "Ad stream disabled"
                invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
                throw v0
            """
        )

        // Bypass license verification to prevent crashes after merging split APKs
        licenseContentProviderOnCreateFingerprint.method.returnEarly(true)
        initializeLicenseCheckFingerprint.method.returnEarly()

        // Fix Conviva analytics crashes by providing a valid heartbeat interval
        // The app crashes when Conviva analytics can't get a proper heartbeat interval
        // from the configuration, so we return a standard 30-second timeout to prevent
        // null pointer exceptions and maintain analytics functionality
        convivaConfigGetHBIntervalFingerprint.method.returnEarly(30000)
    }
}
