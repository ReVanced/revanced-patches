package app.revanced.patches.youtube.misc.externalbrowser

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.patch.transformation.AbstractTransformInstructionsPatch
import app.revanced.patches.youtube.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Enable external browser",
    description = "Adds an option to always open links in your browser instead of in the in-app-browser.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ExternalBrowserPatch : AbstractTransformInstructionsPatch<Pair<Int, Int>>(
) {
    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
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
            intentStringIndex + 1, """
                invoke-static {v$register}, $MISC_PATH/ExternalBrowserPatch;->enableExternalBrowser(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$register
                """
        )
    }

    override fun execute(context: BytecodeContext) {
        super.execute(context)

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: ENABLE_EXTERNAL_BROWSER"
            )
        )

        SettingsPatch.updatePatchStatus("Enable external browser")
    }
}