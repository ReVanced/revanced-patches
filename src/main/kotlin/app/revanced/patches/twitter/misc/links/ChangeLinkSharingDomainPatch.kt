package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.twitter.misc.links.fingerprints.LinkBuilderFingerprint
import app.revanced.patches.twitter.misc.links.fingerprints.LinkResourceGetterFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
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
            // Remove the original call to the method that uses the resources template to build the link.
            val originalMethodIndex = mutableMethod.indexOfFirstInstruction {
                getReference<MethodReference>()?.definingClass == "Landroid/content/res/Resources;"
            }
            if (originalMethodIndex == -1) throw PatchException("Could not find originalMethodIndex")

            val shareLinkRegister =
                mutableMethod.getInstruction<OneRegisterInstruction>(
                    // Offset to instruction that saves the result of the method call to the register.
                    originalMethodIndex + 1
                ).registerA

            mutableMethod.apply {
                // Remove original method call and instruction to move the result to the shareLinkRegister.
                removeInstructions(originalMethodIndex, 2)

                // Remove the instruction that uses the shareLinkRegister as an array reference to prevent an error.
                // Original smali aput-object v3, v1, v5 (v1 is the shareLinkRegister)
                removeInstructions(originalMethodIndex - 2, 1)
            }


            // region Get the nickname of the user that posted the tweet. This is used to build the link.
            val getNicknameIndex = mutableMethod.indexOfFirstInstruction {
                getReference<MethodReference>().toString().endsWith("Ljava/lang/String;")
            }
            if (getNicknameIndex == -1) throw PatchException("Could not find getNicknameIndex")

            val sourceNicknameRegister =
                this.mutableMethod.getInstruction<OneRegisterInstruction>(
                    // Offset to instruction that saves the result of the method call to the register.
                    getNicknameIndex + 1
                ).registerA

            // Instruction with a spare register that can be used to store the user nickname.
            val tempInstructionIndex = mutableMethod.indexOfFirstInstruction {
                opcode == Opcode.IGET_OBJECT
            }
            if (tempInstructionIndex == -1) throw PatchException("Could not find tempInstructionIndex")
            if (tempInstructionIndex > getNicknameIndex) throw PatchException(
                "tempInstructionIndex > getNicknameIndex, this indicates that the instruction was moved."
            )

            val tempInstruction = mutableMethod.getInstruction<TwoRegisterInstruction>(
                tempInstructionIndex
            )
            val nicknameRegister = tempInstruction.registerA

            // Save the user nickname to the register that was used to store "this".
            mutableMethod.addInstruction(
                // This offset is used to place the instruction after sourceNicknameRegister is filled with data.
                getNicknameIndex + 2,
                "move-object v${nicknameRegister}, v$sourceNicknameRegister"
            )
            // endregion


            // region Call the patched method and save the result to shareLinkRegister.
            val convertTweetIdToLongIndex = mutableMethod.indexOfFirstInstruction {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.definingClass == "Ljava/lang/Long;"
            }
            if (convertTweetIdToLongIndex == -1) throw PatchException("Could not find convertTweetIdToLongIndex")

            val tweetIdP1 =
                mutableMethod.getInstruction<OneRegisterInstruction>(convertTweetIdToLongIndex + 1).registerA
            val tweetIdP2 = tweetIdP1 + 1

            this.mutableMethod.addInstructions(
                // This offset is used to place the instruction after the save of the method result.
                convertTweetIdToLongIndex + 2,
                """
                    invoke-static { v$tweetIdP1, v$tweetIdP2, v$nicknameRegister }, $buildShareLinkMethod
                    move-result-object v$shareLinkRegister
                """
            )

            // Restore the borrowed nicknameRegister.
            this.mutableMethod.addInstruction(
                convertTweetIdToLongIndex + 4,
                tempInstruction as BuilderInstruction
            )
            // endregion
        } ?: throw LinkResourceGetterFingerprint.exception
    }
}
