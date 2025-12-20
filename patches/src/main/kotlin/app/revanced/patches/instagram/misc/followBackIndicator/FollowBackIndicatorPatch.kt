package app.revanced.patches.instagram.misc.followBackIndicator

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val followBackIndicatorPatch = bytecodePatch(
    name = "Follow back indicator",
    description = "Adds a label in profile page, indicating if an user is follows you back.",
    use = true,
) {
    dependsOn(sharedExtensionPatch)
    compatibleWith("com.instagram.android")
    execute {
        /**
         * This function replaces a string instruction with a new one.
         *
         * @param index The index of the string constant.
         * @param value The new string to be replaced with.
         */
        fun Fingerprint.changeString(
            index: Int,
            value: String,
        ) {
            method.instructions.filter { it.opcode == Opcode.CONST_STRING }[index].let { instruction ->
                val register = (instruction as BuilderInstruction21c).registerA
                method.replaceInstruction(instruction.location.index, "const-string v$register, \"$value\"")
            }
        }

        // This fingerprint is used to identify the static method and its defining class,
        // which is used for identifying a user's follow back status.
        nametagResultCardViewSetButtonMethodFingerprint.method.apply {
            val moveResultIndex = instructions.first { it.opcode == Opcode.MOVE_RESULT }.location.index
            val invokeStaticMethodReference = getInstruction(moveResultIndex - 1).getReference<MethodReference>()

            val methodDefClassName = invokeStaticMethodReference!!.definingClass.removePrefix("L").replace("/", ".").removeSuffix(";")
            getFollowbackInfoInfoExtension.changeString(0,methodDefClassName)

            val methodName = invokeStaticMethodReference.name
            getFollowbackInfoInfoExtension.changeString(1,methodName)
        }

        // This constant stores the value of the obfuscated profile info class,
        // which is later used to find the index of the parameter.
        var profileInfoClassName:String

        // This fingerprint is used to identify field name in obfuscated profile info class,
        // that holds user data.
        bindRowViewTypesFingerprint.method.apply {
            val igetObjectInstruction = instructions.first { it.opcode == Opcode.IGET_OBJECT  }
            val fieldReference = igetObjectInstruction.getReference<FieldReference>()
            val userObjectFieldName = fieldReference!!.name
            getViewingProfileUserObjectInfoExtension.changeString(0,userObjectFieldName)
            profileInfoClassName = fieldReference.definingClass
        }

        // This fingerprint is used to identify the internal badge, which is used for displaying follow back status.
        bindInternalBadgeFingerprint.method.apply {
            val internalBadgeStringIndex = bindInternalBadgeFingerprint.stringMatches!![0].index

            // Identify the profile info in the method parameter, which is later passed to our custom hook.
            val profileInfoParameter = parameters.indexOfFirst { it.type == profileInfoClassName }

            val internalBadgeInstructionIndex = indexOfFirstInstruction(internalBadgeStringIndex, Opcode.IGET_OBJECT)
            val internalBadgeInstruction = getInstruction<TwoRegisterInstruction>(internalBadgeInstructionIndex)
            // Internal badge is an element/view, which is used internally to mark developers.
            // We hook and update its text to display the follow back status.
            val internalBadgeRegistry = internalBadgeInstruction.registerA
            // User profile page (obfuscated) contains all the elements that are present on the user page.
            // We are hooking it in order to find user session, which is used to get info on logged in user.
            val userProfilePageRegistry = internalBadgeInstruction.registerB

            // Finding the necessary dummy registries.
            val dummyRegistryInstructionIndex = indexOfFirstInstruction(internalBadgeInstructionIndex + 1, Opcode.IGET_OBJECT)
            val dummyRegistry1 = getInstruction<TwoRegisterInstruction>(dummyRegistryInstructionIndex).registerA
            val dummyRegistry2 = getInstruction<OneRegisterInstruction>(internalBadgeStringIndex).registerA

            // Instruction to which the call needs to transfer after our hook.
            val invokeStaticRangeIndex = indexOfFirstInstruction(internalBadgeInstructionIndex, Opcode.INVOKE_STATIC_RANGE)

            val userSessionClassName = "Lcom/instagram/common/session/UserSession;"
            // Finding the user profile page (obfuscated) class name.
            val userProfilePageElementsClassName = internalBadgeInstruction.getReference<FieldReference>()!!.definingClass
            // Finding the user session field.
            val userSessionFieldName = classes.find { it.type == userProfilePageElementsClassName }!!.fields.first { it.type == userSessionClassName }.name

            // Added instructions:
            // Get the user session.
            // Move the profile info parameter to a suitable registry.
            // Call our hook, which will update the badge.
            addInstructionsWithLabels(
                internalBadgeInstructionIndex + 1,
                """
                    iget-object v$dummyRegistry1, v$userProfilePageRegistry, $userProfilePageElementsClassName->$userSessionFieldName:$userSessionClassName
                    move-object/from16 v$dummyRegistry2, p$profileInfoParameter
                    
                    invoke-static {v$dummyRegistry1,v$dummyRegistry2, v$internalBadgeRegistry}, ${EXTENSION_CLASS_DESCRIPTOR}FollowBackIndicator;->indicator($userSessionClassName Ljava/lang/Object;Ljava/lang/Object;)V
                    goto :revanced
                """.trimIndent(),
                ExternalLabel("revanced", getInstruction(invokeStaticRangeIndex)),
            )
        }
    }
}
