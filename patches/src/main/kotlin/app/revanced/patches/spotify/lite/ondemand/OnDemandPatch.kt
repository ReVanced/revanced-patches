package app.revanced.patches.spotify.lite.ondemand

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val onDemandPatch = bytecodePatch(
    name = "Enable on demand",
    description = "Enables listening to songs on-demand, allowing to play any song from playlists, albums or artists without limitations. This does not remove ads.",
) {
    compatibleWith("com.spotify.lite")

    execute {
        // Spoof a premium account

        onDemandFingerprint.method.addInstruction(
            onDemandFingerprint.filterMatches.last().index - 1,
            "const/4 v0, 0x2",
        )
    }
}
