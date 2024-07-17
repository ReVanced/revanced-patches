package app.revanced.patches.all.analytics.crashlytics

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.crashlytics.fingerprints.DoConfigFetchFingerprint
import app.revanced.patches.all.analytics.crashlytics.fingerprints.SettingsSpiCallFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable Crashlytics"
)
@Suppress("unused")
object DisableCrashlytics : BytecodePatch(
    setOf(SettingsSpiCallFingerprint, DoConfigFetchFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Neutralize the two methods responsible for requesting Crashlytics' configuration
        // which effectively disables the SDK

        SettingsSpiCallFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                    const/4 p1, 0x0
                    return-object p1
                    """
        )

        DoConfigFetchFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                    return-object p1
                    """
        )
    }
}