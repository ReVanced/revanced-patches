package app.revanced.patches.music.utils.intenthook

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.utils.integrations.Constants.INTEGRATIONS_PATH
import app.revanced.patches.music.utils.intenthook.fingerprints.FullStackTraceActivityFingerprint
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(dependencies = [SettingsPatch::class])
object IntentHookPatch : BytecodePatch(
    setOf(FullStackTraceActivityFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        FullStackTraceActivityFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    1, """
                        invoke-static {p0}, $INTEGRATIONS_PATH/settingsmenu/ReVancedSettingActivity;->initializeSettings(Landroid/app/Activity;)Z
                        move-result v0
                        if-eqz v0, :show
                        return-void
                        """, ExternalLabel("show", getInstruction(1))
                )
            }
        } ?: throw FullStackTraceActivityFingerprint.exception

    }
}