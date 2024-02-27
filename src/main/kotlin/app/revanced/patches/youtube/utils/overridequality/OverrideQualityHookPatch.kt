package app.revanced.patches.youtube.utils.overridequality

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.youtube.utils.integrations.Constants.INTEGRATIONS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.overridequality.fingerprints.VideoQualityListFingerprint
import app.revanced.patches.youtube.utils.overridequality.fingerprints.VideoQualityPatchFingerprint
import app.revanced.patches.youtube.utils.overridequality.fingerprints.VideoQualityTextFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.QualityAuto
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(dependencies = [SharedResourceIdPatch::class])
object OverrideQualityHookPatch : BytecodePatch(
    setOf(
        VideoQualityListFingerprint,
        VideoQualityPatchFingerprint,
        VideoQualityTextFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        VideoQualityListFingerprint.result?.let {
            val constructorMethod =
                it.mutableClass.methods.first { method -> MethodUtil.isConstructor(method) }
            val overrideMethod =
                it.mutableClass.methods.find { method -> method.parameterTypes.first() == "I" }

            QUALITY_CLASS = it.method.definingClass
            QUALITY_METHOD = overrideMethod?.name
                ?: throw PatchException("Failed to find hook method")

            constructorMethod.apply {
                addInstruction(
                    2,
                    "sput-object p0, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->qualityClass:$QUALITY_CLASS"
                )
            }

            it.mutableMethod.apply {
                val listIndex = it.scanResult.patternScanResult!!.startIndex
                val listRegister = getInstruction<FiveRegisterInstruction>(listIndex).registerD

                val qualityAutoIndex = getWideLiteralInstructionIndex(QualityAuto) + 2
                val qualityAutoRegister =
                    getInstruction<OneRegisterInstruction>(qualityAutoIndex).registerA

                addInstruction(
                    listIndex,
                    "invoke-static {v$listRegister}, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->setVideoQualityList([Ljava/lang/Object;)V"
                )

                addInstruction(
                    qualityAutoIndex + 1,
                    "sput-object v$qualityAutoRegister, $INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR->qualityAutoString:Ljava/lang/String;"
                )
            }
        } ?: throw VideoQualityListFingerprint.exception

        VideoQualityPatchFingerprint.result?.let {
            it.mutableMethod.apply {
                it.mutableClass.staticFields.add(
                    ImmutableField(
                        definingClass,
                        "qualityClass",
                        QUALITY_CLASS,
                        AccessFlags.PUBLIC or AccessFlags.STATIC,
                        null,
                        annotations,
                        null
                    ).toMutable()
                )

                addInstructions(
                    0, """
                        sget-object v0, $INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR->qualityClass:$QUALITY_CLASS
                        invoke-virtual {v0, p0}, $QUALITY_CLASS->$QUALITY_METHOD(I)V
                        """
                )
            }
        } ?: throw VideoQualityPatchFingerprint.exception

        VideoQualityTextFingerprint.result?.let {
            it.mutableMethod.apply {
                val textIndex = it.scanResult.patternScanResult!!.endIndex
                val textRegister = getInstruction<TwoRegisterInstruction>(textIndex).registerA

                addInstruction(
                    textIndex + 1,
                    "invoke-static {v$textRegister}, $INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR->onQualityChanges(Ljava/lang/String;)V"
                )
            }
        } ?: throw VideoQualityTextFingerprint.exception
    }

    private const val INTEGRATIONS_VIDEO_QUALITY_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/VideoQualityPatch;"

    private const val INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR =
        "$INTEGRATIONS_PATH/utils/VideoHelpers;"

    private lateinit var QUALITY_CLASS: String
    private lateinit var QUALITY_METHOD: String
}