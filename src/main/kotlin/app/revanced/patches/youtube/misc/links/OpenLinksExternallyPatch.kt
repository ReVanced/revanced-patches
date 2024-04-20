package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Open links externally",
    description = "Adds an option to always open links in your browser instead of in the in-app-browser.",
    dependencies = [SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
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
object OpenLinksExternallyPatch : BaseTransformInstructionsPatch<Pair<Int, Int>>(
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
                invoke-static {v$register}, Lapp/revanced/integrations/youtube/patches/OpenLinksExternallyPatch;->getIntent(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$register
            """
        )
    }

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_external_browser")
        )

        super.execute(context)
    }
}