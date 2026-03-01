package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.registersUsed
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val unlockAndroidAutoMediaBrowserPatch = bytecodePatch(
    name = "Unlock Android Auto Media Browser",
    description = "Unlocks Android Auto Media Browser which enables the search function including speech to text.",
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
            "8.37.56",
            "8.40.54",
        ),
    )

    apply {
        checkCertificateMethod.returnEarly(true)

        searchMediaItemsConstructorMethod.immutableClassDef.getSearchMediaItemsExecuteMethod()
            .apply {
                val targetIndex = instructions.indexOfFirst {
                    it.opcode == Opcode.IGET_OBJECT && it.fieldReference?.type == "Ljava/lang/String;"
                }

                val register = instructions[targetIndex].registersUsed.first()
                replaceInstruction(
                    targetIndex,
                    "const-string v$register, \"com.google.android.apps.youtube.music\""
                )
            }
    }
}
