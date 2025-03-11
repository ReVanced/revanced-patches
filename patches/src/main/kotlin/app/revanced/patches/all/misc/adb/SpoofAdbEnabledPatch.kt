package app.revanced.patches.all.misc.adb

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR_PREFIX =
    "Lapp/revanced/extension/all/spoof/settings/SpoofSettingsPatch"


val SETTINGS_GLOBAL_CLASS_DESCRIPTOR = "Landroid/provider/Settings\$Global;"

//->getInt(Landroid/content/ContentResolver;Ljava/lang/String;I)I

@Suppress("unused")
val spoofAdbEnabledPatch = bytecodePatch(
    name = "Spoof ADB enabled",
    description = "Allows spoofing of the 'adb_enabled' setting.",
    use = true,
) {
    extendWith("extensions/all/misc/settings/spoof-settings.rve")

    val enabledOption by booleanOption(
        key = "enabled",
        default = false,
        title = "Enabled",
        description = "The status to spoof.",
    )

    dependsOn(
        transformInstructionsPatch(
            filterMap = filterMap@{ classDef, method, instruction, instructionIndex ->
                if (instruction.opcode != Opcode.INVOKE_STATIC) return@filterMap null

                val reference = instruction.getReference<MethodReference>() ?: return@filterMap null
                if(reference.definingClass != SETTINGS_GLOBAL_CLASS_DESCRIPTOR) return@filterMap null
                if(reference.name != "getInt") return@filterMap null
                if(reference.parameterTypes != listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;", "I")) return@filterMap null
                if(reference.returnType != "I") return@filterMap null

                instructionIndex to instruction as Instruction35c
            },
            transform = { method, entry ->
                val (idx, instruction) = entry

                method.replaceInstruction(idx,
                    "invoke-static {v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE}}, $EXTENSION_CLASS_DESCRIPTOR_PREFIX;->getInt(Landroid/content/ContentResolver;Ljava/lang/String;I)I")
            }
        )
    )
}
