package app.revanced.patches.youtube.misc.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_35_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_21_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_28_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.shared.mainActivityOnBackPressedFingerprint
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.util.MethodUtil

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/shared/NavigationBar;"
internal const val EXTENSION_NAVIGATION_BUTTON_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/shared/NavigationBar\$NavigationButton;"
private const val EXTENSION_TOOLBAR_INTERFACE =
    "Lapp/revanced/extension/youtube/shared/NavigationBar${'$'}AppCompatToolbarPatchInterface;"

lateinit var hookNavigationButtonCreated: (String) -> Unit

val navigationBarHookPatch = bytecodePatch(description = "Hooks the active navigation or search bar.") {
    dependsOn(
        sharedExtensionPatch,
        versionCheckPatch,
        playerTypeHookPatch, // Required to detect the search bar in all situations.
        resourceMappingPatch, // Used by fingerprints
        resourcePatch {
            // Copy missing notification icon.
            execute {
                copyResources(
                    "navigationbuttons",
                    ResourceGroup(
                        "drawable",
                        "revanced_fill_bell_cairo_black_24.xml"
                    )
                )
            }
        }
    )

    execute {
        fun MutableMethod.addHook(hook: NavigationHook, insertPredicate: Instruction.() -> Boolean) {
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
            addHook(NavigationHook.SET_LAST_APP_NAVIGATION_ENUM) {
                opcode == Opcode.INVOKE_STATIC &&
                    getReference<MethodReference>()?.definingClass == navigationEnumClassName
            }

            // Hook the creation of navigation tab views.
            val drawableTabMethod = pivotBarButtonsCreateDrawableViewFingerprint.method
            addHook(NavigationHook.NAVIGATION_TAB_LOADED) predicate@{
                MethodUtil.methodSignaturesMatch(
                    getReference<MethodReference>() ?: return@predicate false,
                    drawableTabMethod,
                )
            }

            if (is_20_21_or_greater && !is_20_28_or_greater) {
                val imageResourceIntTabMethod = pivotBarButtonsCreateResourceIntViewFingerprint.originalMethod
                addHook(NavigationHook.NAVIGATION_TAB_LOADED) predicate@{
                    MethodUtil.methodSignaturesMatch(
                        getReference<MethodReference>() ?: return@predicate false,
                        imageResourceIntTabMethod,
                    )
                }
            }

            val imageResourceTabMethod = pivotBarButtonsCreateResourceStyledViewFingerprint.originalMethod
            addHook(NavigationHook.NAVIGATION_IMAGE_RESOURCE_TAB_LOADED) predicate@{
                MethodUtil.methodSignaturesMatch(
                    getReference<MethodReference>() ?: return@predicate false,
                    imageResourceTabMethod,
                )
            }
        }

        pivotBarButtonsViewSetSelectedFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.first().index
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
            "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->onBackPressed(Landroid/app/Activity;)V",
        )

        // Hook the search bar.

        // Two different layouts are used at the hooked code.
        // Insert before the first ViewGroup method call after inflating,
        // so this works regardless which layout is used.
        actionBarSearchResultsFingerprint.let {
            it.method.apply {
                val instructionIndex = it.instructionMatches.last().index
                val viewRegister = getInstruction<FiveRegisterInstruction>(instructionIndex).registerC

                addInstruction(
                    instructionIndex,
                    "invoke-static { v$viewRegister }, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->searchBarResultsViewLoaded(Landroid/view/View;)V",
                )
            }
        }

        // Hook the back button visibility.

        toolbarLayoutFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.last().index
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->setToolbar(Landroid/widget/FrameLayout;)V"
                )
            }
        }

        // Add interface for extensions code to call obfuscated methods.
        appCompatToolbarBackButtonFingerprint.let {
            it.classDef.apply {
                interfaces.add(EXTENSION_TOOLBAR_INTERFACE)

                val definingClass = type
                val obfuscatedMethodName = it.originalMethod.name
                val returnType = "Landroid/graphics/drawable/Drawable;"

                methods.add(
                    ImmutableMethod(
                        definingClass,
                        "patch_getNavigationIcon",
                        listOf(),
                        returnType,
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        addInstructions(
                            0,
                            """
                                invoke-virtual { p0 }, $definingClass->$obfuscatedMethodName()$returnType
                                move-result-object v0
                                return-object v0
                            """
                        )
                    }
                )
            }
        }

        hookNavigationButtonCreated = { extensionClassDescriptor ->
            navigationBarHookCallbackFingerprint.method.addInstruction(
                0,
                "invoke-static { p0, p1 }, $extensionClassDescriptor->navigationTabCreated" +
                    "(${EXTENSION_NAVIGATION_BUTTON_DESCRIPTOR}Landroid/view/View;)V",
            )
        }

        // Fix YT bug of notification tab missing the filled icon.
        if (is_19_35_or_greater) {
            val cairoNotificationEnumReference = imageEnumConstructorFingerprint
                .instructionMatches.last().getInstruction<ReferenceInstruction>().reference

            setEnumMapFingerprint.let {
                it.method.apply {
                    val setEnumIntegerIndex = it.instructionMatches.last().index
                    val enumMapRegister = getInstruction<FiveRegisterInstruction>(setEnumIntegerIndex).registerC
                    val insertIndex = setEnumIntegerIndex + 1
                    val freeRegister = findFreeRegister(insertIndex, enumMapRegister)

                    addInstructions(
                        insertIndex,
                        """
                            sget-object v$freeRegister, $cairoNotificationEnumReference
                            invoke-static { v$enumMapRegister, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->setCairoNotificationFilledIcon(Ljava/util/EnumMap;Ljava/lang/Enum;)V
                        """
                    )
                }
            }
        }
    }
}

private enum class NavigationHook(val methodName: String, val parameters: String) {
    SET_LAST_APP_NAVIGATION_ENUM("setLastAppNavigationEnum", "Ljava/lang/Enum;"),
    NAVIGATION_TAB_LOADED("navigationTabLoaded", "Landroid/view/View;"),
    NAVIGATION_IMAGE_RESOURCE_TAB_LOADED("navigationImageResourceTabLoaded", "Landroid/view/View;"),
}
