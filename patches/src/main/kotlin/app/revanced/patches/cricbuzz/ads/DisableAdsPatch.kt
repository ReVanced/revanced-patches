package app.revanced.patches.cricbuzz.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.cricbuzz.misc.extension.sharedExtensionPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAdsPatch = bytecodePatch (
    name = "Hide ads",
) {
    compatibleWith("com.cricbuzz.android"("6.23.02"))

    dependsOn(sharedExtensionPatch)

    execute {
        userStateSwitchFingerprint.method.returnEarly(true)

        // Remove region-specific Cricbuzz11 elements.
        cb11ConstructorFingerprint.method.addInstruction(0, "const/4 p7, 0x0")
        getBottomBarFingerprint.method.addInstruction(1,
            "invoke-static { v0 }, Lapp/revanced/extension/cricbuzz/ads/DisableAdsPatch;->filterCb11(Ljava/util/List;)V")
    }
}
