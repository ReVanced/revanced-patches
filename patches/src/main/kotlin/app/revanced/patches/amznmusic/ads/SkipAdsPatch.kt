package app.revanced.patches.amznmusic.ads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.patches.amznmusic.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.registersUsed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val skipAdsPatch = bytecodePatch(
    name = "Skip ads",
    description = "Skip ads between the tracks.",
) {
    compatibleWith("com.amazon.mp3")

    dependsOn(sharedExtensionPatch)

    apply {
        firstClassDef { type == "Lcom/amazon/digitalmusicxp/inputs/GetNextEntityInput;" }
            .instanceFields.find { it.name == "currentEntityReferenceId" }!!.accessFlags = AccessFlags.PUBLIC.value

        val getNextTracksMethod = firstMethodDeclaratively{
            definingClass("Lcom/amazon/mp3/amplifyqueue/AmplifyClient;")
            returnType("Lcom/amazon/digitalmusicxp/callbacks/Outcome;")
            name("getNextTracks")
            accessFlags(AccessFlags.PUBLIC)
        }

        getNextTracksMethod.apply {
            val getNextEntityMethodIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lcom/amazon/mp3/amplifyqueue/AmplifyClient;"
                        && reference.returnType == "Lcom/amazon/digitalmusicxp/callbacks/Outcome;"
                        && reference.parameterTypes == listOf("Lcom/amazon/digitalmusicxp/inputs/GetNextEntityInput;")
            }

            val instruction = getInstruction(getNextEntityMethodIndex)

            replaceInstruction(getNextEntityMethodIndex,
                """
                        invoke-static {v${instruction.registersUsed[0]}, v${instruction.registersUsed[1]}}, Lapp/revanced/extension/amznmusic/patches/SkipAdsPatch;->getNextEntity(Lcom/amazon/mp3/amplifyqueue/AmplifyClient;Lcom/amazon/digitalmusicxp/inputs/GetNextEntityInput;)Lcom/amazon/digitalmusicxp/callbacks/Outcome;
                    """)
        }
    }
}
