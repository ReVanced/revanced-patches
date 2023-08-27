package app.revanced.patches.youtube.interaction.swipecontrols.patch.bytecode

import app.revanced.extensions.transformMethods
import app.revanced.extensions.traverseClassHierarchy
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.fingerprints.WatchWhileActivityFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility
import app.revanced.patches.youtube.interaction.swipecontrols.fingerprints.SwipeControlsHostActivityFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.patch.resource.SwipeControlsResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch
@Name("Swipe controls")
@Description("Adds volume and brightness swipe controls.")
@SwipeControlsCompatibility
@DependsOn(
    [
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class,
        SwipeControlsResourcePatch::class
    ]
)
class SwipeControlsBytecodePatch : BytecodePatch(
    listOf(
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