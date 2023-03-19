package app.revanced.patches.youtube.layout.returnyoutubedislike.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.*
import app.revanced.patches.youtube.layout.returnyoutubedislike.resource.patch.ReturnYouTubeDislikeResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        VideoIdPatch::class,
        ReturnYouTubeDislikeResourcePatch::class,
        PlayerTypeHookPatch::class,
    ]
)
@Name("return-youtube-dislike")
@Description("Shows the dislike count of videos using the Return YouTube Dislike API.")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
class ReturnYouTubeDislikePatch : BytecodePatch(
    listOf(
        TextComponentConstructorFingerprint,
        ShortsTextComponentParentFingerprint,
        LikeFingerprint,
        DislikeFingerprint,
        RemoveLikeFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // region Inject newVideoLoaded event handler

        VideoIdPatch.injectCall("$INTEGRATIONS_PATCH_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

        // endregion

        // region Hook interaction

        listOf(
            LikeFingerprint.toPatch(Vote.LIKE),
            DislikeFingerprint.toPatch(Vote.DISLIKE),
            RemoveLikeFingerprint.toPatch(Vote.REMOVE_LIKE)
        ).forEach { (fingerprint, vote) ->
            fingerprint.result?.mutableMethod?.apply {
                addInstructions(
                    0,
                    """
                    const/4 v0, ${vote.value}
                    invoke-static {v0}, $INTEGRATIONS_PATCH_CLASS_DESCRIPTOR->sendVote(I)V
                    """
                )
            } ?: return PatchResultError("Failed to find ${fingerprint.name} method.")
        }

        // endregion

        // region Hook components

        TextReferenceFingerprint.also {
            it.resolve(
                context,
                TextComponentConstructorFingerprint.result!!.classDef
            )
        }.result?.let { result ->
            val moveTextRefParamInstructionIndex = TextReferenceParamFingerprint.also {
                if (!TextReferenceParamFingerprint.resolve(context, result.method, result.classDef))
                    return TextReferenceParamFingerprint.toErrorResult()
            }.result!!.scanResult.patternScanResult!!.endIndex

            result.mutableMethod.apply {
                val insertIndex = result.scanResult.patternScanResult!!.endIndex

                val atomicReferenceInstruction = (instruction(insertIndex - 1) as FiveRegisterInstruction)
                val conversionContextParam = atomicReferenceInstruction.registerC
                val textRefParam = (instruction(moveTextRefParamInstructionIndex) as TwoRegisterInstruction).registerA

                // Overwritten after injected code, which is why it can be used.
                val clobberRegister = atomicReferenceInstruction.registerD

                addInstructions(
                    insertIndex,
                    """
                        # required instruction, otherwise register might be out of range
                        move-object/from16 v$clobberRegister, v$textRefParam
                        invoke-static {v$clobberRegister, v$conversionContextParam}, $ON_COMPONENT_CREATED_DESCRIPTOR
                    """
                )
            }
        } ?: return TextReferenceFingerprint.toErrorResult()

        ShortsTextComponentParentFingerprint.result?.let {
            context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                .getMethod().let { method ->
                    with(method as MutableMethod) {
                        // After walking, verify the found method is what's expected.
                        if (returnType != ("Ljava/lang/CharSequence;") || parameterTypes.size != 1)
                            return PatchResultError("Method signature did not match: $this $parameterTypes")

                        val insertIndex = implementation!!.instructions.size - 1

                        val spannedParameterRegister = (instruction(insertIndex) as OneRegisterInstruction).registerA
                        val parameter = (instruction(insertIndex - 2) as BuilderInstruction35c).reference

                        if (!parameter.toString().endsWith("Landroid/text/Spanned;"))
                            return PatchResultError("Method signature parameter did not match: $parameter")

                        addInstructions(
                            insertIndex,
                            """
                                invoke-static {v$spannedParameterRegister}, $INTEGRATIONS_PATCH_CLASS_DESCRIPTOR->onShortsComponentCreated(Landroid/text/Spanned;)Landroid/text/Spanned;
                                move-result-object v$spannedParameterRegister
                            """
                        )
                    }
                }
        } ?: return ShortsTextComponentParentFingerprint.toErrorResult()

        // endregion

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_PATCH_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/ReturnYouTubeDislikePatch;"

        const val ON_COMPONENT_CREATED_DESCRIPTOR =
            "$INTEGRATIONS_PATCH_CLASS_DESCRIPTOR->onComponentCreated(Ljava/lang/Object;Ljava/util/concurrent/atomic/AtomicReference;)V"

        private fun MethodFingerprint.toPatch(voteKind: Vote) = VotePatch(this, voteKind)
    }

    private data class VotePatch(val fingerprint: MethodFingerprint, val voteKind: Vote)

    private enum class Vote(val value: Int) {
        LIKE(1),
        DISLIKE(-1),
        REMOVE_LIKE(0)
    }
}
