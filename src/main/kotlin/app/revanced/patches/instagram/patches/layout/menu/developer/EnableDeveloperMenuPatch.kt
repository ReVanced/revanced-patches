package app.revanced.patches.instagram.patches.layout.menu.developer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.instagram.patches.layout.menu.developer.fingerprints.ShouldAddPrefTTLFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Enable developer menu",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object EnableDeveloperMenuPatch : BytecodePatch(
    setOf(ShouldAddPrefTTLFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ShouldAddPrefTTLFingerprint.resultOrThrow().mutableMethod.let { method ->
            val isDeveloperMethodCallIndex = method.getInstructions().first {
                it.opcode == Opcode.INVOKE_STATIC
            }.location.index

            val isDeveloperMethod = context.toMethodWalker(method).nextMethod(isDeveloperMethodCallIndex, true)
                .getMethod() as MutableMethod

            isDeveloperMethod.addInstructions(
                0,
                """
                    const v0, 0x1
                    return v0
                """,
            )
        }
    }
}
