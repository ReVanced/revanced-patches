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
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarButtonsViewFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarConstructorFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarCreateButtonViewFingerprint
import app.revanced.patches.youtube.misc.navigation.fingerprints.PivotBarEnumFingerprint
import app.revanced.patches.youtube.misc.navigation.utils.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.patches.youtube.misc.navigation.utils.InjectionUtils.injectHook
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
        ActionBarSearchResultsFingerprint,
        NavigationBarHookCallbackFingerprint
    )
) {
    internal const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/shared/NavigationBar;"

    internal const val INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/shared/NavigationBar\$NavigationButton;"

    private lateinit var navigationTabCreatedCallbackMethod: MutableMethod

    override fun execute(context: BytecodeContext) {
        PivotBarConstructorFingerprint.resultOrThrow().let {
            InitializeButtonsFingerprint.resolve(
                context,
                it.classDef
            )
        }

        val initializeButtonsResult = InitializeButtonsFingerprint.resultOrThrow()

        val fingerprintResults =
            arrayOf(PivotBarEnumFingerprint, PivotBarButtonsViewFingerprint)
                .onEach {
                    it.resolve(
                        context,
                        initializeButtonsResult.mutableMethod,
                        initializeButtonsResult.mutableClass,
                    )
                }
                .map { it.resultOrThrow().scanResult.patternScanResult!! }

        val enumScanResult = fingerprintResults[0]
        val buttonViewResult = fingerprintResults[1]

        val enumHookInsertIndex = enumScanResult.startIndex + 2
        val buttonHookInsertIndex = buttonViewResult.endIndex

        /*
         * Inject hooks
         */

        val enumHook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
            "$INTEGRATIONS_CLASS_DESCRIPTOR->setLastAppNavigationEnum(Ljava/lang/Enum;)V"
        val buttonHook = "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
            "$INTEGRATIONS_CLASS_DESCRIPTOR->navigationTabLoaded(Landroid/view/View;)V"

        // Inject bottom to top to not mess up the indices
        mapOf(
            buttonHook to buttonHookInsertIndex,
            enumHook to enumHookInsertIndex,
        ).forEach { (hook, insertIndex) ->
            initializeButtonsResult.mutableMethod.injectHook(insertIndex, hook)
        }


        /**
         * Unique hook just for the Create tab button.
         */
        PivotBarCreateButtonViewFingerprint.resolve(
            context,
            initializeButtonsResult.mutableMethod,
            initializeButtonsResult.mutableClass
        )

        PivotBarCreateButtonViewFingerprint.resultOrThrow().apply {
            val insertIndex = scanResult.patternScanResult!!.endIndex
            mutableMethod.injectHook(
                insertIndex,
                "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->createTabLoaded(Landroid/view/View;)V"
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
