package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.feedfilter.fingerprints.FeedApiServiceLIZFingerprint
import app.revanced.patches.tiktok.misc.integrations.IntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.SettingsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Feed filter",
    description = "Removes ads, livestreams, stories, image videos " +
            "and videos with a specific amount of views or likes from the feed.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
@Suppress("unused")
object FeedFilterPatch : BytecodePatch(
    setOf(FeedApiServiceLIZFingerprint, SettingsStatusLoadFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        FeedApiServiceLIZFingerprint.result?.mutableMethod?.apply {
            val returnFeedItemInstruction = getInstructions().first { it.opcode == Opcode.RETURN_OBJECT }
            val feedItemsRegister = (returnFeedItemInstruction as OneRegisterInstruction).registerA

            addInstruction(
                returnFeedItemInstruction.location.index,
                "invoke-static { v$feedItemsRegister }, " +
                        "Lapp/revanced/integrations/tiktok/feedfilter/FeedItemsFilter;->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V"
            )
        } ?: throw FeedApiServiceLIZFingerprint.exception

        SettingsStatusLoadFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/integrations/tiktok/settings/SettingsStatus;->enableFeedFilter()V"
        ) ?: throw SettingsStatusLoadFingerprint.exception
    }
}
