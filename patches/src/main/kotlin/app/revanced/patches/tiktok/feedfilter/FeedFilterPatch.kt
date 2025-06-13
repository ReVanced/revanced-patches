package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.misc.settings.settingsPatch
import app.revanced.patches.tiktok.misc.settings.settingsStatusLoadFingerprint
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/tiktok/feedfilter/FeedItemsFilter;"

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
        "com.ss.android.ugc.trill"("39.8.1"),
        "com.zhiliaoapp.musically"("39.8.1"),
    )

    execute {
        arrayOf(
            feedApiServiceLIZFingerprint.method to "$EXTENSION_CLASS_DESCRIPTOR->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V",
            getFollowFeedFingerprint.method to "$EXTENSION_CLASS_DESCRIPTOR->filter(Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;)V"
        ).forEach { (method, filterSignature) -> method.apply {
            val returnInstructionIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
            val feedRegister = getInstruction<OneRegisterInstruction>(returnInstructionIndex).registerA

            method.addInstruction(
                returnInstructionIndex,
                "invoke-static { v$feedRegister }, $filterSignature"
            )
        } }

        settingsStatusLoadFingerprint.method.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/extension/tiktok/settings/SettingsStatus;->enableFeedFilter()V",
        )
    }
}
