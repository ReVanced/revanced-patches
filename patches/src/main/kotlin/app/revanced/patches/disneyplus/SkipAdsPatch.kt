package app.revanced.patches.disneyplus

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val skipAdsPatch = bytecodePatch(
    name = "Skip ads",
    description = "Automatically skips ads.",
) {
    compatibleWith("com.disney.disneyplus")

    apply {
        arrayOf(insertionGetPointsMethod, insertionGetRangesMethod).forEach {
            it.addInstructions(
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
