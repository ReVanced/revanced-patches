package app.revanced.patches.youtube.utils.returnyoutubedislike.general

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.playerresponse.PlayerResponsePatch
import app.revanced.patches.youtube.utils.returnyoutubedislike.general.fingerprints.DislikeFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.general.fingerprints.LikeFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.general.fingerprints.RemoveLikeFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.general.fingerprints.TextComponentConstructorFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.general.fingerprints.TextComponentContextFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.oldlayout.ReturnYouTubeDislikeOldLayoutPatch
import app.revanced.patches.youtube.utils.returnyoutubedislike.rollingnumber.ReturnYouTubeDislikeRollingNumberPatch
import app.revanced.patches.youtube.utils.returnyoutubedislike.shorts.ReturnYouTubeDislikeShortsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.videoid.general.VideoIdPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Return YouTube Dislike",
    description = "Shows the dislike count of videos using the Return YouTube Dislike API.",
    dependencies = [
        LithoFilterPatch::class,
        PlayerResponsePatch::class,
        ReturnYouTubeDislikeOldLayoutPatch::class,
        ReturnYouTubeDislikeRollingNumberPatch::class,
        ReturnYouTubeDislikeShortsPatch::class,
        SettingsPatch::class,
        VideoIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ReturnYouTubeDislikePatch : BytecodePatch(
    setOf(
        DislikeFingerprint,
        LikeFingerprint,
        RemoveLikeFingerprint,
        TextComponentConstructorFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        listOf(
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


        TextComponentConstructorFingerprint.result?.let { parentResult ->
            // Resolves fingerprints
            TextComponentContextFingerprint.resolve(context, parentResult.classDef)

            TextComponentContextFingerprint.result?.let {
                it.mutableMethod.apply {
                    val conversionContextFieldIndex = implementation!!.instructions.indexOfFirst { instruction ->
                        instruction.opcode == Opcode.IGET_OBJECT
                                && instruction.getReference<FieldReference>()?.type == "Ljava/util/Map;"
                    } - 1
                    val conversionContextFieldReference =
                        getInstruction<ReferenceInstruction>(conversionContextFieldIndex).reference

                    val charSequenceIndex = implementation!!.instructions.indexOfFirst { instruction ->
                        instruction.opcode == Opcode.IGET_OBJECT
                                && instruction.getReference<FieldReference>()?.type == "Ljava/util/BitSet;"
                    } - 1
                    val charSequenceRegister = getInstruction<TwoRegisterInstruction>(charSequenceIndex).registerA
                    val freeRegister = getInstruction<TwoRegisterInstruction>(charSequenceIndex).registerB

                    addInstructions(
                        charSequenceIndex - 1, """
                            move-object/from16 v$freeRegister, p0
                            iget-object v$freeRegister, v$freeRegister, $conversionContextFieldReference
                            invoke-static {v$freeRegister, v$charSequenceRegister}, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                            move-result-object v$charSequenceRegister
                            """
                    )
                }
            } ?: throw TextComponentContextFingerprint.exception
        } ?: throw TextComponentConstructorFingerprint.exception

        VideoIdPatch.injectCall("$INTEGRATIONS_RYD_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")
        VideoIdPatch.injectPlayerResponseVideoId("$INTEGRATIONS_RYD_CLASS_DESCRIPTOR->preloadVideoId(Ljava/lang/String;Z)V")

        if (SettingsPatch.upward1834) {
            LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
            VideoIdPatch.injectPlayerResponseVideoId("$FILTER_CLASS_DESCRIPTOR->newPlayerResponseVideoId(Ljava/lang/String;Z)V")
        }

        /**
         * Add ReVanced Extended Settings
         */
        SettingsPatch.addReVancedPreference("ryd_settings")

        SettingsPatch.updatePatchStatus("Return YouTube Dislike")

    }

    private const val INTEGRATIONS_RYD_CLASS_DESCRIPTOR =
        "$UTILS_PATH/ReturnYouTubeDislikePatch;"

    private const val FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/ReturnYouTubeDislikeFilterPatch;"

    private fun MethodFingerprint.toPatch(voteKind: Vote) = VotePatch(this, voteKind)

    private data class VotePatch(val fingerprint: MethodFingerprint, val voteKind: Vote)

    private enum class Vote(val value: Int) {
        LIKE(1),
        DISLIKE(-1),
        REMOVE_LIKE(0)
    }
}
