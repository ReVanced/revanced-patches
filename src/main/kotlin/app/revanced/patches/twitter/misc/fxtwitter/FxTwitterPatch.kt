package app.revanced.patches.twitter.misc.fxtwitter

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import app.revanced.patches.twitter.misc.fxtwitter.fingerprints.FxTwitterPatchFingerprint
import app.revanced.patches.twitter.misc.fxtwitter.fingerprints.FxTwitterPatchFingerprintResource
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import java.util.logging.Logger


@Patch(
    name = "FxTwitter",
    description = "Replaces the default Twitter share link with FxTwitter.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object FxTwitterPatch : BytecodePatch(
    setOf(FxTwitterPatchFingerprint, FxTwitterPatchFingerprintResource)
) {
    private var shareLinkBase by stringPatchOption(
        key = "twitter_share_link_base",
        default = "fxtwitter.com",
        title = "Embed builder domain",
        description = "The embed builder domain used to share the twitter links. (Default: fxtwitter.com) known alts: vxtwitter.com, fixupx.com.",
    )

    override fun execute(context: BytecodeContext) {
        var twitterGetShareLinkMethod = FxTwitterPatchFingerprint.result?.apply {
            val instructions = this.mutableMethod.getInstructions()

            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.CONST_STRING) {
                    val string = ((instruction as ReferenceInstruction).reference as StringReference).string

                    if (string.contains("https://twitter.com/%1\$s/status/%2\$d") || string.contains("https://x.com/%1\$s/status/%2\$d")) {
                        val overrideRegister = (instruction as OneRegisterInstruction).registerA

                        // Totally efficient
                        var overrideString = string.replace("twitter.com", shareLinkBase.toString())
                        overrideString = overrideString.replace("x.com", shareLinkBase.toString())
                        this.mutableMethod.replaceInstruction(
                            index,
                            """
                                const-string v$overrideRegister, "$overrideString"
                            """
                        )
                    }
                }
            }
        } ?: throw PatchException("FxTwitterPatchFingerprint not found")


        FxTwitterPatchFingerprintResource.result?.apply {
            val instructions = this.mutableMethod.getInstructions()

            // Save user nickname to v9
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {

                    val methodRef =
                        (instruction as ReferenceInstruction).reference as MethodReference
                    if (
                        methodRef.returnType != "Ljava/lang/String;") continue;

                    this.mutableMethod.addInstruction(
                        index + 2,
                        """
                            move-object v9, v3
                        """
                    )

                    break;
                }
            }

            // Save tweet id to v7, v8
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {

                    val methodRef =
                        (instruction as ReferenceInstruction).reference as MethodReference
                    if (methodRef.definingClass != "Ljava/lang/Long;") continue;

                    this.mutableMethod.addInstruction(
                        index + 2,
                        """
                            move-wide v7, v5
                        """
                    )
                    break;
                }
            }

            // Call the method patched by FxTwitterPatchFingerprint
            for ((index, instruction) in instructions.withIndex()) {
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL) {

                    val methodRef =
                        (instruction as ReferenceInstruction).reference as MethodReference
                    if (methodRef.definingClass != "Landroid/content/res/Resources;") continue;

                    this.mutableMethod.replaceInstruction(
                        index,
                        """
                            invoke-static { v7, v8, v9 }, ${twitterGetShareLinkMethod.method}
                        """
                    )
                    break;
                }
            }
        }
    }
}
