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
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
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
        fun Method.indexOfFirstOrThrow(errorMessage: String, predicate: Instruction.() -> Boolean) =
            indexOfFirstInstruction(predicate).also {
                if (it == -1) throw PatchException(errorMessage)
            }

        // region Replace the domain name when copying a link with the button.

        val buildShareLinkMethod = LinkBuilderFingerprint.result?.let {
            it.mutableMethod.apply {
                val linkStringIndex = it.scanResult.stringsScanResult!!.matches.first().index
                val targetRegister = getInstruction<OneRegisterInstruction>(linkStringIndex).registerA

                replaceInstruction(
                    linkStringIndex,
                    "const-string v$targetRegister, \"https://$domainName/%1\$s/status/%2\$d\""
                )
            }
        } ?: throw LinkBuilderFingerprint.exception

        // endregion

        // Used in the Share via... dialog.
        LinkResourceGetterFingerprint.result?.mutableMethod?.apply {
            // region Remove the original call to the method that uses the resources template to build the link.

            val originalMethodIndex = indexOfFirstOrThrow("Could not find originalMethodIndex") {
                getReference<MethodReference>()?.definingClass == "Landroid/content/res/Resources;"
            }

            val shareLinkRegister = getInstruction<OneRegisterInstruction>(
                // Offset to instruction that saves the result of the method call to the register.
                originalMethodIndex + 1
            ).registerA

            // Remove original method call and instruction to move the result to the shareLinkRegister.
            removeInstructions(originalMethodIndex, 2)

            // Remove the instruction that uses the shareLinkRegister as an array reference to prevent an error.
            // Original SMALI aput-object v3, v1, v5 (v1 is the shareLinkRegister)
            removeInstructions(originalMethodIndex - 2, 1)
            // endregion

            // region Get the nickname of the user that posted the tweet. This is used to build the link.

            val getNicknameIndex = indexOfFirstOrThrow("Could not find getNicknameIndex") {
                getReference<MethodReference>().toString().endsWith("Ljava/lang/String;")
            }

            val sourceNicknameRegister = getInstruction<OneRegisterInstruction>(
                // Offset to instruction that saves the result of the method call to the register.
                getNicknameIndex + 1
            ).registerA

            // Instruction with a spare register that can be used to store the user nickname.
            val tempInstructionIndex = indexOfFirstOrThrow("Could not find tempInstructionIndex") {
                opcode == Opcode.IGET_OBJECT
            }.also {
                if (it > getNicknameIndex) throw PatchException(
                    "tempInstructionIndex > getNicknameIndex, this indicates that the instruction was moved."
                )
            }

            val tempInstruction = getInstruction<TwoRegisterInstruction>(tempInstructionIndex)

            // Save the user nickname to the register that was used to store "this".
            val tempNicknameRegister = tempInstruction.registerA
            addInstruction(
                // This offset is used to place the instruction after sourceNicknameRegister is filled with data.
                getNicknameIndex + 2,
                "move-object v${tempNicknameRegister}, v$sourceNicknameRegister"
            )

            // endregion

            // region Call the patched method and save the result to shareLinkRegister.

            val convertTweetIdToLongIndex = indexOfFirstOrThrow("Could not find convertTweetIdToLongIndex") {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.definingClass == "Ljava/lang/Long;"
            }

            val tweetIdP1 = getInstruction<OneRegisterInstruction>(convertTweetIdToLongIndex + 1).registerA
            val tweetIdP2 = tweetIdP1 + 1

            addInstructions(
                // This offset is used to place the instruction after the save of the method result.
                convertTweetIdToLongIndex + 2,
                """
                    invoke-static { v$tweetIdP1, v$tweetIdP2, v$tempNicknameRegister }, $buildShareLinkMethod
                    move-result-object v$shareLinkRegister
                """
            )

            // Restore the borrowed nicknameRegister.
            addInstruction(
                convertTweetIdToLongIndex + 4,
                tempInstruction as BuilderInstruction
            )

            // endregion
        } ?: throw LinkResourceGetterFingerprint.exception
    }
}
