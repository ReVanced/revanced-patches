package app.revanced.patches.youtube.utils.overridespeed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.utils.integrations.Constants.INTEGRATIONS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.overridespeed.fingerprints.PlaybackSpeedChangedFingerprint
import app.revanced.patches.youtube.utils.overridespeed.fingerprints.PlaybackSpeedParentFingerprint
import app.revanced.patches.youtube.utils.overridespeed.fingerprints.PlaybackSpeedPatchFingerprint
import app.revanced.patches.youtube.utils.overridespeed.fingerprints.SpeedClassFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

object OverrideSpeedHookPatch : BytecodePatch(
    setOf(
        PlaybackSpeedPatchFingerprint,
        PlaybackSpeedParentFingerprint,
        SpeedClassFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        PlaybackSpeedParentFingerprint.result?.let { parentResult ->
            val parentClassDef = parentResult.classDef

            PlaybackSpeedChangedFingerprint.also {
                it.resolve(
                    context,
                    parentClassDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    playbackSpeedChangedResult = it
                    val startIndex = it.scanResult.patternScanResult!!.startIndex
                    val endIndex = it.scanResult.patternScanResult!!.endIndex

                    val reference1 = getInstruction<ReferenceInstruction>(startIndex).reference
                    val reference2 = getInstruction<ReferenceInstruction>(endIndex - 1).reference
                    val reference3 = getInstruction<ReferenceInstruction>(endIndex).reference
                    val fieldReference = reference2 as FieldReference

                    val parentMutableClass = parentResult.mutableClass

                    parentMutableClass.methods.add(
                        ImmutableMethod(
                            parentMutableClass.type,
                            "overrideSpeed",
                            listOf(ImmutableMethodParameter("F", annotations, null)),
                            "V",
                            AccessFlags.PUBLIC or AccessFlags.PUBLIC,
                            annotations,
                            null,
                            ImmutableMethodImplementation(
                                4, """
                                    const/4 v0, 0x0
                                    cmpg-float v0, v3, v0
                                    if-lez v0, :cond_0
                                    iget-object v0, v2, $reference1
                                    check-cast v0, ${fieldReference.definingClass}
                                    iget-object v1, v0, $reference2
                                    invoke-virtual {v1, v3}, $reference3
                                    :cond_0
                                    return-void
                                    """.toInstructions(), null, null
                            )
                        ).toMutable()
                    )

                    with(
                        context
                            .toMethodWalker(this)
                            .nextMethod(endIndex, true)
                            .getMethod() as MutableMethod
                    ) {
                        addInstruction(
                            this.implementation!!.instructions.size - 1,
                            "sput p1, $INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR->currentSpeed:F"
                        )
                    }
                }

            } ?: throw PlaybackSpeedChangedFingerprint.exception
        } ?: throw PlaybackSpeedParentFingerprint.exception


        SpeedClassFingerprint.result?.let {
            it.mutableMethod.apply {
                val index = it.scanResult.patternScanResult!!.endIndex
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                SPEED_CLASS = this.returnType
                replaceInstruction(
                    index,
                    "sput-object v$register, $INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR->speedClass:$SPEED_CLASS"
                )
                addInstruction(
                    index + 1,
                    "return-object v$register"
                )
            }

        } ?: throw SpeedClassFingerprint.exception

        PlaybackSpeedPatchFingerprint.result?.let {
            it.mutableMethod.apply {
                it.mutableClass.staticFields.add(
                    ImmutableField(
                        definingClass,
                        "speedClass",
                        SPEED_CLASS,
                        AccessFlags.PUBLIC or AccessFlags.STATIC,
                        null,
                        annotations,
                        null
                    ).toMutable()
                )

                addInstructions(
                    0, """
                        sget-object v0, $INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR->speedClass:$SPEED_CLASS
                        invoke-virtual {v0, p0}, $SPEED_CLASS->overrideSpeed(F)V
                        """
                )
            }

        } ?: throw PlaybackSpeedPatchFingerprint.exception

    }

    private const val INTEGRATIONS_PLAYBACK_SPEED_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/PlaybackSpeedPatch;"

    private const val INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR =
        "$INTEGRATIONS_PATH/utils/VideoHelpers;"

    lateinit var playbackSpeedChangedResult: MethodFingerprintResult

    private lateinit var SPEED_CLASS: String
}