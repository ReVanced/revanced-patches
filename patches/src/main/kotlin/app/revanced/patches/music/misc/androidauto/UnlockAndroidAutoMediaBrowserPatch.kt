package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.registersUsed
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val unlockAndroidAutoMediaBrowserPatch = bytecodePatch(
    name = "Android Auto Support",
    description = "Unlock Android Auto Media Browser",
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
        checkCertificateFingerprint.method.returnEarly(true)

        searchMediaItemsExecuteFingerprint
        .match(searchMediaItemsConstructorFingerprint.classDef)
        .method.apply {
            val targetIndex = instructions.indexOfFirst { 
            	it.opcode == Opcode.IGET_OBJECT && it.getReference<FieldReference>()?.type == "Ljava/lang/String;"
            }

            val register = instructions[targetIndex].registersUsed.first()
            replaceInstruction(targetIndex, "const-string v$register, \"com.google.android.apps.youtube.music\"")
        }
    }
}
