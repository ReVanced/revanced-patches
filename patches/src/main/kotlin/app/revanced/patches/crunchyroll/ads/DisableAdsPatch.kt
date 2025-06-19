package app.revanced.patches.crunchyroll.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.removeFlag
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable Ads"
) {
    compatibleWith("com.crunchyroll.crunchyroid")

    execute {
        val enableAdsField = videoUrlReadyToStringFingerprint.let {
            val strIndex = videoUrlReadyToStringFingerprint.stringMatches!!.last().index
            // The iget-xxx should always be after const-string and invoke-virtual (StringBuilder.append()).
            it.method.getInstruction<ReferenceInstruction>(strIndex + 2).getReference<FieldReference>()!!
        }

        // Remove final access flag on field.
        videoUrlReadyToStringFingerprint.classDef.fields
            .first { it.name == enableAdsField.name }
            .removeFlag(AccessFlags.FINAL)

        // Override field non-default constructor (has parameters).
        val constructor = videoUrlReadyToStringFingerprint.classDef.methods.first {
            AccessFlags.CONSTRUCTOR.isSet(it.accessFlags) && it.parameters.isNotEmpty()
        }
        // "this" reference is previously moved from p0 to v0.
        constructor.addInstructions(
            constructor.instructions.count() - 1,
            """
            const/4 v1, 0x0
            iput-boolean v1, v0, ${enableAdsField.definingClass}->${enableAdsField.name}:Z
            """.trimIndent())
    }
}