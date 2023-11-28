package app.revanced.patches.twitter.misc.links

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import app.revanced.patches.twitter.misc.links.fingerprints.LinkBuilderMethodFingerprint
import app.revanced.patches.twitter.misc.links.fingerprints.LinkResourceGetterFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference


@Patch(
    name = "Change link sharing domain",
    description = "Replaces the domain name of Twitter links when sharing them.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object ChangeLinkSharingDomainPatch : BytecodePatch(
    setOf(LinkBuilderMethodFingerprint, LinkResourceGetterFingerprint)
) {
    private var domain by stringPatchOption(
        key = "domain",
        default = "fxtwitter.com",
        title = "Domain name",
        description = "The domain to use when sharing links.",
    )

    override fun execute(context: BytecodeContext) {
        // Replace the domain in the method that generates the share link
        val linkBuilderMethod = LinkBuilderMethodFingerprint.result?.apply {

            // Find the index of the string instruction that contains the link
            val stringIndex = scanResult.stringsScanResult!!.matches.find { match ->
                (match.string == "https://twitter.com/%1\$s/status/%2\$d" || match.string == "https://x.com/%1\$s/status/%2\$d")
            }!!.index

            val instruction = this.mutableMethod.getInstruction<OneRegisterInstruction>(stringIndex)
            val overrideRegister = instruction.registerA
            val string = ((instruction as ReferenceInstruction).reference as StringReference).string

            var overrideString = string
            when {
                string.contains("twitter.com") -> {
                    overrideString = string.replace("twitter.com", domain.toString())
                }

                string.contains("x.com") -> {
                    overrideString = string.replace("x.com", domain.toString())
                }
            }

            this.mutableMethod.replaceInstruction(
                stringIndex,
                """
                    const-string v$overrideRegister, "$overrideString"
                """
            )
        } ?: throw LinkBuilderMethodFingerprint.exception

        LinkResourceGetterFingerprint.result?.apply {
            val instructions = this.mutableMethod.getInstructions()

            // Result register of the original method call
            var resultRegister = 0

            // Remove instructions at the end and get the register of the last instruction
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {
                    val methodRef =
                        (instruction as ReferenceInstruction).reference as MethodReference
                    if (methodRef.definingClass != "Landroid/content/res/Resources;") continue

                    // Get the result register of the original method call
                    resultRegister = (instructions[index + 1] as OneRegisterInstruction).registerA

                    // Remove the original method call for getting the link from resources and the move-result-object instruction
                    this.mutableMethod.removeInstructions(index, 2)

                    // Remove the instruction that uses the resultRegister as an array reference to prevent an error
                    this.mutableMethod.removeInstructions(index - 2, 1)

                    break
                }
            }

            // Save user nickname to v7
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {

                    val methodRef =
                        (instruction as ReferenceInstruction).reference as MethodReference

                    if (methodRef.returnType != "Ljava/lang/String;") continue

                    val sourceRegister = (instructions[index + 1] as OneRegisterInstruction).registerA
                    this.mutableMethod.addInstruction(
                        index + 2,
                        """
                            move-object v7, v$sourceRegister
                        """
                    )
                    break
                }
            }

            // Call the patched method and save the result to resultRegister
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {

                    val methodRef =
                        (instruction as ReferenceInstruction).reference as MethodReference
                    if (methodRef.definingClass != "Ljava/lang/Long;") continue

                    // Get the first part of the tweet ID
                    val sourceRegister = (instructions[index + 1] as OneRegisterInstruction).registerA
                    // Get the second part of the tweet ID
                    val sourcePlusOne = sourceRegister + 1

                    // Call the patched method with the tweet ID and username and save the result to resultRegister
                    this.mutableMethod.addInstructions(
                        index + 2,
                        """
                            invoke-static { v$sourceRegister, v$sourcePlusOne, v7 }, ${linkBuilderMethod.method}
                            move-result-object v$resultRegister
                        """
                    )
                    break
                }
            }

        } ?: throw LinkResourceGetterFingerprint.exception
    }
}
