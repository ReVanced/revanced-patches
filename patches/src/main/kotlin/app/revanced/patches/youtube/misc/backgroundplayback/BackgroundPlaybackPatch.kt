package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var prefBackgroundAndOfflineCategoryId = -1L
    private set

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/BackgroundPlaybackPatch;"

@Suppress("unused")
val removeBackgroundPlaybackRestrictionsPatch = bytecodePatch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {
    dependsOn(
        resourceMappingPatch,
        addResourcesPatch,
        sharedExtensionPatch,
        playerTypeHookPatch,
        videoInformationPatch,
        settingsPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        ),
    )

    apply {
        addResources("youtube", "misc.backgroundplayback.backgroundPlaybackPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_shorts_disable_background_playback"),
        )

        prefBackgroundAndOfflineCategoryId = ResourceType.STRING["pref_background_and_offline_category"]

        arrayOf(
            backgroundPlaybackManagerMethod to "isBackgroundPlaybackAllowed",
            backgroundPlaybackManagerShortsMethod to "isBackgroundShortsPlaybackAllowed",
        ).forEach { (method, integrationsMethod) ->
            method.apply {
                findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructionsAtControlFlowLabel(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->$integrationsMethod(Z)Z
                            move-result v$register 
                        """,
                    )
                }
            }
        }

        // Enable background playback option in YouTube settings
        backgroundPlaybackSettingsMethod.apply {
            val booleanCalls = instructions.withIndex().filter {
                it.value.getReference<MethodReference>()?.returnType == "Z"
            }

            val settingsBooleanIndex = booleanCalls.elementAt(1).index

            val settingsBooleanMethod = navigate(this).to(settingsBooleanIndex).stop()

            settingsBooleanMethod.returnEarly(true)
        }

        // Force allowing background play for Shorts.
        shortsBackgroundPlaybackFeatureFlagMethod.returnEarly(true)

        // Force allowing background play for videos labeled for kids.
        kidsBackgroundPlaybackPolicyControllerMethod.returnEarly()

        // Fix PiP buttons not working after locking/unlocking device screen.
        if (is_19_34_or_greater) {
            pipInputConsumerFeatureFlagMethodMatch.let {
                it.method.insertLiteralOverride(
                    it[0],
                    false,
                )
            }
        }
    }
}
