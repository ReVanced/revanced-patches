package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves to the Enum class that looks up ordinal -> instance.
 */
internal object NavigationEnumFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.STATIC or AccessFlags.CONSTRUCTOR,
    strings = listOf(
        "PIVOT_HOME",
        "TAB_SHORTS",
        "CREATION_TAB_LARGE",
        "PIVOT_SUBSCRIPTIONS",
        "TAB_ACTIVITY",
        "VIDEO_LIBRARY_WHITE",
        "INCOGNITO_CIRCLE"
    )
)
