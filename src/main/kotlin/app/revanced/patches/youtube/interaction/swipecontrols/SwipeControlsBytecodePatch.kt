package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.interaction.swipecontrols.fingerprints.swipeControlsHostActivityFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.shared.fingerprints.mainActivityFingerprint
import app.revanced.util.transformMethods
import app.revanced.util.traverseClassHierarchy
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Suppress("unused")
val swipeControlsBytecodePatch = bytecodePatch(
    name = "Swipe controls",
    description = "Adds options to enable and configure volume and brightness swipe controls.",
) {
    dependsOn(
        integrationsPatch,
        playerTypeHookPatch,
        swipeControlsResourcePatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43", // 19.12.x has an issue with opening YT using external links,
            // and the app then crashes if double tap to skip forward/back is immediately used.
            // The stack trace shows a call coming from integrations SwipeController,
            // but it may be a bug in YT itself as other target versions do not have this issue.
        ),
    )

    val mainActivityResult by mainActivityFingerprint
    val swipeControlsHostActivityResult by swipeControlsHostActivityFingerprint

    execute { context ->
        val wrapperClass = swipeControlsHostActivityResult.mutableClass
        val targetClass = mainActivityResult.mutableClass

        // Inject the wrapper class from integrations into the class hierarchy of MainActivity.
        wrapperClass.setSuperClass(targetClass.superclass)
        targetClass.setSuperClass(wrapperClass.type)

        // Ensure all classes and methods in the hierarchy are non-final, so we can override them in integrations.
        context.traverseClassHierarchy(targetClass) {
            accessFlags = accessFlags and AccessFlags.FINAL.value.inv()
            transformMethods {
                ImmutableMethod(
                    definingClass,
                    name,
                    parameters,
                    returnType,
                    accessFlags and AccessFlags.FINAL.value.inv(),
                    annotations,
                    hiddenApiRestrictions,
                    implementation,
                ).toMutable()
            }
        }
    }
}
