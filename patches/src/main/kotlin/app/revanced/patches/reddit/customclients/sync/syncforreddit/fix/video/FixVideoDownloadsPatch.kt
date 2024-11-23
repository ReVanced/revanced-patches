package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/syncforreddit/FixRedditVideoDownloadPatch;"
private const val GET_LINKS_METHOD = "getLinks([B)[Ljava/lang/String;"

@Suppress("unused")
val fixVideoDownloadsPatch = bytecodePatch(
    name = "Fix video downloads",
    description = "Fixes a bug in Sync's MPD parser resulting in only the audio-track being saved.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    execute {
        val scanResult = parseRedditVideoNetworkResponseFingerprint.patternMatch!!
        val newInstanceIndex = scanResult.startIndex
        val invokeDirectIndex = scanResult.endIndex - 1

        val buildResponseInstruction =
            parseRedditVideoNetworkResponseFingerprint.method.getInstruction<Instruction35c>(invokeDirectIndex)

        parseRedditVideoNetworkResponseFingerprint.method.addInstructions(
            newInstanceIndex + 1,
            """
                # Get byte array from response.
                iget-object v2, p1, Lcom/android/volley/NetworkResponse;->data:[B
                    
                # Parse the videoUrl and audioUrl from the byte array.
                invoke-static { v2 }, $EXTENSION_CLASS_DESCRIPTOR->$GET_LINKS_METHOD
                move-result-object v2
        
                # Get videoUrl (Index 0).
                const/4 v5, 0x0
                aget-object v${buildResponseInstruction.registerE}, v2, v5
        
                # Get audioUrl (Index 1).
                const/4 v6, 0x1
                aget-object v${buildResponseInstruction.registerF}, v2, v6

                # Register E and F are used to build the response.
            """,
        )
    }
}
