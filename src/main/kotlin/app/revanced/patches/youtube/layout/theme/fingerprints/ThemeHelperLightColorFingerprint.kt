package app.revanced.patches.youtube.layout.theme.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.AccessFlags

internal object ThemeHelperLightColorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "lightThemeResourceName" &&
                classDef.type == SettingsPatch.THEME_HELPER_DESCRIPTOR
    }
)