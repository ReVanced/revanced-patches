package app.revanced.patches.instagram.patches.developerMenu

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.patches.developerMenu.fingerprints.ShouldAddPrefTTLFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.dexbacked.reference.DexBackedMethodReference

@Patch(
    name = "Enable developer menu",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false,
)
@Suppress("unused")
object EnableDeveloperMenuPatch : BytecodePatch(
    setOf(ShouldAddPrefTTLFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ShouldAddPrefTTLFingerprint.resultOrThrow().let { methodFingerprintResult ->
            methodFingerprintResult.mutableMethod.apply {
                // finding the method which calls the developer flag check method.
                val developerMethodCallIndex = getInstructions().first { it.opcode == Opcode.INVOKE_STATIC }.location.index
                val developerMethodReference = (getInstruction<BuilderInstruction35c>(developerMethodCallIndex).reference as DexBackedMethodReference)
                // the class and method name were the developer flag check happens.
                val className = developerMethodReference.definingClass
                val methodName = developerMethodReference.name

                // locating the developer flag check method.
                val developerFlagCheckMethod = context.findClass(className)!!.mutableClass.methods.first { it.name == methodName }

                // make the method return true always.
                developerFlagCheckMethod.addInstructions(
                    0,
                    """
                    const v0, 0x1
                    return v0
                    """.trimIndent(),
                )
            }
        }
    }
}
