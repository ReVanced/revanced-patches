package app.revanced.patches.interaction

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.smali.asInstructions
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.formats.Instruction11n
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference

class EnableSeekbarTapping : Patch("enable-seekbar-tapping") {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["tap-seekbar-parent-method"]

        val tapSeekMethods = mutableMapOf<String, Method>()

        // find the methods which tap the seekbar
        for (it in map.definingClassProxy.immutableClass.methods) {
            if (it.implementation == null) continue

            val instructions = it.implementation!!.instructions
            // here we make sure we actually find the method because it has more then 7 instructions
            if (instructions.count() < 7) continue

            // we know that the 7th instruction has the opcode CONST_4
            val instruction = instructions.elementAt(6)
            if (instruction.opcode != Opcode.CONST_4) continue

            // the literal for this instruction has to be either 1 or 2
            val literal = (instruction as Instruction11n).narrowLiteral

            // method founds
            if (literal == 1) tapSeekMethods["P"] = it
            if (literal == 2) tapSeekMethods["O"] = it
        }

        val implementation = cache.methodMap["enable-seekbar-tapping"].method.implementation!!

        // if tap-seeking is enabled, do not invoke the two methods below
        val pMethod = tapSeekMethods["P"]!!
        val oMethod = tapSeekMethods["O"]!!
        implementation.addInstructions(
            map.scanData.endIndex,
            listOf(
                BuilderInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    0,
                    3,
                    0,
                    0,
                    0,
                    0,
                    ImmutableMethodReference(
                        oMethod.definingClass,
                        oMethod.name,
                        setOf("I"),
                        "V"
                    )
                ),
                BuilderInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    0,
                    3,
                    0,
                    0,
                    0,
                    0,
                    ImmutableMethodReference(
                        pMethod.definingClass,
                        pMethod.name,
                        setOf("I"),
                        "V"
                    )
                )
            )
        )

        // if tap-seeking is disabled, do not invoke the two methods above by jumping to the else label
        val elseLabel = implementation.newLabelForIndex(map.scanData.endIndex)
        implementation.addInstruction(
            map.scanData.endIndex,
            BuilderInstruction21t(Opcode.IF_EQZ, 0, elseLabel)
        )
        implementation.addInstructions(
            map.scanData.endIndex,
            """
                invoke-static { }, Lfi/razerman/youtube/preferences/BooleanPreferences;->isTapSeekingEnabled()Z
                move-result v0
            """.trimIndent().asInstructions()
        )
        return PatchResultSuccess()
    }
}