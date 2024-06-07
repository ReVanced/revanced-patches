package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.backgroundplayback.fingerprints.KidsBackgroundPlaybackPolicyControllerFingerprint
import app.revanced.patches.youtube.misc.backgroundplayback.fingerprints.BackgroundPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.backgroundplayback.fingerprints.BackgroundPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
    dependencies = [
        BackgroundPlaybackResourcePatch::class,
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class,
        VideoInformationPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
            ]
        )
    ]
)
@Suppress("unused")
object BackgroundPlaybackPatch : BytecodePatch(
    setOf(
        BackgroundPlaybackManagerFingerprint,
        BackgroundPlaybackSettingsFingerprint,
        KidsBackgroundPlaybackPolicyControllerFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/BackgroundPlaybackPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            NonInteractivePreference("revanced_background_playback")
        )

        BackgroundPlaybackManagerFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->playbackIsNotShort()Z
                move-result v0
                return v0
            """
        )

        // Enable background playback option in YouTube settings
        BackgroundPlaybackSettingsFingerprint.resultOrThrow().mutableMethod.apply {
            val booleanCalls = implementation!!.instructions.withIndex()
                .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

            val settingsBooleanIndex = booleanCalls.elementAt(1).index
            val settingsBooleanMethod =
                context.toMethodWalker(this).nextMethod(settingsBooleanIndex, true).getMethod() as MutableMethod

            settingsBooleanMethod.addInstructions(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->overrideBackgroundPlaybackAvailable()Z
                    move-result v0
                    return v0
                """
            )
        }

        // Force allowing background play for videos labeled for kids.
        KidsBackgroundPlaybackPolicyControllerFingerprint.resultOrThrow().mutableMethod.addInstruction(
            0,
            "return-void"
        )
    }
}
