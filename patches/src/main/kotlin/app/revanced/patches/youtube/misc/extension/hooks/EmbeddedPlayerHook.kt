package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For embedded playback inside the Google app (such as the in-app 'discover' tab).
 *
 * Note: This hook may or may not be needed, as
 * [remoteEmbedFragmentHook] might be set before this is called.
 */
internal val embeddedPlayerHook = extensionHook(
    // Extension context is the third method parameter.
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size + 2 },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L", "L", "Landroid/content/Context;")
    strings("android.hardware.type.television") // String is also found in other classes.
}
