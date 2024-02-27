package app.revanced.patches.music.general.oldstylelibraryshelf

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.general.oldstylelibraryshelf.fingerprints.BrowseIdFingerprint
import app.revanced.patches.music.utils.integrations.Constants.GENERAL
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Enable old style library shelf",
    description = "Adds an option to return the library tab to the old style.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object OldStyleLibraryShelfPatch : BytecodePatch(
    setOf(BrowseIdFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        BrowseIdFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getStringInstructionIndex("FEmusic_offline") - 5
                val targetRegister = getInstruction<TwoRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        invoke-static {v$targetRegister}, $GENERAL->enableOldStyleLibraryShelf(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$targetRegister
                        """
                )
            }
        } ?: throw BrowseIdFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_enable_old_style_library_shelf",
            "false"
        )

    }
}