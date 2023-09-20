package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.extensions.transformMethods
import app.revanced.extensions.traverseClassHierarchy
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.fingerprints.WatchWhileActivityFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.fingerprints.SwipeControlsHostActivityFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    name = "Swipe controls",
    description = "Adds volume and brightness swipe controls.",
    dependencies = [
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class,
        SwipeControlsResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object SwipeControlsBytecodePatch : BytecodePatch(
    setOf(
        WatchWhileActivityFingerprint,
        SwipeControlsHostActivityFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val wrapperClass = SwipeControlsHostActivityFingerprint.result!!.mutableClass
        val targetClass = WatchWhileActivityFingerprint.result!!.mutableClass

        // inject the wrapper class from integrations into the class hierarchy of WatchWhileActivity
        wrapperClass.setSuperClass(targetClass.superclass)
        targetClass.setSuperClass(wrapperClass.type)

        // ensure all classes and methods in the hierarchy are non-final, so we can override them in integrations
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
                    implementation
                ).toMutable()
            }
        }
    }
}