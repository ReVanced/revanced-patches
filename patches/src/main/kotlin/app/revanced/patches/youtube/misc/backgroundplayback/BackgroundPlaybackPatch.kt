package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var prefBackgroundAndOfflineCategoryId = -1L
    private set

private val backgroundPlaybackResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch, addResourcesPatch)

    execute {
        prefBackgroundAndOfflineCategoryId = resourceMappings["string", "pref_background_and_offline_category"]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/BackgroundPlaybackPatch;"

val backgroundPlaybackPatch = bytecodePatch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {
    dependsOn(
        backgroundPlaybackResourcePatch,
        sharedExtensionPatch,
        playerTypeHookPatch,
        videoInformationPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
        ),
    )

    val backgroundPlaybackManagerMatch by backgroundPlaybackManagerFingerprint()
    val backgroundPlaybackSettingsMatch by backgroundPlaybackSettingsFingerprint()

    val shortsBackgroundPlaybackFeatureFlagMatch by shortsBackgroundPlaybackFeatureFlagFingerprint()
    val backgroundPlaybackManagerShortsMatch by backgroundPlaybackManagerShortsFingerprint()

    val kidsBackgroundPlaybackPolicyControllerMatch by kidsBackgroundPlaybackPolicyControllerFingerprint()

    execute { context ->
        addResources("youtube", "misc.backgroundplayback.backgroundPlaybackPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_shorts_disable_background_playback")
        )

        arrayOf(
            backgroundPlaybackManagerMatch to "isBackgroundPlaybackAllowed",
            backgroundPlaybackManagerShortsMatch to "isBackgroundShortsPlaybackAllowed"
        ).forEach { (match, integrationsMethod) ->
            match.mutableMethod.apply {
                findInstructionIndicesReversedOrThrow(Opcode.RETURN).forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructionsAtControlFlowLabel(
                        index,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->$integrationsMethod(Z)Z
                            move-result v$register 
                        """
                    )
                }
            }
        }

        // Enable background playback option in YouTube settings
        backgroundPlaybackSettingsMatch.mutableMethod.apply {
            val booleanCalls = instructions.withIndex().filter {
                it.value.getReference<MethodReference>()?.returnType == "Z"
            }

            val settingsBooleanIndex = booleanCalls.elementAt(1).index
            val settingsBooleanMethod = context.navigate(this).at(settingsBooleanIndex).mutable()

            settingsBooleanMethod.returnEarly(true)
        }

        // Force allowing background play for Shorts.
        shortsBackgroundPlaybackFeatureFlagMatch.mutableMethod.returnEarly(true)

        // Force allowing background play for videos labeled for kids.
        kidsBackgroundPlaybackPolicyControllerMatch.mutableMethod.returnEarly()
    }
}
