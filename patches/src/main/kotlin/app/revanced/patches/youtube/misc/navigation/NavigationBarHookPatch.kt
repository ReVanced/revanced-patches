package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_35_or_greater
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/shared/NavigationBar;"
internal const val EXTENSION_NAVIGATION_BUTTON_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/shared/NavigationBar\$NavigationButton;"

lateinit var hookNavigationButtonCreated: (String) -> Unit

val navigationBarHookPatch = bytecodePatch(description = "Hooks the active navigation or search bar.") {
    dependsOn(
        sharedExtensionPatch,
        playerTypeHookPatch, // Required to detect the search bar in all situations.
        resourceMappingPatch // Used by fingerprints
    )

    execute {
        fun MutableMethod.addHook(hook: Hook, insertPredicate: Instruction.() -> Boolean) {
            val filtered = instructions.filter(insertPredicate)
            if (filtered.isEmpty()) throw PatchException("Could not find insert indexes")
            filtered.forEach {
                val insertIndex = it.location.index + 2
                val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$register }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->${hook.methodName}(${hook.parameters})V",
                )
            }
        }

        initializeButtonsFingerprint.match(pivotBarConstructorFingerprint.originalClassDef).method.apply {
            // Hook the current navigation bar enum value. Note, the 'You' tab does not have an enum value.
            val navigationEnumClassName = navigationEnumFingerprint.classDef.type
            addHook(Hook.SET_LAST_APP_NAVIGATION_ENUM) {
                opcode == Opcode.INVOKE_STATIC &&
                    getReference<MethodReference>()?.definingClass == navigationEnumClassName
            }

            // Hook the creation of navigation tab views.
            val drawableTabMethod = pivotBarButtonsCreateDrawableViewFingerprint.method
            addHook(Hook.NAVIGATION_TAB_LOADED) predicate@{
                MethodUtil.methodSignaturesMatch(
                    getReference<MethodReference>() ?: return@predicate false,
                    drawableTabMethod,
                )
            }

            val imageResourceTabMethod = pivotBarButtonsCreateResourceViewFingerprint.originalMethod
            addHook(Hook.NAVIGATION_IMAGE_RESOURCE_TAB_LOADED) predicate@{
                MethodUtil.methodSignaturesMatch(
                    getReference<MethodReference>() ?: return@predicate false,
                    imageResourceTabMethod,
                )
            }
        }

        pivotBarButtonsViewSetSelectedFingerprint.let {
            it.method.apply {
                val index = it.filterMatches.first().index
                val instruction = getInstruction<FiveRegisterInstruction>(index)
                val viewRegister = instruction.registerC
                val isSelectedRegister = instruction.registerD

                addInstruction(
                    index + 1,
                    "invoke-static { v$viewRegister, v$isSelectedRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->navigationTabSelected(Landroid/view/View;Z)V",
                )
            }
        }

        // Hook onto back button pressed.  Needed to fix race problem with
        // Litho filtering based on navigation tab before the tab is updated.
        mainActivityOnBackPressedFingerprint.method.addInstruction(
            0,
            "invoke-static { p0 }, " +
                "$EXTENSION_CLASS_DESCRIPTOR->onBackPressed(Landroid/app/Activity;)V",
        )

        // Hook the search bar.

        // Two different layouts are used at the hooked code.
        // Insert before the first ViewGroup method call after inflating,
        // so this works regardless which layout is used.
        actionBarSearchResultsFingerprint.let {
            it.method.apply {
                val instructionIndex = it.filterMatches.last().index
                val viewRegister = getInstruction<FiveRegisterInstruction>(instructionIndex).registerC

                addInstruction(
                    instructionIndex,
                    "invoke-static { v$viewRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->searchBarResultsViewLoaded(Landroid/view/View;)V",
                )
            }
        }

        hookNavigationButtonCreated = { extensionClassDescriptor ->
            navigationBarHookCallbackFingerprint.method.addInstruction(
                0,
                "invoke-static { p0, p1 }, " +
                    "$extensionClassDescriptor->navigationTabCreated" +
                    "(${EXTENSION_NAVIGATION_BUTTON_DESCRIPTOR}Landroid/view/View;)V",
            )
        }

        // Fix YT bug of notification tab missing the filled icon.
        if (is_19_35_or_greater) {
            val cairoNotificationEnumReference = with(imageEnumConstructorFingerprint) {
                val stringIndex = stringMatches.first().index
                val cairoNotificationEnumIndex = method.indexOfFirstInstructionOrThrow(stringIndex) {
                    opcode == Opcode.SPUT_OBJECT
                }
                method.getInstruction<ReferenceInstruction>(cairoNotificationEnumIndex).reference
            }

            setEnumMapFingerprint.method.apply {
                val enumMapIndex = indexOfFirstInstructionReversedOrThrow {
                    val reference = getReference<MethodReference>()
                    opcode == Opcode.INVOKE_VIRTUAL &&
                            reference?.definingClass == "Ljava/util/EnumMap;" &&
                            reference.name == "put" &&
                            reference.parameterTypes.firstOrNull() == "Ljava/lang/Enum;"
                }
                val instruction = getInstruction<FiveRegisterInstruction>(enumMapIndex)

                addInstructions(
                    enumMapIndex + 1,
                    """
                        sget-object v${instruction.registerD}, $cairoNotificationEnumReference
                        invoke-static { v${instruction.registerC}, v${instruction.registerD} }, $EXTENSION_CLASS_DESCRIPTOR->setCairoNotificationFilledIcon(Ljava/util/EnumMap;Ljava/lang/Enum;)V
                    """
                )
            }
        }
    }
}

private enum class Hook(val methodName: String, val parameters: String) {
    SET_LAST_APP_NAVIGATION_ENUM("setLastAppNavigationEnum", "Ljava/lang/Enum;"),
    NAVIGATION_TAB_LOADED("navigationTabLoaded", "Landroid/view/View;"),
    NAVIGATION_IMAGE_RESOURCE_TAB_LOADED("navigationImageResourceTabLoaded", "Landroid/view/View;"),
}
