package app.revanced.patches.facebook.ads.mainfeed

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.facebook.ads.mainfeed.fingerprints.GetStoryVisibilityFingerprint
import app.revanced.patches.spotify.lite.ondemand.fingerprints.OnDemandFingerprint

@Patch(
    name = "Hide Sponsored stories",
    description = "Hide Sponsored stories in main feed.",
    compatiblePackages = [CompatiblePackage("com.facebook.katana")]
)
@Suppress("unused")
object HideSponsoredStoriesPatch : BytecodePatch(setOf(GetStoryVisibilityFingerprint)) {
    override fun execute(context: BytecodeContext) {
        OnDemandFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.startIndex
            mutableMethod?.addInstruction(insertIndex,"")


            // Hide stories with sponsored data defined
            mutableMethod.addInstruction(insertIndex, """
                    # Test cast p0 into Lcom/facebook/graphql/model/GraphQLStory
                    instance-of v0, p0, Lcom/facebook/graphql/model/GraphQLStory;
                    # if 0 return to normal logic
                    if-eqz v0, :resume_normal
                    # We have a graphQLStory, check if a call on A1G() returns null (return type : LX/3ba)
                    invoke-virtual {p0}, Lcom/facebook/graphql/model/GraphQLStory;->A1G()LX/3ba;
                    move-result-object v0
                    # if non-null, hide ad    
                    if-eqz v0, :resume_normal
                    # Return Ljava/lang/Integer; 3 (StoryVisibility.GONE)
                    const/4 v0, 0x3
                    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object p0 
                    return-object p0
            """.trimIndent())
        } ?: throw GetStoryVisibilityFingerprint.exception
    }
}