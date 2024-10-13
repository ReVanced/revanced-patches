package app.revanced.patches.reddit.customclients

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Option
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption

/**
 * Base class for patches that spoof the Reddit client.
 *
 * @param redirectUri The redirect URI of the Reddit OAuth client.
 * @param block The patch block. It is called with the client ID option.
 */
fun spoofClientPatch(
    redirectUri: String,
    block: BytecodePatchBuilder.(Option<String>) -> Unit = {},
) = bytecodePatch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID.",
) {
    block(
        stringOption(
            "client-id",
            null,
            null,
            "OAuth client ID",
            "The Reddit OAuth client ID. " +
                "You can get your client ID from https://www.reddit.com/prefs/apps. " +
                "The application type has to be \"Installed app\" " +
                "and the redirect URI has to be set to \"$redirectUri\".",
            true,
        ),
    )
}
