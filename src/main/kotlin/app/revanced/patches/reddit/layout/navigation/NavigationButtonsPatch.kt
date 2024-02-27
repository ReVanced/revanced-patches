package app.revanced.patches.reddit.layout.navigation

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.navigation.fingerprints.BottomNavScreenFingerprint
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Hide navigation buttons",
    description = "Adds options to hide buttons in the navigation bar.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object NavigationButtonsPatch : BytecodePatch(
    setOf(BottomNavScreenFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/NavigationButtonsPatch;" +
                "->hideNavigationButtons(Landroid/view/ViewGroup;)V"

    override fun execute(context: BytecodeContext) {

        BottomNavScreenFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = it.scanResult.patternScanResult!!.startIndex
                val targetRegister =
                    getInstruction<FiveRegisterInstruction>(startIndex).registerC

                addInstruction(
                    startIndex + 1,
                    "invoke-static {v$targetRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR"
                )
            }
        } ?: throw BottomNavScreenFingerprint.exception

        updateSettingsStatus("NavigationButtons")

    }
}
