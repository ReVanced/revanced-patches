package app.revanced.patches.youtube.misc.autorepeat.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.autorepeat.annotations.AutoRepeatCompatibility
import app.revanced.patches.youtube.misc.autorepeat.fingerprints.AutoRepeatFingerprint
import app.revanced.patches.youtube.misc.autorepeat.fingerprints.AutoRepeatParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Patch
@DependsOn([IntegrationsPatch::class])
@Name("always-autorepeat")
@Description("Always repeats the playing video again.")
@AutoRepeatCompatibility
@Version("0.0.1")
class AutoRepeatPatch : BytecodePatch(
    listOf(
        AutoRepeatParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_pref_auto_repeat",
                StringResource("revanced_auto_repeat_enabled_title", "Enable auto-repeat"),
                false,
                StringResource("revanced_auto_repeat_summary_on", "Auto-repeat is enabled"),
                StringResource("revanced_auto_repeat_summary_off", "Auto-repeat is disabled")
            )
        )

        //Get Result from the ParentFingerprint which is the playMethod we need to get.
        val parentResult = AutoRepeatParentFingerprint.result
            ?: return PatchResultError("ParentFingerprint did not resolve.")

        //this one needs to be called when app/revanced/integrations/patches/AutoRepeatPatch;->shouldAutoRepeat() returns true
        val playMethod = parentResult.mutableMethod
        AutoRepeatFingerprint.resolve(context, parentResult.classDef)
        //String is: Laamp;->E()V
        val methodToCall = playMethod.definingClass + "->" + playMethod.name + "()V";

        //This is the method we search for
        val result = AutoRepeatFingerprint.result
            ?: return PatchResultError("FingerPrint did not resolve.")
        val method = result.mutableMethod

        //Instructions to add to the smali code
        val instructions = """
            invoke-static {}, Lapp/revanced/integrations/patches/AutoRepeatPatch;->shouldAutoRepeat()Z
            move-result v0
            if-eqz v0, :noautorepeat
            invoke-virtual {p0}, $methodToCall
            :noautorepeat
            return-void
        """

        //Get the implementation so we can do a check for null and get instructions size.
        val implementation = method.implementation
            ?: return PatchResultError("No Method Implementation found!")

        //Since addInstructions needs an index which starts counting at 0 and size starts counting at 1,
        //we have to remove 1 to get the latest instruction
        val index = implementation.instructions.size - 1


        //remove last instruction which is return-void
        method.removeInstruction(index)
        // Add our own instructions there
        method.addInstructions(index, instructions)

        //Everything worked as expected, return Success
        return PatchResultSuccess()
    }
}
