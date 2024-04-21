package app.revanced.patches.youtube.layout.buttons.captions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.autocaptions.fingerprints.SubtitleButtonControllerFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Hide captions button",
    description = "Adds an option to hide the captions button in the video player.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object HideCaptionsButtonPatch : BytecodePatch(
    setOf(SubtitleButtonControllerFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_captions_button")
        )

        val subtitleButtonControllerMethod = SubtitleButtonControllerFingerprint.result!!.mutableMethod

        // Due to previously applied patches, scanResult index cannot be used in this context
        val insertIndex = subtitleButtonControllerMethod.implementation!!.instructions.indexOfFirst {
            it.opcode == Opcode.IGET_BOOLEAN
        } + 1

        subtitleButtonControllerMethod.addInstruction(
            insertIndex,
            """
                invoke-static {v0}, Lapp/revanced/integrations/youtube/patches/HideCaptionsButtonPatch;->hideCaptionsButton(Landroid/widget/ImageView;)V
            """
        )
    }
}
