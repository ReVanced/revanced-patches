package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.navigation.fingerprints.ActionBarSearchResultsFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.InitializeButtonsFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.NavigationBarHookCallbackFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.NavigationEnumFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarButtonsCreateDrawableViewFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarButtonsCreateResourceViewFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarConstructorFingerprint
import app.revanced.patches.youtube.misc.navigation.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.misc.navigation.utils.InjectionUtils.injectHooksByFilter
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    description = "Hook to get the current navigation bar tab that is active, and if the search bar is active.",
    dependencies = [
        IntegrationsPatch::class,
        NavigationBarHookResourcePatch::class
    ]
)
@Suppress("unused")
object NavigationBarHookPatch : BytecodePatch(
    setOf(
        PivotBarConstructorFingerprint,
        NavigationEnumFingerprint,
        PivotBarButtonsCreateDrawableViewFingerprint,
        PivotBarButtonsCreateResourceViewFingerprint,
        NavigationBarHookCallbackFingerprint,
        ActionBarSearchResultsFingerprint
    )
) {
    internal const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/shared/NavigationBar;"

    internal const val INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/shared/NavigationBar\$NavigationButton;"

    private lateinit var navigationTabCreatedCallbackMethod: MutableMethod

    override fun execute(context: BytecodeContext) {
        InitializeButtonsFingerprint.resolve(
            context,
            PivotBarConstructorFingerprint.resultOrThrow().classDef
        )

        InitializeButtonsFingerprint.resultOrThrow().apply {

            // Hook the navigation enum.  Note: The 'You' tab does not have an enum hook.
            val navigationEnumClassName = NavigationEnumFingerprint.resultOrThrow().mutableClass.type
            mutableMethod.injectHooksByFilter(
                {
                    it.opcode == Opcode.INVOKE_STATIC &&
                            it.getReference<MethodReference>()?.definingClass == navigationEnumClassName
                },
                "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->setLastAppNavigationEnum(Ljava/lang/Enum;)V"
            )

            // Hook the creation of navigation tab views.
            val drawableTabResult = PivotBarButtonsCreateDrawableViewFingerprint.resultOrThrow()
            mutableMethod.injectHooksByFilter(
                {
                    // Don't need to check for the opcode since the method reference already validates.
                    val reference = it.getReference<MethodReference>()
                    reference?.definingClass == drawableTabResult.mutableClass.type &&
                            reference.name == drawableTabResult.mutableMethod.name
                },
                "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->navigationTabLoaded(Landroid/view/View;)V"
            )

            val imageResourceTabResult = PivotBarButtonsCreateResourceViewFingerprint.resultOrThrow()
            mutableMethod.injectHooksByFilter(
                {
                    val reference = it.getReference<MethodReference>()
                    reference?.definingClass == imageResourceTabResult.mutableClass.type &&
                            reference.name == imageResourceTabResult.mutableMethod.name
                },
                "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->navigationImageResourceTabLoaded(Landroid/view/View;)V"
            )
        }

        /**
         * Callback for other patches.
         */
        NavigationBarHookCallbackFingerprint.resultOrThrow().apply {
            navigationTabCreatedCallbackMethod = mutableMethod
        }

        /**
         * Search bar.
         */

        // Two different layouts are used at the hooked code.
        // Insert before the first ViewGroup method call after inflating,
        // so this works regardless which layout is used.
        ActionBarSearchResultsFingerprint.resultOrThrow().mutableMethod.apply {
            val instructionIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.INVOKE_VIRTUAL &&
                        it.getReference<MethodReference>()?.name == "setLayoutDirection"
            }
            val register = getInstruction<FiveRegisterInstruction>(instructionIndex).registerC
            addInstruction(
                instructionIndex,
                "invoke-static { v$register }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->searchBarResultsViewLoaded(Landroid/view/View;)V"
            )
        }
    }

    fun hookNavigationButtonCreated(classDescriptor: String) {
        navigationTabCreatedCallbackMethod.addInstruction(
            0,
            "invoke-static { p0, p1 }, " +
                    "$classDescriptor->navigationTabCreated(${INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR}Landroid/view/View;)V"
        )
    }
}
