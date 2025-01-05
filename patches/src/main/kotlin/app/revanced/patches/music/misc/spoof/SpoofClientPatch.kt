package app.revanced.patches.music.misc.spoof

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/music/spoof/SpoofClientPatch;"

// TODO: Replace this patch with spoofVideoStreamsPatch once possible.
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to fix playback.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    dependsOn(
        sharedExtensionPatch,
        // TODO: Add settingsPatch
        userAgentClientSpoofPatch,
    )

    execute {
        val playerRequestClass = playerRequestConstructorFingerprint.classDef

        val createPlayerRequestBodyMatch = createPlayerRequestBodyFingerprint.match(playerRequestClass)

        val clientInfoContainerClass = createPlayerRequestBodyMatch.method
            .getInstruction(createPlayerRequestBodyMatch.filterMatches.first().index)
            .getReference<TypeReference>()!!.type

        val clientInfoField = setClientInfoClientVersionFingerprint.method.instructions.first {
            it.opcode == Opcode.IPUT_OBJECT && it.getReference<FieldReference>()!!.definingClass == clientInfoContainerClass
        }.getReference<FieldReference>()!!

        val setClientInfoFieldInstructions = setClientInfoFieldsFingerprint.method.instructions.filter {
            (it.opcode == Opcode.IPUT_OBJECT || it.opcode == Opcode.IPUT) &&
                it.getReference<FieldReference>()!!.definingClass == clientInfoField.type
        }.map { it.getReference<FieldReference>()!! }

        // Offsets are known for the fields in the clientInfo object.
        val clientIdField = setClientInfoFieldInstructions[0]
        val clientModelField = setClientInfoFieldInstructions[5]
        val osVersionField = setClientInfoFieldInstructions[7]
        val clientVersionField = setClientInfoClientVersionFingerprint.method
            .getInstruction(setClientInfoClientVersionFingerprint.stringMatches.first().index + 1)
            .getReference<FieldReference>()

        // Helper method to spoof the client info.
        val spoofClientInfoMethod = ImmutableMethod(
            playerRequestClass.type,
            "spoofClientInfo",
            listOf(ImmutableMethodParameter(clientInfoContainerClass, null, null)),
            "V",
            AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(3),
        ).toMutable().also(playerRequestClass.methods::add).apply {
            addInstructions(
                """
                    iget-object v0, p0, $clientInfoField
                
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->getClientId()I
                    move-result v1
                    iput v1, v0, $clientIdField
    
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->getClientModel()Ljava/lang/String;
                    move-result-object v1
                    iput-object v1, v0, $clientModelField
                
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->getClientVersion()Ljava/lang/String;
                    move-result-object v1
                    iput-object v1, v0, $clientVersionField
    
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->getOsVersion()Ljava/lang/String;
                    move-result-object v1
                    iput-object v1, v0, $osVersionField
    
                    return-void
                """,
            )
        }

        createPlayerRequestBodyMatch.method.apply {
            val checkCastIndex = createPlayerRequestBodyMatch.filterMatches.first().index
            val clientInfoContainerRegister = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

            addInstruction(checkCastIndex + 1, "invoke-static {v$clientInfoContainerRegister}, $spoofClientInfoMethod")
        }
    }
}
