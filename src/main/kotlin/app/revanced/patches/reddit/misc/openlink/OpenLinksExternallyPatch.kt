package app.revanced.patches.reddit.misc.openlink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.misc.openlink.fingerprints.ScreenNavigatorFingerprint
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex

@Patch(
    name = "Open links externally",
    description = "Adds an option to always open links in your browser instead of in the in-app-browser.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object OpenLinksExternallyPatch : BytecodePatch(
    setOf(ScreenNavigatorFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/OpenLinksExternallyPatch;"

    override fun execute(context: BytecodeContext) {
        ScreenNavigatorFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getStringInstructionIndex("uri") + 2

                addInstructionsWithLabels(
                    insertIndex, """
                        invoke-static {p1, p2}, $INTEGRATIONS_METHOD_DESCRIPTOR->openLinksExternally(Landroid/app/Activity;Landroid/net/Uri;)Z
                        move-result v0
                        if-eqz v0, :dismiss
                        return-void
                        """, ExternalLabel("dismiss", getInstruction(insertIndex))
                )
            }
        } ?: throw ScreenNavigatorFingerprint.exception

        updateSettingsStatus("OpenLinksExternally")

    }
}