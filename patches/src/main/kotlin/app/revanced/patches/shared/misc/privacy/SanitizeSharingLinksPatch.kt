package app.revanced.patches.shared.misc.privacy

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.Match
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.addInstructionsAtControlFlowLabel
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/SanitizeSharingLinksPatch;"

/**
 * Patch shared by YouTube and YT Music.
 */
internal fun sanitizeSharingLinksPatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
    preferenceScreen: BasePreferenceScreen.Screen,
    replaceMusicLinksWithYouTube: Boolean = false,
) = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    block()

    dependsOn(addResourcesPatch)

    apply {
        executeBlock()

        addResources("shared", "misc.privacy.sanitizeSharingLinksPatch")

        val sanitizePreference = SwitchPreference("revanced_sanitize_sharing_links")

        preferenceScreen.addPreferences(
            if (replaceMusicLinksWithYouTube) {
                PreferenceCategory(
                    titleKey = null,
                    sorting = Sorting.UNSORTED,
                    tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                    preferences = setOf(
                        sanitizePreference,
                        SwitchPreference("revanced_replace_music_with_youtube"),
                    ),
                )
            } else {
                sanitizePreference
            },
        )

        fun Match.hook(
            getInsertIndex: List<Int>.() -> Int,
            getUrlRegister: MutableMethod.(insertIndex: Int) -> Int,
        ) {
            val insertIndex = indices[0].getInsertIndex()
            val urlRegister = method.getUrlRegister(insertIndex)

            method.addInstructions(
                insertIndex,
                """
                    invoke-static {v$urlRegister}, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """,
            )
        }

        // YouTube share sheet.\
        youTubeShareSheetMethodMatch.hook(getInsertIndex = { first() + 1 }) { insertIndex ->
            getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
        }

        // Native system share sheet.
        youTubeSystemShareSheetMethodMatch.hook(getInsertIndex = { last() }) { insertIndex ->
            getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
        }

        youTubeCopyTextMethodMatch.hook(getInsertIndex = { first() + 2 }) { insertIndex ->
            getInstruction<TwoRegisterInstruction>(insertIndex - 2).registerA
        }
    }
}
