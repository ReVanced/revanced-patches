package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/spotify/layout/hide/createbutton/HideCreateButtonPatch;"

@Suppress("unused")
val hideCreateButtonPatch = bytecodePatch(
    name = "Hide Create button",
    description = "Hides the \"Create\" button in the navigation bar."
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {
        val oldNavigationBarAddItemMethod = oldNavigationBarAddItemFingerprint.originalMethodOrNull
        // Only throw the fingerprint error when oldNavigationBarAddItemMethod does not exist.
        val navigationBarItemSetClassDef = if (oldNavigationBarAddItemMethod == null) {
            navigationBarItemSetClassFingerprint.originalClassDef
        } else {
            navigationBarItemSetClassFingerprint.originalClassDefOrNull
        }

        if (navigationBarItemSetClassDef != null) {
            // Main patch for newest and most versions.
            // The NavigationBarItemSet constructor accepts multiple parameters which represent each navigation bar item.
            // Each item is manually checked whether it is not null and then added to a LinkedHashSet.
            // Since the order of the items can differ, we are required to check every parameter to see whether it is the
            // Create button. So, for every parameter passed to the method, invoke our extension method and overwrite it
            // to null in case it is the Create button.
            navigationBarItemSetConstructorFingerprint.match(navigationBarItemSetClassDef).method.apply {
                // Add 1 to the index because the first parameter register is `this`.
                val parameterTypesWithRegister = parameterTypes.mapIndexed { index, parameterType ->
                    parameterType to (index + 1)
                }

                val returnNullIfIsCreateButtonDescriptor =
                    "$EXTENSION_CLASS_DESCRIPTOR->returnNullIfIsCreateButton(Ljava/lang/Object;)Ljava/lang/Object;"

                parameterTypesWithRegister.reversed().forEach { (parameterType, parameterRegister) ->
                    addInstructions(
                        0,
                        """
                            invoke-static { p$parameterRegister }, $returnNullIfIsCreateButtonDescriptor
                            move-result-object p$parameterRegister
                            check-cast p$parameterRegister, $parameterType
                        """
                    )
                }
            }
        }

        if (oldNavigationBarAddItemMethod != null) {
            // In case an older version of the app is being patched, hook the old method which adds navigation bar items.
            // Return early if the navigation bar item title resource id is the old Create button title resource id.
            oldNavigationBarAddItemFingerprint.methodOrNull?.apply {
                val getNavigationBarItemTitleStringIndex = indexOfFirstInstructionOrThrow {
                    val reference = getReference<MethodReference>()
                    reference?.definingClass == "Landroid/content/res/Resources;" && reference.name == "getString"
                }
                // This register is a parameter register, so it can be used at the start of the method when adding
                // the new instructions.
                val oldNavigationBarItemTitleResIdRegister =
                    getInstruction<FiveRegisterInstruction>(getNavigationBarItemTitleStringIndex).registerD

                // The instruction where the normal method logic starts.
                val firstInstruction = getInstruction(0)

                val isOldCreateButtonDescriptor =
                    "$EXTENSION_CLASS_DESCRIPTOR->isOldCreateButton(I)Z"

                val returnEarlyInstruction = if (returnType == "V") {
                    // In older implementations the method return value is void.
                    "return-void"
                } else {
                    // In newer implementations
                    // return null because the method return value is a BottomNavigationItemView.
                    "const/4 v0, 0\n" +
                    "return-object v0"
                }

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static { v$oldNavigationBarItemTitleResIdRegister }, $isOldCreateButtonDescriptor
                        move-result v0

                        # If this navigation bar item is not the Create button, jump to the normal method logic.
                        if-eqz v0, :normal-method-logic

                        $returnEarlyInstruction
                    """,
                    ExternalLabel("normal-method-logic", firstInstruction)
                )
            }
        }
    }
}
