package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS
import app.revanced.patches.shared.PATCH_NAME_SANITIZE_SHARING_LINKS
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.addInstructionsAtControlFlowLabel
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/SanitizeSharingLinksPatch;"

/**
 * Patch shared by YouTube and YT Music.
 */
internal fun sanitizeSharingLinksPatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
    preferenceScreen: BasePreferenceScreen.Screen,
    replaceMusicLinksWithYouTube: Boolean = false
) = bytecodePatch(
    name = PATCH_NAME_SANITIZE_SHARING_LINKS,
    description = PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS,
) {
    block()

    dependsOn(addResourcesPatch)

    execute {
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
                        SwitchPreference("revanced_replace_music_with_youtube")
                    )
                )
            } else {
                sanitizePreference
            }
        )

        fun Fingerprint.hookUrlString(matchIndex: Int) {
            val index = instructionMatches[matchIndex].index
            val urlRegister = method.getInstruction<OneRegisterInstruction>(index).registerA

            method.addInstructions(
                index + 1,
                """
                    invoke-static { v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """
            )
        }

        fun Fingerprint.hookIntentPutExtra(matchIndex: Int) {
            val index = instructionMatches[matchIndex].index
            val urlRegister = method.getInstruction<FiveRegisterInstruction>(index).registerE

            method.addInstructionsAtControlFlowLabel(
                index,
                """
                    invoke-static { v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """
            )
        }

        // YouTube share sheet copy link.
        youTubeCopyTextFingerprint.hookUrlString(0)

        // YouTube share sheet other apps.
        youTubeShareSheetFingerprint.hookIntentPutExtra(3)

        // Native system share sheet.
        youTubeSystemShareSheetFingerprint.hookIntentPutExtra(3)
    }
}