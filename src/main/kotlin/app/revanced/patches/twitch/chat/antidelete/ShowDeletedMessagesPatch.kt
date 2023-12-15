package app.revanced.patches.twitch.chat.antidelete

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.twitch.chat.antidelete.fingerprints.ChatUtilCreateDeletedSpanFingerprint
import app.revanced.patches.twitch.chat.antidelete.fingerprints.DeletedMessageClickableSpanCtorFingerprint
import app.revanced.patches.twitch.chat.antidelete.fingerprints.SetHasModAccessFingerprint
import app.revanced.patches.twitch.misc.integrations.IntegrationsPatch
import app.revanced.patches.twitch.misc.strings.StringsPatch
import app.revanced.patches.twitch.misc.settings.SettingsPatch

@Patch(
    name = "Show deleted messages",
    description = "Shows deleted chat messages behind a clickable spoiler.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("tv.twitch.android.app", ["15.4.1", "16.1.0", "16.9.1"])]
)
@Suppress("unused")
object ShowDeletedMessagesPatch : BytecodePatch(
    setOf(
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

    override fun execute(context: BytecodeContext) {
        // Spoiler mode: Force set hasModAccess member to true in constructor
        DeletedMessageClickableSpanCtorFingerprint.result?.mutableMethod?.apply {
            addInstructionsWithLabels(
                implementation!!.instructions.lastIndex, /* place in front of return-void */
                """
                    ${createSpoilerConditionInstructions()}
                    const/4 v0, 1
                    iput-boolean v0, p0, $definingClass->hasModAccess:Z
                """,
                ExternalLabel("no_spoiler", getInstruction(implementation!!.instructions.lastIndex))
            )
        } ?: throw DeletedMessageClickableSpanCtorFingerprint.exception

        // Spoiler mode: Disable setHasModAccess setter
        SetHasModAccessFingerprint.result?.mutableMethod?.addInstruction(0, "return-void")
            ?: throw SetHasModAccessFingerprint.exception

        // Cross-out mode: Reformat span of deleted message
        ChatUtilCreateDeletedSpanFingerprint.result?.mutableMethod?.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static {p2}, Lapp/revanced/twitch/patches/ShowDeletedMessagesPatch;->reformatDeletedMessage(Landroid/text/Spanned;)Landroid/text/Spanned;
                    move-result-object v0
                    if-eqz v0, :no_reformat
                    return-object v0
                """,
                ExternalLabel("no_reformat", getInstruction(0))
            )
        } ?: throw ChatUtilCreateDeletedSpanFingerprint.exception

        StringsPatch.includePatchStrings("ShowDeletedMessages")
        SettingsPatch.PreferenceScreen.CHAT.GENERAL.addPreferences(
            ListPreference(
                "revanced_show_deleted_messages",
                "revanced_show_deleted_messages_title",
                ArrayResource(
                    "revanced_deleted_messages_entries",
                    listOf(
                        "revanced_deleted_messages_entry_hide",
                        "revanced_deleted_messages_entry_spoiler",
                        "revanced_deleted_messages_entry_cross_out",
                    )
                ),
                ArrayResource(
                    "revanced_deleted_messages_values",
                    listOf(
                        "hide",
                        "spoiler",
                        "cross-out",
                    ),
                    literalValues = true
                ),
                default = "cross-out"
            )
        )
    }
}
