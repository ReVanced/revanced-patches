package app.revanced.patches.twitch.chat.antidelete.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.twitch.chat.antidelete.annotations.ShowDeletedMessagesCompatibility
import app.revanced.patches.twitch.chat.antidelete.fingerprints.*
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("show-deleted-messages")
@Description("Shows deleted chat messages behind a clickable spoiler.")
@ShowDeletedMessagesCompatibility
@Version("0.0.1")
class ShowDeletedMessagesPatch : BytecodePatch(
    listOf(
        SetHasModAccessFingerprint,
        DeletedMessageClickableSpanCtorFingerprint,
        ChatUtilCreateDeletedSpanFingerprint
    )
) {
    private fun createSpoilerConditionInstructions(register: String = "v0") = """
        invoke-static {}, Lapp/revanced/twitch/patches/ShowDeletedMessagesPatch;->shouldUseSpoiler()Z
        move-result $register
        if-eqz $register, :no_spoiler
    """

    override fun execute(context: BytecodeContext): PatchResult {
        // Spoiler mode: Force set hasModAccess member to true in constructor
        with(DeletedMessageClickableSpanCtorFingerprint.result!!.mutableMethod) {
            addInstructions(
                implementation!!.instructions.lastIndex, /* place in front of return-void */
                """
                    ${createSpoilerConditionInstructions()}
                    const/4 v0, 1
                    iput-boolean v0, p0, $definingClass->hasModAccess:Z
                """,
                listOf(ExternalLabel("no_spoiler", instruction(implementation!!.instructions.lastIndex)))
            )
        }

        // Spoiler mode: Disable setHasModAccess setter
        with(SetHasModAccessFingerprint.result!!) {
            mutableMethod.addInstruction(0, "return-void")
        }

        // Cross-out mode: Reformat span of deleted message
        with(ChatUtilCreateDeletedSpanFingerprint.result!!) {
            mutableMethod.addInstructions(
                0,
                """
                    invoke-static {p2}, Lapp/revanced/twitch/patches/ShowDeletedMessagesPatch;->reformatDeletedMessage(Landroid/text/Spanned;)Landroid/text/Spanned;
                    move-result-object v0
                    if-eqz v0, :no_reformat
                    return-object v0
                """,
                listOf(ExternalLabel("no_reformat", mutableMethod.instruction(0)))
            )
        }

        SettingsPatch.PreferenceScreen.CHAT.GENERAL.addPreferences(
            ListPreference(
                "revanced_show_deleted_messages",
                StringResource(
                    "revanced_show_deleted_messages_title",
                    "Show deleted messages"
                ),
                ArrayResource(
                    "revanced_deleted_messages",
                    listOf(
                        StringResource("revanced_deleted_messages_hide", "Do not show deleted messages"),
                        StringResource("revanced_deleted_messages_spoiler", "Hide deleted messages behind a spoiler"),
                        StringResource("revanced_deleted_messages_cross_out", "Show deleted messages as crossed-out text")
                    )
                ),
                ArrayResource(
                    "revanced_deleted_messages_values",
                    listOf(
                        StringResource("key_revanced_deleted_messages_hide", "hide"),
                        StringResource("key_revanced_deleted_messages_spoiler", "spoiler"),
                        StringResource("key_revanced_deleted_messages_cross_out", "cross-out")
                    )
                ),
                "cross-out"
            )
        )

        SettingsPatch.addString("revanced_deleted_msg", "message deleted")

        return PatchResultSuccess()
    }
}
