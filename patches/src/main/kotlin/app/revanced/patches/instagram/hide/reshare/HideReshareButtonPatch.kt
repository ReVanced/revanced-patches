package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val hideReshareButtonPatch = bytecodePatch(
    name = "Hide reshare button",
    description = "Hides the reshare button from posts.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        mediaJsonParserFingerprint.method.apply {
            addInstructions(
                0,
                """
                    move-object/from16 v2, p3
                    const/16 v0, 0x94
                    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v0
                    sget-object v1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    invoke-virtual {v2, v0, v1}, Ljava/util/AbstractMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                """
            )
        }

        mediaJsonParserFingerprint3.method.apply {
            val i =  indexOfFirstInstructionOrThrow {
                getReference<FieldReference>()?.name=="A3J"
            }

            addInstruction(i,
                "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }

    }
}
