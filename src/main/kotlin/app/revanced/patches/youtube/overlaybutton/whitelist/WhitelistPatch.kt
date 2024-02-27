package app.revanced.patches.youtube.overlaybutton.whitelist

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.overlaybutton.whitelist.fingerprints.ChannelNameFingerprint
import app.revanced.patches.youtube.overlaybutton.whitelist.fingerprints.PlayerResponseModelFingerprint
import app.revanced.patches.youtube.overlaybutton.whitelist.fingerprints.PrimaryInjectFingerprint
import app.revanced.patches.youtube.overlaybutton.whitelist.fingerprints.SecondaryInjectFingerprint
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.Reference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

object WhitelistPatch : BytecodePatch(
    setOf(
        ChannelNameFingerprint,
        PlayerResponseModelFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        ChannelNameFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getStringInstructionIndex("Person") + 2

                channelNameReference = getReference(targetIndex)
            }
        } ?: throw ChannelNameFingerprint.exception

        PlayerResponseModelFingerprint.result?.let { parentResult ->
            parentResult.mutableMethod.apply {
                val targetIndex = parentResult.scanResult.patternScanResult!!.startIndex

                val primaryReference = getReference(targetIndex)
                val secondaryReference = getReference(targetIndex + 1)
                val tertiaryReference = getReference(targetIndex + 2)

                val parentMutableClass = parentResult.mutableClass
                parentMutableClass.methods.add(
                    ImmutableMethod(
                        parentMutableClass.type,
                        "setChannelName",
                        emptyList(),
                        "V",
                        AccessFlags.PRIVATE or AccessFlags.FINAL,
                        annotations,
                        null,
                        ImmutableMethodImplementation(
                            2,
                            """
                                iget-object v0, v1, $primaryReference
                                iget-object v0, v0, $secondaryReference
                                invoke-interface {v0}, $tertiaryReference
                                move-result-object v0
                                invoke-interface {v0}, $channelNameReference
                                move-result-object v0
                                invoke-static {v0}, $VIDEO_PATH/VideoInformation;->setChannelName(Ljava/lang/String;)V
                                return-void
                                """.toInstructions(), null, null
                        )
                    ).toMutable()
                )
            }
            arrayOf(
                PrimaryInjectFingerprint,
                SecondaryInjectFingerprint
            ).forEach { fingerprint ->
                fingerprint.also {
                    it.resolve(
                        context,
                        parentResult.classDef
                    )
                }.result?.let {
                    it.mutableMethod.apply {
                        val index = it.scanResult.patternScanResult!!.endIndex + 1

                        addInstruction(
                            index,
                            "invoke-direct {p0}, ${parentResult.classDef.type}->setChannelName()V"
                        )
                    }
                } ?: throw fingerprint.exception
            }
        } ?: throw PlayerResponseModelFingerprint.exception
    }

    private lateinit var channelNameReference: Reference

    private fun MutableMethod.getReference(index: Int): Reference {
        return getInstruction<ReferenceInstruction>(index).reference
    }
}