package app.revanced.patches.music.general.historybutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.general.historybutton.fingerprints.HistoryMenuItemFingerprint
import app.revanced.patches.music.general.historybutton.fingerprints.HistoryMenuItemOfflineTabFingerprint
import app.revanced.patches.music.utils.integrations.Constants.GENERAL
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Hide history button",
    description = "Adds an option to hide the history button in the toolbar.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object HideHistoryButtonPatch : BytecodePatch(
    setOf(
        HistoryMenuItemFingerprint,
        HistoryMenuItemOfflineTabFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        arrayOf(
            HistoryMenuItemFingerprint,
            HistoryMenuItemOfflineTabFingerprint
        ).forEach { fingerprint ->
            fingerprint.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.startIndex
                    val insertRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

                    addInstructions(
                        insertIndex, """
                            invoke-static {v$insertRegister}, $GENERAL->hideHistoryButton(Z)Z
                            move-result v$insertRegister
                            """
                    )
                }
            } ?: throw fingerprint.exception
        }

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_hide_history_button",
            "false"
        )

    }
}
