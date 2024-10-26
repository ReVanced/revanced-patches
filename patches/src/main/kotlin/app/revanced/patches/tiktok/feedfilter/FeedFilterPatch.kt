package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.misc.settings.settingsPatch
import app.revanced.patches.tiktok.misc.settings.settingsStatusLoadFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val feedFilterPatch = bytecodePatch(
    name = "Feed filter",
    description = "Removes ads, livestreams, stories, image videos " +
        "and videos with a specific amount of views or likes from the feed.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.ss.android.ugc.trill"("36.5.4"),
        "com.zhiliaoapp.musically"("36.5.4"),
    )

    val feedApiServiceLIZMatch by feedApiServiceLIZFingerprint()
    val settingsStatusLoadMatch by settingsStatusLoadFingerprint()

    execute {
        feedApiServiceLIZMatch.mutableMethod.apply {
            val returnFeedItemInstruction = instructions.first { it.opcode == Opcode.RETURN_OBJECT }
            val feedItemsRegister = (returnFeedItemInstruction as OneRegisterInstruction).registerA

            addInstruction(
                returnFeedItemInstruction.location.index,
                "invoke-static { v$feedItemsRegister }, " +
                    "Lapp/revanced/extension/tiktok/feedfilter/FeedItemsFilter;->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V",
            )
        }

        settingsStatusLoadMatch.mutableMethod.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/extension/tiktok/settings/SettingsStatus;->enableFeedFilter()V",
        )
    }
}
