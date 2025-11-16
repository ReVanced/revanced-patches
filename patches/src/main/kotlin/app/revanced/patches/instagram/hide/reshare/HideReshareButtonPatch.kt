package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.*
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val hideReshareButtonPatch = bytecodePatch(
    name = "Hide reshare button",
    description = "Hides the reshare button from both posts and reels.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        p.method.apply {
            val setCanReshareInstructionIndex = indexOfFirstInstruction(
                p.stringMatches!!.first().index,
                Opcode.MOVE_RESULT_OBJECT
            )

            val canReshareBooleanRegister = getInstruction<OneRegisterInstruction>(setCanReshareInstructionIndex).registerA

            addInstruction(
                setCanReshareInstructionIndex + 1,
                "sget-object v$canReshareBooleanRegister, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }

        mediaJsonParserFingerprint.method.apply {
            addInstruction(
                0,
                """
                    move-object/from16 v2, p3
                    const/16 v0, 0x94
                    sget-object v1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    invoke-virtual {v2, v0, v1}, Ljava/util/AbstractMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                """
            )
        }

        mediaJsonParserFingerprint3.method.apply {
            val i = indexOfFirstInstructionOrThrow {
                getReference<FieldReference>()?.name=="A3E"
            }

            addInstruction(i,
                "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }

        mediaJsonParserFingerprint4.method.apply {
            val i = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_16 && (this as NarrowLiteralInstruction).narrowLiteral == 0x94
            }

            addInstruction(i,
                "sget-object v1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }
    }
}
