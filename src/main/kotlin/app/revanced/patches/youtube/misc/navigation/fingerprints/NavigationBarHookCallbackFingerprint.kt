package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.NavigationButtonsPatch
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookPatch
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Integrations method, used for callback into to other patches.
 * Specifically, [NavigationButtonsPatch].
 */
internal object NavigationBarHookCallbackFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    returnType = "V",
    parameters = listOf(NavigationBarHookPatch.INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR, "Landroid/view/View;"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "navigationTabCreatedCallback" &&
            methodDef.definingClass == NavigationBarHookPatch.INTEGRATIONS_CLASS_DESCRIPTOR
    },
)
