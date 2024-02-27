package app.revanced.patches.shared.patch.customspeed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.shared.fingerprints.customspeed.SpeedArrayGeneratorFingerprint
import app.revanced.patches.shared.fingerprints.customspeed.SpeedLimiterFallBackFingerprint
import app.revanced.patches.shared.fingerprints.customspeed.SpeedLimiterFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

abstract class AbstractCustomPlaybackSpeedPatch(
    private val descriptor: String,
    private val maxSpeed: Float
) : BytecodePatch(
    setOf(
        SpeedArrayGeneratorFingerprint,
        SpeedLimiterFallBackFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SpeedArrayGeneratorFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val targetIndex = result.scanResult.patternScanResult!!.startIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        invoke-static {v$targetRegister}, $descriptor->getLength(I)I
                        move-result v$targetRegister
                        """
                )

                val targetInstruction = implementation!!.instructions

                for ((index, instruction) in targetInstruction.withIndex()) {
                    if (instruction.opcode != Opcode.INVOKE_INTERFACE) continue

                    val sizeInstruction = getInstruction<Instruction35c>(index)
                    if ((sizeInstruction.reference as MethodReference).name != "size") continue

                    val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                    addInstructions(
                        index + 2, """
                        invoke-static {v$register}, $descriptor->getSize(I)I
                        move-result v$register
                        """
                    )
                    break
                }


                for ((index, instruction) in targetInstruction.withIndex()) {
                    if (instruction.opcode != Opcode.SGET_OBJECT) continue

                    val targetReference =
                        getInstruction<ReferenceInstruction>(index).reference.toString()

                    if (targetReference.endsWith(":[F")) {
                        val register = getInstruction<OneRegisterInstruction>(index).registerA

                        addInstructions(
                            index + 1, """
                                invoke-static {v$register}, $descriptor->getArray([F)[F
                                move-result-object v$register
                                """
                        )
                        break
                    }
                }
            }
        } ?: throw SpeedArrayGeneratorFingerprint.exception

        val speedLimiterParentResult = SpeedLimiterFallBackFingerprint.result
            ?: throw SpeedLimiterFallBackFingerprint.exception
        SpeedLimiterFingerprint.resolve(context, speedLimiterParentResult.classDef)
        val speedLimiterResult = SpeedLimiterFingerprint.result
            ?: throw SpeedLimiterFingerprint.exception

        arrayOf(
            speedLimiterParentResult,
            speedLimiterResult
        ).forEach { result ->
            result.mutableMethod.apply {
                val limiterMinConstIndex =
                    implementation!!.instructions.indexOfFirst { (it as? NarrowLiteralInstruction)?.narrowLiteral == 0.25f.toRawBits() }
                val limiterMaxConstIndex =
                    implementation!!.instructions.indexOfFirst { (it as? NarrowLiteralInstruction)?.narrowLiteral == 2.0f.toRawBits() }

                val limiterMinConstDestination =
                    getInstruction<OneRegisterInstruction>(limiterMinConstIndex).registerA
                val limiterMaxConstDestination =
                    getInstruction<OneRegisterInstruction>(limiterMaxConstIndex).registerA

                replaceInstruction(
                    limiterMinConstIndex,
                    "const/high16 v$limiterMinConstDestination, 0x0"
                )
                replaceInstruction(
                    limiterMaxConstIndex,
                    "const/high16 v$limiterMaxConstDestination, ${maxSpeed.toRawBits()}"
                )
            }
        }

    }
}
