package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideExportFeedPatch = bytecodePatch(
    name = "Hide explore feed",
    description = "Hides posts and reels from the explore/search page.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        exploreResponseJsonParserFingerprint.method.apply {
            val sectionalItemStringIndex = exploreResponseJsonParserFingerprint.stringMatches!!.first().index
            val sectionalItemStringRegister = getInstruction<OneRegisterInstruction>(sectionalItemStringIndex).registerA

            /**
             * Replacing the JSON key we want to skip with a random string that is not a valid JSON key.
             * This way the feeds array will never be populated.
             * Received JSON keys that are not handled are simply ignored, so there are no side effects.
             */
            replaceInstruction(
                sectionalItemStringIndex,
                "const-string v$sectionalItemStringRegister, \"BOGUS\""
            )
        }
    }
}

