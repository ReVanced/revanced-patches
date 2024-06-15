package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

internal var imageOnlyTabResourceId = -1L
    private set
internal var actionBarSearchResultsViewMicId = -1L
    private set

private val navigationBarHookResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        imageOnlyTabResourceId = resourceMappings["layout", "image_only_tab"]
        actionBarSearchResultsViewMicId = resourceMappings["layout", "action_bar_search_results_view_mic"]
    }
}

internal const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/shared/NavigationBar;"
internal const val INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/shared/NavigationBar\$NavigationButton;"

lateinit var hookNavigationButtonCreated: (String) -> Unit

@Suppress("unused")
val navigationBarHookPatch = bytecodePatch(description = "Hooks the active navigation or search bar.") {
    dependsOn(
        integrationsPatch,
        navigationBarHookResourcePatch,
        playerTypeHookPatch, // Required to detect the search bar in all situations.
    )

    val pivotBarConstructorFingerprintResult by pivotBarConstructorFingerprint
    val navigationEnumFingerprintResult by navigationEnumFingerprint
    val pivotBarButtonsCreateDrawableViewFingerprintResult by pivotBarButtonsCreateDrawableViewFingerprint
    val pivotBarButtonsCreateResourceViewFingerprintResult by pivotBarButtonsCreateResourceViewFingerprint
    val pivotBarButtonsViewSetSelectedFingerprintResult by pivotBarButtonsViewSetSelectedFingerprint
    val navigationBarHookCallbackFingerprintResult by navigationBarHookCallbackFingerprint
    val mainActivityOnBackPressedFingerprintResult by mainActivityOnBackPressedFingerprint
    val actionBarSearchFingerprintResultsFingerprintResult by actionBarSearchResultsFingerprint

    execute { context ->
        fun MutableMethod.addHook(hook: Hook, insertPredicate: Instruction.() -> Boolean) {
            val filtered = instructions.filter(insertPredicate)
            if (filtered.isEmpty()) throw PatchException("Could not find insert indexes")
            filtered.forEach {
                val insertIndex = it.location.index + 2
                val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$register }, " +
                        "$INTEGRATIONS_CLASS_DESCRIPTOR->${hook.methodName}(${hook.parameters})V",
                )
            }
        }

        initializeButtonsFingerprint.apply {
            resolve(context, pivotBarConstructorFingerprintResult.classDef)
        }.resultOrThrow().mutableMethod.apply {
            // Hook the current navigation bar enum value. Note, the 'You' tab does not have an enum value.
            val navigationEnumClassName = navigationEnumFingerprintResult.mutableClass.type
            addHook(Hook.SET_LAST_APP_NAVIGATION_ENUM) {
                opcode == Opcode.INVOKE_STATIC &&
                    getReference<MethodReference>()?.definingClass == navigationEnumClassName
            }

            // Hook the creation of navigation tab views.
            val drawableTabMethod = pivotBarButtonsCreateDrawableViewFingerprintResult.mutableMethod
            addHook(Hook.NAVIGATION_TAB_LOADED) predicate@{
                MethodUtil.methodSignaturesMatch(
                    getReference<MethodReference>() ?: return@predicate false,
                    drawableTabMethod,
                )
            }

            val imageResourceTabMethod = pivotBarButtonsCreateResourceViewFingerprintResult.method
            addHook(Hook.NAVIGATION_IMAGE_RESOURCE_TAB_LOADED) predicate@{
                MethodUtil.methodSignaturesMatch(
                    getReference<MethodReference>() ?: return@predicate false,
                    imageResourceTabMethod,
                )
            }
        }

        pivotBarButtonsViewSetSelectedFingerprintResult.mutableMethod.apply {
            val index = indexOfSetViewSelectedInstruction(this)
            val instruction = getInstruction<FiveRegisterInstruction>(index)
            val viewRegister = instruction.registerC
            val isSelectedRegister = instruction.registerD

            addInstruction(
                index + 1,
                "invoke-static { v$viewRegister, v$isSelectedRegister }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->navigationTabSelected(Landroid/view/View;Z)V",
            )
        }

        // Hook onto back button pressed.  Needed to fix race problem with
        // Litho filtering based on navigation tab before the tab is updated.
        mainActivityOnBackPressedFingerprintResult.mutableMethod.addInstruction(
            0,
            "invoke-static { p0 }, " +
                "$INTEGRATIONS_CLASS_DESCRIPTOR->onBackPressed(Landroid/app/Activity;)V",
        )

        // Hook the search bar.

        // Two different layouts are used at the hooked code.
        // Insert before the first ViewGroup method call after inflating,
        // so this works regardless which layout is used.
        actionBarSearchFingerprintResultsFingerprintResult.mutableMethod.apply {
            val instructionIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setLayoutDirection"
            }

            val viewRegister = getInstruction<FiveRegisterInstruction>(instructionIndex).registerC

            addInstruction(
                instructionIndex,
                "invoke-static { v$viewRegister }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->searchBarResultsViewLoaded(Landroid/view/View;)V",
            )
        }

        hookNavigationButtonCreated = { integrationsClassDescriptor ->
            navigationBarHookCallbackFingerprintResult.mutableMethod.addInstruction(
                0,
                "invoke-static { p0, p1 }, " +
                    "$integrationsClassDescriptor->navigationTabCreated" +
                    "(${INTEGRATIONS_NAVIGATION_BUTTON_DESCRIPTOR}Landroid/view/View;)V",
            )
        }
    }
}

private enum class Hook(val methodName: String, val parameters: String) {
    SET_LAST_APP_NAVIGATION_ENUM("setLastAppNavigationEnum", "Ljava/lang/Enum;"),
    NAVIGATION_TAB_LOADED("navigationTabLoaded", "Landroid/view/View;"),
    NAVIGATION_IMAGE_RESOURCE_TAB_LOADED("navigationImageResourceTabLoaded", "Landroid/view/View;"),
    SEARCH_BAR_RESULTS_VIEW_LOADED("searchBarResultsViewLoaded", "Landroid/view/View;"),
}
