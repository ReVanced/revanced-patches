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
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.twitter.misc.links.fingerprints.LinkBuilderFingerprint
import app.revanced.patches.twitter.misc.links.fingerprints.LinkResourceGetterFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference


@Patch(
    name = "Change link sharing domain",
    description = "Replaces the domain name of Twitter links when sharing them.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object ChangeLinkSharingDomainPatch : BytecodePatch(
    setOf(LinkBuilderFingerprint, LinkResourceGetterFingerprint)
) {
    private var domainName by stringPatchOption(
        key = "domainName",
        default = "fxtwitter.com",
        title = "Domain name",
        description = "The domain to use when sharing links.",
        required = true
    )

    override fun execute(context: BytecodeContext) {
        val linkBuilderResult = LinkBuilderFingerprint.result ?: throw LinkBuilderFingerprint.exception

        // region Copy link button.
        val buildShareLinkMethod = linkBuilderResult.mutableMethod.apply {
            val stringIndex = linkBuilderResult.scanResult.stringsScanResult!!.matches
                .first().index

            val targetRegister = getInstruction<OneRegisterInstruction>(stringIndex).registerA
            replaceInstruction(
                stringIndex,
                "const-string v$targetRegister, \"https://$domainName/%1\$s/status/%2\$d\""
            )
        }


        // endregion

        // Used in the Share via... dialog.
        LinkResourceGetterFingerprint.result?.apply {
            val instructions = mutableMethod.getInstructions()

            // Result register of the original method call.
            var shareLinkRegister = 0

            // Remove the original call to the method that uses the resources template to build the link.
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode != Opcode.INVOKE_VIRTUAL) continue

                val methodRef =
                    (instruction as ReferenceInstruction).reference as MethodReference
                if (methodRef.definingClass != "Landroid/content/res/Resources;") continue

                shareLinkRegister = (instructions[index + 1] as OneRegisterInstruction).registerA

                mutableMethod.apply {
                    removeInstructions(index, 2)

                    // Remove the instruction that uses the resultRegister as an array reference to prevent an error.
                    removeInstructions(index - 2, 1)
                }
                break
            }


            var tempInstruction: TwoRegisterInstruction? = null
            var nicknameRegister = -1

            // Get the nickname of the user that posted the tweet. This is used to build the link.
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode != Opcode.INVOKE_VIRTUAL) continue
                val methodReference = (instruction as ReferenceInstruction).reference.toString()
                if (!methodReference.endsWith("Ljava/lang/String;")) continue

                val sourceNicknameRegister =
                    this.mutableMethod.getInstruction<OneRegisterInstruction>(index + 1).registerA

                tempInstruction = mutableMethod.getInstruction<TwoRegisterInstruction>(index - 1)
                nicknameRegister = tempInstruction.registerA

                // Save the user nickname to the register that was used to store "this".
                mutableMethod.addInstruction(
                    index + 2,
                    "move-object v${nicknameRegister}, v$sourceNicknameRegister"
                )
                break

            }
            if (tempInstruction == null) throw PatchException("Save user nickname to free register failed.")


            // Call the patched method and save the result to resultRegister.
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode != Opcode.INVOKE_VIRTUAL) continue
                val methodRef =
                    (instruction as ReferenceInstruction).reference as MethodReference
                if (methodRef.definingClass != "Ljava/lang/Long;") continue

                // Get tweet id (long)
                val tweetIdP1 = (instructions[index + 1] as OneRegisterInstruction).registerA
                val tweetIdP2 = tweetIdP1 + 1

                // Call the patched method with the tweet ID and username and save the result to resultRegister.
                this.mutableMethod.addInstructions(
                    index + 2,
                    """
                        invoke-static { v$tweetIdP1, v$tweetIdP2, v$nicknameRegister }, $buildShareLinkMethod
                        move-result-object v$shareLinkRegister
                    """
                )

                // Restore the register that was used to store our string by duplicating the instruction that got "this".
                this.mutableMethod.addInstruction(index + 4, tempInstruction as BuilderInstruction)
                break
            }

        } ?: throw LinkResourceGetterFingerprint.exception
    }
}
