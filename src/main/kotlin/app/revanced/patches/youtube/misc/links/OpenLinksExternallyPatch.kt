package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.patch.AbstractTransformInstructionsPatch
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Open links externally",
    description = "Open links outside of the app directly in your browser.",
    compatiblePackages = [CompatiblePackage("com.google.android.youtube", ["18.16.37", "18.19.35", "18.20.39", "18.23.35", "18.29.38", "18.32.39"])]
)
@Suppress("unused")
object OpenLinksExternallyPatch : AbstractTransformInstructionsPatch<Pair<Int, Int>>(
) {
    override fun filterMap(
        classDef: ClassDef, method: Method, instruction: Instruction, instructionIndex: Int
    ): Pair<Int, Int>? {
        if (instruction !is ReferenceInstruction) return null
        val reference = instruction.reference as? StringReference ?: return null

        if (reference.string != "android.support.customtabs.action.CustomTabsService") return null

        return instructionIndex to (instruction as OneRegisterInstruction).registerA
    }

    override fun transform(mutableMethod: MutableMethod, entry: Pair<Int, Int>) {
        val (intentStringIndex, register) = entry

        // Hook the intent string.
        mutableMethod.addInstructions(
            intentStringIndex + 1,
            """
                invoke-static {v$register}, Lapp/revanced/integrations/patches/OpenLinksExternallyPatch;->getIntent(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$register
            """
        )
    }

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_external_browser",
                StringResource("revanced_external_browser_title", "Open links in browser"),
                StringResource("revanced_external_browser_summary_on", "Opening links externally"),
                StringResource("revanced_external_browser_summary_off", "Opening links in app")
            )
        )

        super.execute(context)
    }
}