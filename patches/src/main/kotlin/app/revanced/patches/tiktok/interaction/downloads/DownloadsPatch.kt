package app.revanced.patches.tiktok.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.misc.settings.settingsStatusLoadFingerprint
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/tiktok/download/DownloadsPatch;"

@Suppress("unused")
val downloadsPatch = bytecodePatch(
    name = "Downloads",
    description = "Removes download restrictions and changes the default path to download to.",
) {
    dependsOn(
        sharedExtensionPatch,
    )

    compatibleWith(
        "com.ss.android.ugc.trill"("43.8.3"),
        "com.zhiliaoapp.musically"("43.8.3"),
    )

    execute {
        aclCommonShareFingerprint.method.returnEarly(0)
        aclCommonShare2Fingerprint.method.returnEarly(2)

        aclCommonShare3Fingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->shouldRemoveWatermark()Z
                move-result v0
                if-eqz v0, :noremovewatermark
                const/4 v0, 0x1
                return v0
                :noremovewatermark
                nop
            """,
        )

        awemeGetVideoFingerprint.method.apply {
            val returnIndex = findInstructionIndicesReversedOrThrow { opcode == Opcode.RETURN_OBJECT }.first()
            val register = getInstruction<OneRegisterInstruction>(returnIndex).registerA

            addInstructions(
                returnIndex,
                """
                    invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->patchVideoObject(Lcom/ss/android/ugc/aweme/feed/model/Video;)V
                """
            )
        }

        commentImageWatermarkFingerprint.method.apply {
            val drawBitmapIndex = findInstructionIndicesReversedOrThrow {
                opcode.name == "invoke-virtual" &&
                this is ReferenceInstruction &&
                reference.toString().contains("->drawBitmap(Landroid/graphics/Bitmap;FFLandroid/graphics/Paint;)V")
            }.first()

            val drawInstr = getInstruction<FiveRegisterInstruction>(drawBitmapIndex)
            val canvasReg = drawInstr.registerC
            val bitmapReg = drawInstr.registerD
            val xReg = drawInstr.registerE
            val yReg = drawInstr.registerF
            val paintReg = drawInstr.registerG

            removeInstructions(drawBitmapIndex, 1)

            addInstructionsWithLabels(
                drawBitmapIndex,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->shouldRemoveWatermark()Z
                    move-result v$xReg
                    
                    if-nez v$xReg, :skip_watermark
                    
                    const/4 v$xReg, 0x0
                    invoke-virtual {v$canvasReg, v$bitmapReg, v$xReg, v$yReg, v$paintReg}, Landroid/graphics/Canvas;->drawBitmap(Landroid/graphics/Bitmap;FFLandroid/graphics/Paint;)V
                    
                    :skip_watermark
                    nop
                """
            )
        }

        downloadUriFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow {
                getReference<FieldReference>().let {
                    it?.definingClass == "Landroid/os/Environment;" && it.name.startsWith("DIRECTORY_")
                }
            }.forEach { fieldIndex ->
                val pathRegister = getInstruction<OneRegisterInstruction>(fieldIndex).registerA
                val builderRegister = getInstruction<FiveRegisterInstruction>(fieldIndex + 1).registerC

                // Remove 'field load → append → "/Camera/" → append' block.
                removeInstructions(fieldIndex, 4)

                addInstructions(
                    fieldIndex,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->getDownloadPath()Ljava/lang/String;
                        move-result-object v$pathRegister
                        invoke-virtual { v$builderRegister, v$pathRegister }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    """,
                )
            }
        }

        settingsStatusLoadFingerprint.method.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/extension/tiktok/settings/SettingsStatus;->enableDownload()V",
        )
    }
}