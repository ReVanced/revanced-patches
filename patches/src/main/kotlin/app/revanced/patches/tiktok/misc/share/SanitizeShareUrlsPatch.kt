package app.revanced.patches.tiktok.misc.share

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS
import app.revanced.patches.shared.PATCH_NAME_SANITIZE_SHARING_LINKS
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/tiktok/share/ShareUrlSanitizer;"

@Suppress("unused")
val sanitizeShareUrlsPatch = bytecodePatch(
    name = PATCH_NAME_SANITIZE_SHARING_LINKS,
    description = PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS,
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.ss.android.ugc.trill"("36.5.4"),
        "com.zhiliaoapp.musically"("36.5.4"),
    )

    execute {
        urlShorteningFingerprint.method.apply {
            val invokeIndex = indexOfFirstInstructionOrThrow {
                val ref = getReference<MethodReference>()
                ref?.name == "LIZ" && ref.definingClass.startsWith("LX/")
            }

            val moveResultIndex = indexOfFirstInstructionOrThrow(invokeIndex, Opcode.MOVE_RESULT_OBJECT)
            val urlRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            // Resolve Observable wrapper classes at runtime
            val observableWrapperIndex = indexOfFirstInstructionOrThrow(Opcode.NEW_INSTANCE)
            val observableWrapperClass = getInstruction<ReferenceInstruction>(observableWrapperIndex)
                .reference.toString()

            val observableFactoryIndex = indexOfFirstInstructionOrThrow {
                val ref = getReference<MethodReference>()
                ref?.name == "LJ" && ref.definingClass.startsWith("LX/")
            }
            val observableFactoryRef = getInstruction<ReferenceInstruction>(observableFactoryIndex)
                .reference as MethodReference

            val observableFactoryClass = observableFactoryRef.definingClass
            val observableInterfaceType = observableFactoryRef.parameterTypes.first()
            val observableReturnType = observableFactoryRef.returnType

            val wrapperRegister = findFreeRegister(moveResultIndex + 1, urlRegister)

            // Check setting and conditionally sanitize share URL.
            addInstructionsWithLabels(
                moveResultIndex + 1,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->shouldSanitize()Z
                    move-result v$wrapperRegister
                    if-eqz v$wrapperRegister, :skip_sanitization

                    invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->sanitizeShareUrl(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister

                    # Wrap sanitized URL and return early to bypass ShareExtService
                    new-instance v$wrapperRegister, $observableWrapperClass
                    invoke-direct { v$wrapperRegister, v$urlRegister }, $observableWrapperClass-><init>(Ljava/lang/String;)V
                    invoke-static { v$wrapperRegister }, $observableFactoryClass->LJ($observableInterfaceType)$observableReturnType
                    move-result-object v$urlRegister
                    return-object v$urlRegister

                    :skip_sanitization
                    nop
                """
            )
        }
    }
}
