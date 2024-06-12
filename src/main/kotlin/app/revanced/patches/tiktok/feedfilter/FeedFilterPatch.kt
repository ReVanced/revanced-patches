package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.integrations.integrationsPatch
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
        integrationsPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.ss.android.ugc.trill"("32.5.3"),
        "com.zhiliaoapp.musically"("32.5.3"),
    )

    val feedApiServiceLIZFingerprintResult by feedApiServiceLIZFingerprint
    val settingsStatusLoadFingerprintResult by settingsStatusLoadFingerprint

    execute {
        feedApiServiceLIZFingerprintResult.mutableMethod.apply {
            val returnFeedItemInstruction = instructions.first { it.opcode == Opcode.RETURN_OBJECT }
            val feedItemsRegister = (returnFeedItemInstruction as OneRegisterInstruction).registerA

            addInstruction(
                returnFeedItemInstruction.location.index,
                "invoke-static { v$feedItemsRegister }, " +
                    "Lapp/revanced/integrations/tiktok/feedfilter/FeedItemsFilter;->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V",
            )
        }

        settingsStatusLoadFingerprintResult.mutableMethod.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/integrations/tiktok/settings/SettingsStatus;->enableFeedFilter()V",
        )
    }
}
