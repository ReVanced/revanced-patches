package app.revanced.patches.youtube.misc.minimizedplayback

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
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.KidsMinimizedPlaybackPolicyControllerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsParentFingerprint
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Minimized playback",
    description = "Unlocks options for picture-in-picture and background playback.",
    dependencies = [
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
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object MinimizedPlaybackPatch : BytecodePatch(
    setOf(
        MinimizedPlaybackManagerFingerprint,
        MinimizedPlaybackSettingsParentFingerprint,
        KidsMinimizedPlaybackPolicyControllerFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/MinimizedPlaybackPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            NonInteractivePreference("revanced_minimized_playback")
        )

        MinimizedPlaybackManagerFingerprint.result?.apply {
            mutableMethod.addInstructions(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->playbackIsNotShort()Z
                    move-result v0
                    return v0
                """
            )
        } ?: throw MinimizedPlaybackManagerFingerprint.exception

        // Enable minimized playback option in YouTube settings
        MinimizedPlaybackSettingsParentFingerprint.result ?: throw MinimizedPlaybackSettingsParentFingerprint.exception
        MinimizedPlaybackSettingsFingerprint.resolve(
            context,
            MinimizedPlaybackSettingsParentFingerprint.result!!.classDef
        )
        MinimizedPlaybackSettingsFingerprint.result?.apply {
            val booleanCalls = method.implementation!!.instructions.withIndex()
                .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

            val settingsBooleanIndex = booleanCalls.elementAt(1).index
            val settingsBooleanMethod =
                context.toMethodWalker(method).nextMethod(settingsBooleanIndex, true).getMethod() as MutableMethod

            settingsBooleanMethod.addInstructions(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->overrideMinimizedPlaybackAvailable()Z
                    move-result v0
                    return v0
                """
            )
        } ?: throw MinimizedPlaybackSettingsFingerprint.exception

        // Force allowing background play for videos labeled for kids.
        // Some regions and YouTube accounts do not require this patch.
        KidsMinimizedPlaybackPolicyControllerFingerprint.result?.apply {
            mutableMethod.addInstruction(
                0,
                "return-void"
            )
        } ?: throw KidsMinimizedPlaybackPolicyControllerFingerprint.exception
    }
}
