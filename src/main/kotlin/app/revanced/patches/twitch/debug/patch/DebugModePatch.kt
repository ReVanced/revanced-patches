package app.revanced.patches.twitch.debug.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.twitch.debug.annotations.DebugModeCompatibility
import app.revanced.patches.twitch.debug.fingerprints.IsDebugConfigEnabledFingerprint
import app.revanced.patches.twitch.debug.fingerprints.IsOmVerificationEnabledFingerprint
import app.revanced.patches.twitch.debug.fingerprints.ShouldShowDebugOptionsFingerprint
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch(false)
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("debug-mode")
@Description("Enables Twitch's internal debugging mode.")
@DebugModeCompatibility
@Version("0.0.1")
class DebugModePatch : BytecodePatch(
    listOf(
        IsDebugConfigEnabledFingerprint,
        IsOmVerificationEnabledFingerprint,
        ShouldShowDebugOptionsFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        listOf(
            IsDebugConfigEnabledFingerprint,
            IsOmVerificationEnabledFingerprint,
            ShouldShowDebugOptionsFingerprint
        ).forEach {
            with(it.result!!) {
                with(mutableMethod) {
                    addInstructions(
                        0,
                        """
                             invoke-static {}, Lapp/revanced/twitch/patches/DebugModePatch;->isDebugModeEnabled()Z
                             move-result v0
                             return v0
                          """
                    )
                }
            }
        }

        SettingsPatch.PreferenceScreen.MISC.OTHER.addPreferences(
            SwitchPreference(
                "revanced_debug_mode",
                StringResource(
                    "revanced_debug_mode_enable",
                    "Enable debug mode"
                ),
                false,
                StringResource(
                    "revanced_debug_mode_on",
                    "Debug mode is enabled (not recommended)"
                ),
                StringResource(
                    "revanced_debug_mode_off",
                    "Debug mode is disabled"
                ),
            )
        )

        return PatchResultSuccess()
    }
}
