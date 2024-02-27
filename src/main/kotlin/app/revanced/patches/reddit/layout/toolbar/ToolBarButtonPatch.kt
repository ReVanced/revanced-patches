package app.revanced.patches.reddit.layout.toolbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.toolbar.fingerprints.HomePagerScreenFingerprint
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch.ToolBarNavSearchCtaContainer
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide toolbar button",
    description = "Adds an option to hide the r/place or Reddit recap button in the toolbar.",
    dependencies =
    [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object ToolBarButtonPatch : BytecodePatch(
    setOf(HomePagerScreenFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/ToolBarButtonPatch;" +
                "->hideToolBarButton(Landroid/view/View;)V"

    override fun execute(context: BytecodeContext) {

        HomePagerScreenFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex =
                    getWideLiteralInstructionIndex(ToolBarNavSearchCtaContainer) + 3
                val targetRegister =
                    getInstruction<OneRegisterInstruction>(targetIndex - 1).registerA

                addInstruction(
                    targetIndex,
                    "invoke-static {v$targetRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR"
                )
            }
        } ?: throw HomePagerScreenFingerprint.exception

        updateSettingsStatus("ToolBarButton")

    }
}
