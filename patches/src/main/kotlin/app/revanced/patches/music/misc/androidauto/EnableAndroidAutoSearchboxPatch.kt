package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.registersUsed
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference

@Suppress("unused")
val enableFullSearchAndroidAutoPatch = bytecodePatch(
    name = "Enable full search (Android Auto)",
    description = "Enable full search in YouTube Music on Android Auto.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        val method = enableFullSearchAndroidAutoFingerprint.method
        val instructions = enableFullSearchAndroidAutoFingerprint.method.instructions
        val targetIndex = instructions.indexOfFirst { it.opcode == Opcode.IGET_OBJECT
                            && it.getReference<FieldReference>()?.let { field ->
                                field.type == "Ljava/lang/String;"
                            } ?: false }

        val register = instructions[targetIndex].registersUsed.first()
        val newInstruction = BuilderInstruction21c(
            Opcode.CONST_STRING,
            register,
            ImmutableStringReference("com.google.android.apps.youtube.music")
        )
        method.replaceInstruction(targetIndex, newInstruction)
    }
}
