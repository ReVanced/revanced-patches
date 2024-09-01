package app.revanced.patches.instagram.developerMenu

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.developerMenu.fingerprints.EnableDeveloperMenuFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.dexbacked.reference.DexBackedMethodReference

@Patch(
    name = "Enable developer menu",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false
)
@Suppress("unused")
object EnableDeveloperMenuPatch:BytecodePatch(
    setOf(EnableDeveloperMenuFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        val result = EnableDeveloperMenuFingerprint.result ?: throw EnableDeveloperMenuFingerprint.exception

        val method = result.mutableMethod
        //find the method
        val loc = method.getInstructions().first { it.opcode == Opcode.INVOKE_STATIC }.location.index
        val ref = (method.getInstruction<BuilderInstruction35c>(loc).reference as DexBackedMethodReference)
        //the class and method name were check happens
        val className = ref.definingClass
        val methodName = ref.name

        val inject_method = context.findClass(className)!!.mutableClass.methods.first { it.name == methodName }

        //make the method return always true
        inject_method.addInstructions(0,"""
            const v0, 0x1
            return v0
        """.trimIndent())

    }
}