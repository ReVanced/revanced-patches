package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.feedfilter.fingerprints.feedApiServiceLIZFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.settingsStatusLoadFingerprint
import app.revanced.patches.tiktok.misc.settings.settingsPatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val feedFilterPatch = bytecodePatch(
    name = "Feed filter",
    description = "Removes ads, livestreams, stories, image videos " +
        "and videos with a specific amount of views or likes from the feed.",
) {
    dependsOn(integrationsPatch, settingsPatch)

    compatibleWith("com.ss.android.ugc.trill"("32.5.3"), "com.zhiliaoapp.musically"("32.5.3"))

    val feedApiServiceLIZResult by feedApiServiceLIZFingerprint
    val settingsStatusLoadResult by settingsStatusLoadFingerprint

    execute {
        feedApiServiceLIZResult.mutableMethod.apply {
            val returnFeedItemInstruction = getInstructions().first { it.opcode == Opcode.RETURN_OBJECT }
            val feedItemsRegister = (returnFeedItemInstruction as OneRegisterInstruction).registerA

            addInstruction(
                returnFeedItemInstruction.location.index,
                "invoke-static { v$feedItemsRegister }, " +
                    "Lapp/revanced/integrations/tiktok/feedfilter/FeedItemsFilter;->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V",
            )
        }

        settingsStatusLoadResult.mutableMethod.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/integrations/tiktok/settings/SettingsStatus;->enableFeedFilter()V",
        )
    }
}
