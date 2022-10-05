package app.revanced.patches.tiktok.feedfilter.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.feedfilter.annotations.FeedFilterCompatibility
import app.revanced.patches.tiktok.feedfilter.fingerprints.FeedApiServiceLIZFingerprint
import app.revanced.patches.tiktok.misc.integrations.patch.TikTokIntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.patches.tiktok.misc.settings.patch.TikTokSettingsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([TikTokIntegrationsPatch::class, TikTokSettingsPatch::class])
@Name("tiktok-feed-filter")
@Description("Filters tiktok videos: removing ads, removing livestreams.")
@FeedFilterCompatibility
@Version("0.0.1")
class TiktokFeedFilter : BytecodePatch(
    listOf(
        FeedApiServiceLIZFingerprint,
        SettingsStatusLoadFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val method = FeedApiServiceLIZFingerprint.result!!.mutableMethod
        for ((index, instruction) in method.implementation!!.instructions.withIndex()) {
            if (instruction.opcode != Opcode.RETURN_OBJECT) continue
            val feedItemsRegister = (instruction as OneRegisterInstruction).registerA
            method.addInstruction(
                index,
                "invoke-static {v$feedItemsRegister}, Lapp/revanced/tiktok/feedfilter/FeedItemsFilter;->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V"
            )
            break
        }
        val method2 = SettingsStatusLoadFingerprint.result!!.mutableMethod
        method2.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/tiktok/settingsmenu/SettingsStatus;->enableFeedFilter()V"
        )
        return PatchResultSuccess()
    }
}
