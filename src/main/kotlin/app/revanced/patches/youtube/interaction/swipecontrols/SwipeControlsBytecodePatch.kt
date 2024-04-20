package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.interaction.swipecontrols.fingerprints.SwipeControlsHostActivityFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityFingerprint
import app.revanced.util.transformMethods
import app.revanced.util.traverseClassHierarchy
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    name = "Swipe controls",
    description = "Adds options to enable and configure volume and brightness swipe controls.",
    dependencies = [
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class,
        SwipeControlsResourcePatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
            ],
        ),
    ],
)
@Suppress("unused")
object SwipeControlsBytecodePatch : BytecodePatch(
    setOf(
        MainActivityFingerprint,
        SwipeControlsHostActivityFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        val wrapperClass = SwipeControlsHostActivityFingerprint.result!!.mutableClass
        val targetClass = MainActivityFingerprint.result!!.mutableClass

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
