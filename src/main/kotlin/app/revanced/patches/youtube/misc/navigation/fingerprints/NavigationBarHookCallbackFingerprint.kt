package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.NavigationButtonsPatch
import app.revanced.patches.youtube.misc.navigation.INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Integrations method, used for callback into to other patches.
 * Specifically, [NavigationButtonsPatch].
 */
internal val navigationBarHookCallbackFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("V")
    parameters(INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR, "Landroid/view/View;")
    custom { methodDef, classDef ->
        methodDef.name == "navigationTabCreatedCallback" &&
            classDef == INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR
    }
}
