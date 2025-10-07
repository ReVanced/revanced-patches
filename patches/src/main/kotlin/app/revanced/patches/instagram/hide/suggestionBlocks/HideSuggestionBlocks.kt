package app.revanced.patches.instagram.hide.suggestionBlocks

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideSuggestionBlocks = bytecodePatch(
    name = "Hide suggestion blocks",
    description = "Hides suggested stories, reels, threads and other blocks from feed.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    execute {

        feedItemParseFromJsonFingerprint.let {
            listOf(
                FEED_ITEM_SUGGESTED_REELS_BLOCK_KEY,
                FEED_ITEM_SUGGESTED_STORY_BLOCK_KEY,
                FEED_ITEM_SUGGESTED_SURVEY_BLOCK_KEY,
                FEED_ITEM_SPONSORED_POST_BLOCK_KEY,
                FEED_ITEM_SUGGESTED_THREADS_BLOCK_KEY,
                FEED_ITEM_SUGGESTED_TOP_ACCOUNTS_BLOCK_KEY,
                FEED_ITEM_SUGGESTED_USERS_BLOCK_KEY,
            ).forEach { key ->
                val stringMatchIndex = it.stringMatches?.first { match -> match.string == key }!!.index

                it.method.apply {
                    val equalsCheckResultIndex = indexOfFirstInstruction(stringMatchIndex,Opcode.MOVE_RESULT)
                    val resultRegister = (getInstruction(equalsCheckResultIndex) as OneRegisterInstruction).registerA

                    // Make sure the equals check fails so that the block doesn't get parsed.
                    addInstruction(equalsCheckResultIndex+1,
                        "const v$resultRegister, 0x0")
                }
            }
        }
    }
}
