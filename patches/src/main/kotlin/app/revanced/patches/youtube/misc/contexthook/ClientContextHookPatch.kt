package app.revanced.patches.youtube.misc.contexthook

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.accessFlags
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.cloneMutableAndPreserveParameters
import app.revanced.util.findInstructionIndicesReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private lateinit var browseIdField: FieldReference
private lateinit var clientInfoField: FieldReference
private lateinit var clientVersionField: FieldReference
private lateinit var messageLiteBuilderField: FieldReference
private lateinit var messageLiteBuilderMethod: MethodReference
private lateinit var osNameField: FieldReference

enum class Endpoint(
    vararg val getEndpointMethods: BytecodePatchContext.() -> Method,
    var instructions: String = "",
) {
    BROWSE(
        BytecodePatchContext::browseEndpointParentMethod::get
    ),
    GUIDE(
        BytecodePatchContext::guideEndpointConstructorMethod::get
    ),
    REEL(
        BytecodePatchContext::reelCreateItemsEndpointConstructorMethod::get,
        BytecodePatchContext::reelItemWatchEndpointConstructorMethod::get,
        BytecodePatchContext::reelWatchSequenceEndpointConstructorMethod::get,
    ),
    SEARCH(BytecodePatchContext::searchRequestBuildParametersMethod::get)
}

val hookClientContextPatch = bytecodePatch(
    description = "Hooks the context body of the endpoint.",
) {
    dependsOn(sharedExtensionPatch)

    apply {
        buildDummyClientContextBodyMethodMatch.let {
            it.method.apply {
                val clientInfoIndex = it[-1]
                val clientVersionIndex = it[2]
                val messageLiteBuilderIndex = it[0]

                clientInfoField =
                    getInstruction<ReferenceInstruction>(clientInfoIndex).fieldReference!!
                clientVersionField =
                    getInstruction<ReferenceInstruction>(clientVersionIndex).fieldReference!!
                messageLiteBuilderField =
                    getInstruction<ReferenceInstruction>(messageLiteBuilderIndex).fieldReference!!
            }
        }

        authenticationChangeListenerMethod.apply {
            val messageLiteBuilderIndex =
                indexOfMessageLiteBuilderReference(this, messageLiteBuilderField.definingClass)

            messageLiteBuilderMethod =
                getInstruction<ReferenceInstruction>(messageLiteBuilderIndex).methodReference!!
        }

        buildClientContextBodyConstructorMethod.immutableClassDef.buildClientContextBodyMethodMatch.let {
            it.method.apply {
                val osNameIndex = it[1]

                osNameField =
                    getInstruction<ReferenceInstruction>(osNameIndex).fieldReference!!
            }
        }

        browseEndpointParentMethod.immutableClassDef.browseEndpointConstructorMethodMatch.let {
            it.method.apply {
                val browseIdIndex = it[-1]
                browseIdField =
                    getInstruction<ReferenceInstruction>(browseIdIndex).fieldReference!!
            }
        }
    }

    afterDependents {
        val helperMethodName = "patch_setClientContext"

        Endpoint.entries.filter {
            it.instructions.isNotEmpty()
        }.forEach { endpoint ->
            endpoint.getEndpointMethods.forEach { getEndpointRequestBodyParentMethod ->
                getEndpointRequestBodyParentMethod().immutableClassDef.firstMethodDeclaratively {
                    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
                    returnType("V")
                    parameterTypes()
                }.cloneMutableAndPreserveParameters().let {
                    it.classDef.methods.add(
                        ImmutableMethod(
                            it.definingClass,
                            helperMethodName,
                            emptyList(),
                            "V",
                            AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
                            it.annotations,
                            null,
                            MutableMethodImplementation(5),
                        ).toMutable().apply {
                            addInstructionsWithLabels(
                                0,
                                """
                                    invoke-virtual { p0 }, $messageLiteBuilderMethod
                                    move-result-object v0
                                    iget-object v0, v0, $messageLiteBuilderField
                                    check-cast v0, ${clientInfoField.definingClass}
                                    iget-object v1, v0, $clientInfoField
                                    if-eqz v1, :ignore
                                """ + endpoint.instructions +
                                        """
                                    :ignore
                                    return-void
                                """,
                            )
                        }
                    )

                   it.findInstructionIndicesReversedOrThrow(Opcode.RETURN_VOID).forEach { index ->
                        it.addInstructionsAtControlFlowLabel(
                            index,
                            "invoke-direct/range { p0 .. p0 }, ${it.definingClass}->$helperMethodName()V"
                        )
                    }
                }
            }
        }
    }
}

fun addClientVersionHook(endPoint: Endpoint, descriptor: String) {
    endPoint.instructions += if (endPoint == Endpoint.BROWSE) """
        iget-object v3, p0, $browseIdField
        iget-object v2, v1, $clientVersionField
        invoke-static { v3, v2 }, $descriptor
        move-result-object v2
        iput-object v2, v1, $clientVersionField
        """ else """
        iget-object v2, v1, $clientVersionField
        invoke-static { v2 }, $descriptor
        move-result-object v2
        iput-object v2, v1, $clientVersionField
        """
}

fun addOSNameHook(endPoint: Endpoint, descriptor: String) {
    endPoint.instructions += """
        iget-object v2, v1, $osNameField
        invoke-static { v2 }, $descriptor
        move-result-object v2
        iput-object v2, v1, $osNameField
        """
}
