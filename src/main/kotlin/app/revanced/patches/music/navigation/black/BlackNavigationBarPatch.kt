package app.revanced.patches.music.navigation.black

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.navigation.black.fingerprints.TabLayoutFingerprint
import app.revanced.patches.music.utils.integrations.Constants.NAVIGATION
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable black navigation bar",
    description = "Adds an option to set the navigation bar color to black.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object BlackNavigationBarPatch : BytecodePatch(
    setOf(TabLayoutFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        TabLayoutFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        invoke-static {}, $NAVIGATION->enableBlackNavigationBar()I
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw TabLayoutFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.NAVIGATION,
            "revanced_enable_black_navigation_bar",
            "true"
        )

    }
}
