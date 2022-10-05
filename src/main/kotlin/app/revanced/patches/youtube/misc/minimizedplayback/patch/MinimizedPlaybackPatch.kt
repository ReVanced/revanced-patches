package app.revanced.patches.youtube.misc.minimizedplayback.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackKidsFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference


@Patch
@Name("minimized-playback")
@Description("Enables minimized and background playback.")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@MinimizedPlaybackCompatibility
@Version("0.0.1")
class MinimizedPlaybackPatch : BytecodePatch(
    listOf(
        MinimizedPlaybackKidsFingerprint, MinimizedPlaybackManagerFingerprint, MinimizedPlaybackSettingsFingerprint
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

        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        MinimizedPlaybackManagerFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                invoke-static {}, Lapp/revanced/integrations/patches/MinimizedPlaybackPatch;->isMinimizedPlaybackEnabled()Z
                move-result v0
                return v0
                """
        )

        val method = MinimizedPlaybackSettingsFingerprint.result!!.mutableMethod
        val booleanCalls = method.implementation!!.instructions.withIndex()
            .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

        val settingsBooleanIndex = booleanCalls.elementAt(1).index
        val settingsBooleanMethod =
            context.toMethodWalker(method).nextMethod(settingsBooleanIndex, true).getMethod() as MutableMethod

        settingsBooleanMethod.addInstructions(
            0, """
                invoke-static {}, Lapp/revanced/integrations/patches/MinimizedPlaybackPatch;->isMinimizedPlaybackEnabled()Z
                move-result v0
                return v0
                """
        )

        MinimizedPlaybackKidsFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                invoke-static {}, Lapp/revanced/integrations/patches/MinimizedPlaybackPatch;->isMinimizedPlaybackEnabled()Z
                move-result v0
                if-eqz v0, :enable
                return-void
                :enable
                nop
                """
        )

        return PatchResultSuccess()
    }
}
