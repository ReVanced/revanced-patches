package app.revanced.patches.youtube.misc.minimizedplayback.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.KidsMinimizedPlaybackPolicyControllerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.PipControllerFingerprint
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction
import org.jf.dexlib2.iface.reference.MethodReference


@Patch
@Name("minimized-playback")
@Description("Enables minimized and background playback.")
@DependsOn([IntegrationsPatch::class, PlayerTypeHookPatch::class, SettingsPatch::class])
@MinimizedPlaybackCompatibility
@Version("0.0.1")
class MinimizedPlaybackPatch : BytecodePatch(
    listOf(
        KidsMinimizedPlaybackPolicyControllerFingerprint,
        MinimizedPlaybackManagerFingerprint,
        MinimizedPlaybackSettingsFingerprint,
        PipControllerFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_enable_minimized_playback",
                StringResource("revanced_minimized_playback_enabled_title", "Enable minimized playback"),
                true,
                StringResource("revanced_minimized_playback_summary_on", "Minimized playback is enabled"),
                StringResource("revanced_minimized_playback_summary_off", "Minimized playback is disabled")
            )
        )

        MinimizedPlaybackManagerFingerprint.result?.apply {
            mutableMethod.addInstructions(
                0, """
                invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->isMinimizedPlaybackEnabled()Z
                move-result v0
                return v0
                """
            )
        } ?: return MinimizedPlaybackManagerFingerprint.toErrorResult()

        val method = MinimizedPlaybackSettingsFingerprint.result!!.mutableMethod
        val booleanCalls = method.implementation!!.instructions.withIndex()
            .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

        val settingsBooleanIndex = booleanCalls.elementAt(1).index
        val settingsBooleanMethod =
            context.toMethodWalker(method).nextMethod(settingsBooleanIndex, true).getMethod() as MutableMethod

        settingsBooleanMethod.addInstructions(
            0, """
                invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->isMinimizedPlaybackEnabled()Z
                move-result v0
                return v0
                """
        )

        KidsMinimizedPlaybackPolicyControllerFingerprint.result?.apply {
            mutableMethod.addInstructions(
                0, """
                invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->isMinimizedPlaybackEnabled()Z
                move-result v0
                if-eqz v0, :enable
                return-void
                :enable
                nop
                """
            )
        } ?: return KidsMinimizedPlaybackPolicyControllerFingerprint.toErrorResult()

        PipControllerFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.endIndex + 1
            val pipEnabledRegister = (mutableMethod.instruction(insertIndex - 1) as TwoRegisterInstruction).registerA

            mutableMethod.addInstructions(
                insertIndex,
                """
                    invoke-static {v$pipEnabledRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->isNotPlayingShorts(Z)Z
                    move-result v$pipEnabledRegister
                """
            )

        } ?: return PipControllerFingerprint.toErrorResult()
        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/MinimizedPlaybackPatch;"
    }
}
