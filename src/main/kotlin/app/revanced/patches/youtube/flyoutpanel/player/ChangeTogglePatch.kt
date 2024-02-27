package app.revanced.patches.youtube.flyoutpanel.player

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.flyoutpanel.player.fingerprints.AdditionalSettingsConfigFingerprint
import app.revanced.patches.youtube.flyoutpanel.player.fingerprints.CinematicLightingFingerprint
import app.revanced.patches.youtube.flyoutpanel.player.fingerprints.PlaybackLoopInitFingerprint
import app.revanced.patches.youtube.flyoutpanel.player.fingerprints.PlaybackLoopOnClickListenerFingerprint
import app.revanced.patches.youtube.flyoutpanel.player.fingerprints.StableVolumeFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.FLYOUT_PANEL
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Change player flyout panel toggles",
    description = "Adds an option to use text toggles instead of switch toggles within the additional settings menu.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ChangeTogglePatch : BytecodePatch(
    setOf(
        AdditionalSettingsConfigFingerprint,
        CinematicLightingFingerprint,
        PlaybackLoopOnClickListenerFingerprint,
        StableVolumeFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val additionalSettingsConfigResult = AdditionalSettingsConfigFingerprint.result
            ?: throw AdditionalSettingsConfigFingerprint.exception

        val additionalSettingsConfigMethod = additionalSettingsConfigResult.mutableMethod
        val methodToCall = additionalSettingsConfigMethod.definingClass + "->" + additionalSettingsConfigMethod.name + "()Z"

        // Resolves fingerprints
        val playbackLoopOnClickListenerResult = PlaybackLoopOnClickListenerFingerprint.result
            ?: throw PlaybackLoopOnClickListenerFingerprint.exception
        PlaybackLoopInitFingerprint.resolve(context, playbackLoopOnClickListenerResult.classDef)

        arrayOf(
            CinematicLightingFingerprint,
            PlaybackLoopInitFingerprint,
            PlaybackLoopOnClickListenerFingerprint,
            StableVolumeFingerprint
        ).forEach { fingerprint ->
            fingerprint.injectCall(methodToCall)
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: FLYOUT_PANEL_SETTINGS",
                "SETTINGS: PLAYER_FLYOUT_PANEL_ADDITIONAL_SETTINGS_HEADER",
                "SETTINGS: CHANGE_PLAYER_FLYOUT_PANEL_TOGGLE"
            )
        )

        SettingsPatch.updatePatchStatus("Change player flyout panel toggles")

    }

    private fun MethodFingerprint.injectCall(descriptor: String) {
        result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.INVOKE_VIRTUAL
                            && (instruction as ReferenceInstruction).reference.toString().endsWith(descriptor)
                } + 2
                val insertRegister =
                    getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $FLYOUT_PANEL->changeSwitchToggle(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        } ?: throw exception
    }
}
