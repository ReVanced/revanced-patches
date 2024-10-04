package app.revanced.patches.reddit.customclients.syncforreddit.fix.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.video.fingerprints.ParseRedditVideoNetworkResponseFingerprint
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Fix video downloads",
    description = "Fixes a bug in Sync's MPD parser resulting in only the audio-track being saved.",
    compatiblePackages = [
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev"),
    ],
    requiresIntegrations = true,
    use = true,
)
@Suppress("unused")
object FixVideoDownloadsPatch : BytecodePatch(
    fingerprints = setOf(ParseRedditVideoNetworkResponseFingerprint),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/syncforreddit/FixRedditVideoDownloadPatch;"
    private const val GET_LINKS_METHOD = "getLinks([B)[Ljava/lang/String;"

    override fun execute(context: BytecodeContext) {
        val downloadMethod = ParseRedditVideoNetworkResponseFingerprint.resultOrThrow().mutableMethod
        val constIdx = downloadMethod.indexOfFirstInstruction { opcode == Opcode.CONST_WIDE_32 } - 2

        downloadMethod.addInstructions(constIdx, """
            new-instance v0, Lo8/h;
            iget-object v2, p1, Lcom/android/volley/NetworkResponse;->data:[B
            invoke-static { v2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->$GET_LINKS_METHOD
            move-result-object v2
            
            # videoUrl at idx 0
            const/4 v5, 0x0
            aget-object v3, v2, v5

            # audioUrl at idx 1
            const/4 v6, 0x1
            aget-object v4, v2, v6
        """.trimIndent())

        downloadMethod.removeInstructions(0, constIdx)
    }
}
