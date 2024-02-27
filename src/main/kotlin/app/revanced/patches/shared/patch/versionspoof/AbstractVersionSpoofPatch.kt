package app.revanced.patches.shared.patch.versionspoof

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.shared.fingerprints.versionspoof.ClientInfoFingerprint
import app.revanced.patches.shared.fingerprints.versionspoof.ClientInfoParentFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.dexbacked.reference.DexBackedFieldReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

abstract class AbstractVersionSpoofPatch(
    private val descriptor: String
) : BytecodePatch(
    setOf(ClientInfoParentFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        ClientInfoParentFingerprint.result?.let { parentResult ->
            ClientInfoFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.mutableMethod?.let {
                it.apply {
                    var insertIndex = 0
                    val insertInstructions = implementation!!.instructions
                    val targetString = "Landroid/os/Build\$VERSION;->RELEASE:Ljava/lang/String;"

                    for ((index, instruction) in insertInstructions.withIndex()) {
                        if (instruction.opcode != Opcode.SGET_OBJECT) continue

                        val indexString =
                            ((instruction as? ReferenceInstruction)?.reference as? DexBackedFieldReference).toString()

                        if (indexString != targetString) continue

                        val targetRegister = (instruction as OneRegisterInstruction).registerA
                        insertIndex = index - 1

                        addInstructions(
                            insertIndex, """
                                invoke-static {v$targetRegister}, $descriptor
                                move-result-object v$targetRegister
                                """
                        )
                        break
                    }
                    if (insertIndex <= 0) throw ClientInfoFingerprint.exception
                }
            } ?: throw ClientInfoFingerprint.exception
        } ?: throw ClientInfoParentFingerprint.exception

    }
}