package app.revanced.patches.duolingo.ad

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.duolingo.ad.fingerprints.MonetizationDebugSettingsFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c

@Patch(
    name = "Remove Duolingo Ads", compatiblePackages = [CompatiblePackage("com.duolingo")]
)
@Suppress("unused")
object RemoveDuolingoAdsPatch : BytecodePatch(
    setOf(
        MonetizationDebugSettingsFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        // we have a couple of options to approach this:
        // - `MonetizationDebugSettings` has a boolean value for `disableAds`
        // - `OnboardingState` has a getter to check if the user has any `adFreeSessions`
        // - `SharedPreferences` has a debug boolean value with key `disable_ads`, which maps to `DebugCategory.DISABLE_ADS`
        //
        // we'll target `MonetizationDebugSettings`, as it seems to be the most general setting that seems to work a-okay
        MonetizationDebugSettingsFingerprint.result?.mutableMethod?.apply {
            val firstAssigner = getInstructions().filterIsInstance<BuilderInstruction22c>()
                .firstOrNull { it.opcode == Opcode.IPUT_BOOLEAN } ?: throw MonetizationDebugSettingsFingerprint.exception

            // force the value of the first assignment (`disableAds`) to be `true`
            addInstructions(
                firstAssigner.location.index, """
                    const/4 v${firstAssigner.registerA}, 0x1
                """.trimIndent()
            )
        } ?: throw MonetizationDebugSettingsFingerprint.exception
    }
}