package app.revanced.patches.music.utils.returnyoutubedislike

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.returnyoutubedislike.fingerprints.DislikeFingerprint
import app.revanced.patches.music.utils.returnyoutubedislike.fingerprints.LikeFingerprint
import app.revanced.patches.music.utils.returnyoutubedislike.fingerprints.RemoveLikeFingerprint
import app.revanced.patches.music.utils.returnyoutubedislike.fingerprints.TextComponentFingerprint
import app.revanced.patches.music.video.videoid.VideoIdPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

@Patch(
    dependencies = [
        SharedResourceIdPatch::class,
        VideoIdPatch::class
    ]
)
object ReturnYouTubeDislikeBytecodePatch : BytecodePatch(
    setOf(
        DislikeFingerprint,
        LikeFingerprint,
        RemoveLikeFingerprint,
        TextComponentFingerprint
    )
) {
    private const val INTEGRATIONS_RYD_CLASS_DESCRIPTOR =
        "$UTILS_PATH/ReturnYouTubeDislikePatch;"

    override fun execute(context: BytecodeContext) {
        setOf(
            LikeFingerprint.toPatch(Vote.LIKE),
            DislikeFingerprint.toPatch(Vote.DISLIKE),
            RemoveLikeFingerprint.toPatch(Vote.REMOVE_LIKE)
        ).forEach { (fingerprint, vote) ->
            with(fingerprint.result ?: throw fingerprint.exception) {
                mutableMethod.addInstructions(
                    0,
                    """
                    const/4 v0, ${vote.value}
                    invoke-static {v0}, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->sendVote(I)V
                    """
                )
            }
        }

        TextComponentFingerprint.result?.let {
            it.mutableMethod.apply {
                var insertIndex = -1
                for ((index, instruction) in implementation!!.instructions.withIndex()) {
                    if (instruction.opcode != Opcode.INVOKE_STATIC) continue

                    val reference = getInstruction<Instruction35c>(index).reference.toString()
                    if (!reference.endsWith("Ljava/lang/CharSequence;") && !reference.endsWith("Landroid/text/Spanned;")) continue

                    val insertRegister = getInstruction<OneRegisterInstruction>(index + 1).registerA

                    insertIndex = index + 2

                    addInstructions(
                        insertIndex, """
                            invoke-static {v$insertRegister}, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->onComponentCreated(Landroid/text/Spanned;)Landroid/text/Spanned;
                            move-result-object v$insertRegister
                            """
                    )

                    break
                }
                if (insertIndex == -1)
                    throw PatchException("target Instruction not found!")
            }
        } ?: throw TextComponentFingerprint.exception

        VideoIdPatch.hookVideoId("$INTEGRATIONS_RYD_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

    }

    private fun MethodFingerprint.toPatch(voteKind: Vote) = VotePatch(this, voteKind)

    private data class VotePatch(val fingerprint: MethodFingerprint, val voteKind: Vote)

    private enum class Vote(val value: Int) {
        LIKE(1),
        DISLIKE(-1),
        REMOVE_LIKE(0)
    }
}
