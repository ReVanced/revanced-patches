package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object ThemeConstructorFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    listOf("L"),
    strings = listOf("settings.SettingsActivity", ":android:show_fragment", "settings.GeneralPrefsFragment")
)