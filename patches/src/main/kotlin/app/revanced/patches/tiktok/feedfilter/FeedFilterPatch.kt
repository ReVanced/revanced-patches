package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.misc.settings.settingsStatusLoadFingerprint
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
    )

    compatibleWith(
        "com.ss.android.ugc.trill"("43.6.2"),
        "com.zhiliaoapp.musically"("43.6.2"),
    )

    execute {
        settingsStatusLoadFingerprint.method.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/extension/tiktok/settings/SettingsStatus;->enableFeedFilter()V",
        )

        // Hook into the model getter, as TikTok feed data is no longer guaranteed to go through
        // FeedApiService.fetchFeedList() on 43.6.2 (e.g., cache pipelines).
        feedItemListGetItemsFingerprint.method.let { method ->
            val returnIndices = method.implementation!!.instructions.withIndex()
                .filter { it.value.opcode == Opcode.RETURN_OBJECT }
                .map { it.index }
                .toList()

            returnIndices.asReversed().forEach { returnIndex ->
                method.addInstructions(
                    returnIndex,
                    """
                        invoke-static {p0}, $EXTENSION_CLASS_DESCRIPTOR->filter(Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;)V
                        nop
                    """,
                )
            }
        }

        arrayOf(
            followFeedFingerprint.method to "$EXTENSION_CLASS_DESCRIPTOR->filter(Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;)V"
        ).forEach { (method, filterSignature) ->
            val returnIndices = method.implementation!!.instructions.withIndex()
                .filter { it.value.opcode == Opcode.RETURN_OBJECT }
                .map { it.index }
                .toList()

            returnIndices.asReversed().forEach { returnIndex ->
                val register = (method.implementation!!.instructions[returnIndex] as OneRegisterInstruction).registerA

                method.addInstructions(
                    returnIndex,
                    """
                        if-eqz v$register, :revanced_skip_filter_$returnIndex
                        invoke-static/range { v$register .. v$register }, $filterSignature
                        :revanced_skip_filter_$returnIndex
                        nop
                    """,
                )
            }
        }
    }

}
