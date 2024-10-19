package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.hide.fullscreenambientmode.fingerprints.InitializeAmbientModeFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Disable fullscreen ambient mode",
    description = "Adds an option to disable the ambient mode when in fullscreen.",
    dependencies = [SettingsPatch::class, IntegrationsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object DisableFullscreenAmbientModePatch : BytecodePatch(
    setOf(InitializeAmbientModeFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DisableFullscreenAmbientModePatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_fullscreen_ambient_mode")
        )

        InitializeAmbientModeFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveIsEnabledIndex = it.scanResult.patternScanResult!!.endIndex

                addInstruction(
                    moveIsEnabledIndex,
                    "invoke-static { }, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->enableFullScreenAmbientMode()Z"
                )
            }
        } ?: throw InitializeAmbientModeFingerprint.exception
    }
}
