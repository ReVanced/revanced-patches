package app.revanced.patches.youtube.misc.minimizedplayback.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.NonInteractivePreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.KidsMinimizedPlaybackPolicyControllerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsParentFingerprint
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch
@Name("Minimized playback")
@Description("Enables minimized and background playback.")
@DependsOn([IntegrationsPatch::class, PlayerTypeHookPatch::class, SettingsPatch::class])
@MinimizedPlaybackCompatibility
class MinimizedPlaybackPatch : BytecodePatch(
    listOf(
        MinimizedPlaybackManagerFingerprint,
        MinimizedPlaybackSettingsParentFingerprint,
        KidsMinimizedPlaybackPolicyControllerFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        // TODO: remove this empty preference sometime after mid 2023
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            NonInteractivePreference(
                StringResource("revanced_minimized_playback_enabled_title", "Minimized playback"),
                StringResource("revanced_minimized_playback_summary_on", "This setting can be found in Settings -> Background")
            )
        )

        MinimizedPlaybackManagerFingerprint.result?.apply {
            mutableMethod.addInstructions(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->isPlaybackNotShort()Z
                    move-result v0
                    return v0
                """
            )
        } ?: throw MinimizedPlaybackManagerFingerprint.exception

        // Enable minimized playback option in YouTube settings
        MinimizedPlaybackSettingsParentFingerprint.result ?: throw MinimizedPlaybackSettingsParentFingerprint.exception
        MinimizedPlaybackSettingsFingerprint.resolve(context, MinimizedPlaybackSettingsParentFingerprint.result!!.classDef)
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

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/MinimizedPlaybackPatch;"
    }
}
