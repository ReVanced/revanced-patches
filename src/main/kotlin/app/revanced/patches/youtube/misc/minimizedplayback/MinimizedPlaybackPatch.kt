package app.revanced.patches.youtube.misc.minimizedplayback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.kidsMinimizedPlaybackPolicyControllerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.minimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.minimizedPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.minimizedPlaybackSettingsParentFingerprint
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/MinimizedPlaybackPatch;"

@Suppress("unused")
val minimizedPlaybackPatch = bytecodePatch(
    name = "Minimized playback",
    description = "Unlocks options for picture-in-picture and background playback.",
) {
    dependsOn(
        integrationsPatch,
        playerTypeHookPatch,
        videoInformationPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
        ),
    )

    val minimizedPlaybackManagerResult by minimizedPlaybackManagerFingerprint
    val minimizedPlaybackSettingsParentResult by minimizedPlaybackSettingsParentFingerprint
    val kidsMinimizedPlaybackPolicyControllerResult by kidsMinimizedPlaybackPolicyControllerFingerprint

    execute { context ->
        addResources("youtube", "misc.minimizedplayback.MinimizedPlaybackPatch")

        PreferenceScreen.MISC.addPreferences(
            NonInteractivePreference("revanced_minimized_playback"),
        )

        minimizedPlaybackManagerResult.mutableMethod.addInstructions(
            0,
            """
                invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->playbackIsNotShort()Z
                move-result v0
                return v0
            """,
        )

        // Enable minimized playback option in YouTube settings
        minimizedPlaybackSettingsFingerprint.apply {
            resolve(
                context,
                minimizedPlaybackSettingsParentResult.classDef,
            )
        }.resultOrThrow().let { result ->
            val settingsBooleanIndex = result.method.implementation!!.instructions.withIndex()
                .filter { (_, instruction) -> instruction.getReference<MethodReference>()?.returnType == "Z" }
                .elementAt(1).index

            val settingsBooleanMethod = context.navigate(result.method).at(settingsBooleanIndex).mutable()
            settingsBooleanMethod.addInstructions(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->overrideMinimizedPlaybackAvailable()Z
                    move-result v0
                    return v0
                """,
            )
        }

        // Force allowing background play for videos labeled for kids.
        // Some regions and YouTube accounts do not require this patch.
        kidsMinimizedPlaybackPolicyControllerResult.mutableMethod.addInstruction(0, "return-void")
    }
}
