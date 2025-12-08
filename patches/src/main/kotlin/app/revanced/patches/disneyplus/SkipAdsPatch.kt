package app.revanced.patches.disneyplus.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val skipAdsPatch = bytecodePatch(
    name = "Skip ads",
    description = "Automatically skips ads.",
) {
    compatibleWith("com.disney.disneyplus")

    execute {
        arrayOf(insertionGetPointsFingerprint, insertionGetRangesFingerprint).forEach {
            it.method.addInstructions(
	            0,
	            """
	                new-instance v0, Ljava/util/ArrayList;
	                invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
	                return-object v0
	            """,
            )
        }
    }
}
