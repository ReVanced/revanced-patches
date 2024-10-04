package app.revanced.patches.reddit.customclients.syncforreddit.fix.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.video.fingerprints.ParseRedditVideoNetworkResponseFingerprint
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

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

        // Find the instruction which creates the result data class
        val newInstanceIdx = downloadMethod.indexOfFirstInstructionOrThrow {
            opcode == Opcode.NEW_INSTANCE && (this as ReferenceInstruction).reference.toString() == "Lo8/h;"
        }

        // Remove all instructions up to that point, we don't need them
        downloadMethod.removeInstructions(0, newInstanceIdx)

        // new-instance instruction from above is now at idx 0, add patch directly after it
        downloadMethod.addInstructions(1, """
            # Get byte array from response
            iget-object v2, p1, Lcom/android/volley/NetworkResponse;->data:[B
            # Call integration method, move result (which is a string array: [videoUrl?, audioUrl?]) to v2
            invoke-static { v2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->$GET_LINKS_METHOD
            move-result-object v2
            
            # Get videoUrl from idx 0 and move it into v3
            const/4 v5, 0x0
            aget-object v3, v2, v5

            # Get audioUrl from idx 0 and move it into v4
            const/4 v6, 0x1
            aget-object v4, v2, v6
            
            # We are done, the following method instructions use v3 and v4 to build the response
        """.trimIndent())
    }
}
