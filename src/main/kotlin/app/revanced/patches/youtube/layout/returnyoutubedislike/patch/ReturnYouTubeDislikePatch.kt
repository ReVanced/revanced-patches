package app.revanced.patches.youtube.layout.returnyoutubedislike.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.TextComponentSpecParentFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.DislikeFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.LikeFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RemoveLikeFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.videoid.patch.VideoIdPatch

@Patch
@Dependencies([IntegrationsPatch::class, VideoIdPatch::class])
@Name("return-youtube-dislike")
@Description("Shows the dislike count of videos using the Return YouTube Dislike API.")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
class ReturnYouTubeDislikePatch : BytecodePatch(
    listOf(
        TextComponentSpecParentFingerprint, LikeFingerprint, DislikeFingerprint, RemoveLikeFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        LikeFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, 1
            invoke-static {v0}, Lapp/revanced/integrations/patches/ReturnYouTubeDislikesPatch;->sendVote(I)V
            """
        )
        DislikeFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, -1
            invoke-static {v0}, Lapp/revanced/integrations/patches/ReturnYouTubeDislikesPatch;->sendVote(I)V
            """
        )
        RemoveLikeFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, 0
            invoke-static {v0}, Lapp/revanced/integrations/patches/ReturnYouTubeDislikesPatch;->sendVote(I)V
            """
        )

        VideoIdPatch.injectCall("Lapp/revanced/integrations/patches/ReturnYouTubeDislikesPatch;->newVideoLoaded(Ljava/lang/String;)V")

        val parentResult = TextComponentSpecParentFingerprint.result!!
        val createComponentMethod = parentResult.mutableClass.methods.find { method ->
                method.parameters.size >= 19 && method.parameterTypes.takeLast(4)
                    .all { param -> param == "Ljava/util/concurrent/atomic/AtomicReference;" }
            }
            ?: return PatchResultError("TextComponentSpec.createComponent not found")

        val conversionContextParam = 5
        val textRefParam = createComponentMethod.parameters.size - 2

        createComponentMethod.addInstructions(
            0,
            """
            move-object/from16 v0, p$conversionContextParam
            move-object/from16 v1, p$textRefParam
            invoke-static {v0, v1}, Lapp/revanced/integrations/patches/ReturnYouTubeDislikesPatch;->onComponentCreated(Ljava/lang/Object;Ljava/util/concurrent/atomic/AtomicReference;)V
            """
        )

        return PatchResultSuccess()
    }
}
