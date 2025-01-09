package app.revanced.patches.spotify.navbar

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags

internal val addNavBarItemFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        resourceLiteral("bool", "show_bottom_navigation_items_text")
    )
}
