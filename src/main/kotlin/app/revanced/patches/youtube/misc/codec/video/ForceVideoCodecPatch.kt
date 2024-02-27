package app.revanced.patches.youtube.misc.codec.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.codec.video.fingerprints.VideoPrimaryFingerprint
import app.revanced.patches.youtube.misc.codec.video.fingerprints.VideoPropsFingerprint
import app.revanced.patches.youtube.misc.codec.video.fingerprints.VideoPropsParentFingerprint
import app.revanced.patches.youtube.misc.codec.video.fingerprints.VideoSecondaryFingerprint
import app.revanced.patches.youtube.utils.fingerprints.LayoutSwitchFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.dexbacked.reference.DexBackedFieldReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Force video codec",
    description = "Adds an option to force the video codec.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object ForceVideoCodecPatch : BytecodePatch(
    setOf(
        LayoutSwitchFingerprint,
        VideoPropsParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        LayoutSwitchFingerprint.result?.classDef?.let { classDef ->
            arrayOf(
                VideoPrimaryFingerprint,
                VideoSecondaryFingerprint
            ).forEach { fingerprint ->
                fingerprint.also { it.resolve(context, classDef) }.result?.injectOverride()
                    ?: throw fingerprint.exception
            }
        } ?: throw LayoutSwitchFingerprint.exception

        VideoPropsParentFingerprint.result?.let { parentResult ->
            VideoPropsFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.mutableMethod?.let {
                mapOf(
                    "MANUFACTURER" to "getManufacturer",
                    "BRAND" to "getBrand",
                    "MODEL" to "getModel"
                ).forEach { (fieldName, descriptor) ->
                    it.hookProps(fieldName, descriptor)
                }
            } ?: throw VideoPropsFingerprint.exception
        } ?: throw VideoPropsParentFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: EXPERIMENTAL_FLAGS",
                "SETTINGS: ENABLE_VIDEO_CODEC"
            )
        )

        SettingsPatch.updatePatchStatus("Force video codec")

    }

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$MISC_PATH/CodecOverridePatch;"

    private const val INTEGRATIONS_CLASS_METHOD_REFERENCE =
        "$INTEGRATIONS_CLASS_DESCRIPTOR->shouldForceCodec(Z)Z"

    private fun MethodFingerprintResult.injectOverride() {
        mutableMethod.apply {
            val startIndex = scanResult.patternScanResult!!.startIndex
            val endIndex = scanResult.patternScanResult!!.endIndex

            val startRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA
            val endRegister = getInstruction<OneRegisterInstruction>(endIndex).registerA

            hookOverride(endIndex + 1, endRegister)
            removeInstruction(endIndex)
            hookOverride(startIndex + 1, startRegister)
            removeInstruction(startIndex)
        }
    }

    private fun MutableMethod.hookOverride(
        index: Int,
        register: Int
    ) {
        addInstructions(
            index, """
                    invoke-static {v$register}, $INTEGRATIONS_CLASS_METHOD_REFERENCE
                    move-result v$register
                    return v$register
                    """
        )
    }

    private fun MutableMethod.hookProps(
        fieldName: String,
        descriptor: String
    ) {
        val targetString = "Landroid/os/Build;->" +
                fieldName +
                ":Ljava/lang/String;"

        for ((index, instruction) in implementation!!.instructions.withIndex()) {
            if (instruction.opcode != Opcode.SGET_OBJECT) continue

            val indexString =
                ((instruction as? ReferenceInstruction)?.reference as? DexBackedFieldReference).toString()

            if (indexString != targetString) continue

            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1, """
                        invoke-static {v$register}, $INTEGRATIONS_CLASS_DESCRIPTOR->$descriptor(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                        """
            )
            break
        }
    }

}
