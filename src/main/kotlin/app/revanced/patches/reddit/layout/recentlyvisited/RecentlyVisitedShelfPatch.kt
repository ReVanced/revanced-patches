package app.revanced.patches.reddit.layout.recentlyvisited

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.recentlyvisited.fingerprints.CommunityDrawerPresenterFingerprint
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide recently visited shelf",
    description = "Adds an option to hide the recently visited shelf in the sidebar.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object RecentlyVisitedShelfPatch : BytecodePatch(
    setOf(CommunityDrawerPresenterFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/RecentlyVisitedShelfPatch;" +
                "->hideRecentlyVisitedShelf(Ljava/util/List;)Ljava/util/List;"

    override fun execute(context: BytecodeContext) {

        CommunityDrawerPresenterFingerprint.result?.let {
            it.mutableMethod.apply {
                arrayOf(
                    it.scanResult.patternScanResult!!.endIndex,
                    it.scanResult.patternScanResult!!.startIndex + 3
                ).forEach { insertIndex ->
                    val insertRegister =
                        getInstruction<OneRegisterInstruction>(insertIndex).registerA

                    addInstructions(
                        insertIndex, """
                            invoke-static {v$insertRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR
                            move-result-object v$insertRegister
                            """
                    )
                }
            }
        } ?: throw CommunityDrawerPresenterFingerprint.exception

        updateSettingsStatus("RecentlyVisitedShelf")

    }
}
