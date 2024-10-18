package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ComponentContextParserFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.EmptyComponentFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.LithoFilterFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ProtobufBufferReferenceFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ReadComponentIdentifierFingerprint
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Field
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.io.Closeable

@Patch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
    dependencies = [IntegrationsPatch::class, VersionCheckPatch::class]
)
@Suppress("unused")
object LithoFilterPatch : BytecodePatch(
    setOf(
        ComponentContextParserFingerprint,
        LithoFilterFingerprint,
        ProtobufBufferReferenceFingerprint,
        ReadComponentIdentifierFingerprint,
        EmptyComponentFingerprint
    )
), Closeable {
    private val Instruction.descriptor
        get() = (this as ReferenceInstruction).reference.toString()

    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/components/LithoFilterPatch;"

    internal lateinit var addFilter: (String) -> Unit
        private set

    private var filterCount = 0

    /**
     * The following patch inserts a hook into the method that parses the bytes into a ComponentContext.
     * This method contains a StringBuilder object that represents the pathBuilder of the component.
     * The pathBuilder is used to filter components by their path.
     *
     * Additionally, the method contains a reference to the components identifier.
     * The identifier is used to filter components by their identifier.
     *
     * The protobuf buffer is passed along from a different injection point before the filtering occurs.
     * The buffer is a large byte array that represents the component tree.
     * This byte array is searched for strings that indicate the current component.
     *
     * The following pseudo code shows how the patch works:
     *
     * class SomeOtherClass {
     *    // Called before ComponentContextParser.parseBytesToComponentContext method.
     *    public void someOtherMethod(ByteBuffer byteBuffer) {
     *        IntegrationsClass.setProtoBuffer(byteBuffer); // Inserted by this patch.
     *        ...
     *   }
     * }
     *
     * When patching 19.17 and earlier:
     *
     * class ComponentContextParser {
     *    public ComponentContext ReadComponentIdentifierFingerprint(...) {
     *        ...
     *        if (IntegrationsClass.filter(identifier, pathBuilder)); // Inserted by this patch.
     *            return emptyComponent;
     *        ...
     *    }
     * }
     *
     * When patching 19.18 and later:
     *
     * class ComponentContextParser {
     *    public ComponentContext parseBytesToComponentContext(...) {
     *        ...
     *        if (ReadComponentIdentifierFingerprint() == null); // Inserted by this patch.
     *            return emptyComponent;
     *        ...
     *    }
     *
     *    public ComponentIdentifierObj readComponentIdentifier(...) {
     *        ...
     *        if (IntegrationsClass.filter(identifier, pathBuilder)); // Inserted by this patch.
     *            return null;
     *        ...
     *    }
     * }
     */
    override fun execute(context: BytecodeContext) {

        // Remove dummy filter from Integrations static field
        // and add the filters included during patching.
        LithoFilterFingerprint.resultOrThrow().mutableMethod.apply {
            removeInstructions(2, 4) // Remove dummy filter.

            addFilter = { classDescriptor ->
                addInstructions(
                    2,
                    """
                        new-instance v1, $classDescriptor
                        invoke-direct {v1}, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v1, v0, v2
                    """
                )
            }
        }

        // region Pass the buffer into Integrations.

        ProtobufBufferReferenceFingerprint.resultOrThrow().mutableMethod.addInstruction(
            0,
            " invoke-static { p2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V"
        )

        // endregion

        // region Hook the method that parses bytes into a ComponentContext.

        var readComponentMethod : Method
        var builderMethodDescriptor : Method
        val emptyComponentField : Field

        ComponentContextParserFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                // Get the only static method in the class.
                builderMethodDescriptor = EmptyComponentFingerprint.resultOrThrow().classDef
                    .methods.first { method -> AccessFlags.STATIC.isSet(method.accessFlags) }
                // Only one field.
                emptyComponentField = context.findClass(builderMethodDescriptor.returnType)!!
                    .immutableClass.fields.single()
                readComponentMethod = ReadComponentIdentifierFingerprint.resultOrThrow().method

                // 19.18 and later require patching 2 methods instead of one.
                // Otherwise the patched code is the same.
                if (VersionCheckPatch.is_19_18_or_greater) {
                    // Get the method name of the ReadComponentIdentifierFingerprint call.
                    val readComponentMethodCallIndex = indexOfFirstInstructionOrThrow {
                        val reference = getReference<MethodReference>()
                        reference?.definingClass == readComponentMethod.definingClass
                                && reference.name == readComponentMethod.name
                    }

                    // Result of read component, and also a free register.
                    val register = getInstruction<OneRegisterInstruction>(
                        readComponentMethodCallIndex + 1
                    ).registerA

                    // Insert after 'move-result-object'
                    val insertHookIndex = readComponentMethodCallIndex + 2

                    // Return an EmptyComponent instead of the original component if the filterState method returns true.
                    addInstructionsWithLabels(
                        insertHookIndex,
                        """
                            if-nez v$register, :unfiltered
    
                            # Component was filtered in ReadComponentIdentifierFingerprint hook
                            move-object/from16 v$register, p1
                            invoke-static { v$register }, $builderMethodDescriptor
                            move-result-object v$register
                            iget-object v$register, v$register, $emptyComponentField
                            return-object v$register
                        """,
                        ExternalLabel("unfiltered", getInstruction(insertHookIndex))
                    )
                }
            }
        }

        // endregion

        // region Read component then store the result.

        ReadComponentIdentifierFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val insertHookIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IPUT_OBJECT &&
                            getReference<FieldReference>()?.type == "Ljava/lang/StringBuilder;"
                }
                val stringBuilderRegister = getInstruction<TwoRegisterInstruction>(insertHookIndex).registerA

                // Identifier is saved to a field just before the string builder.
                val identifierRegister = getInstruction<TwoRegisterInstruction>(
                    indexOfFirstInstructionReversedOrThrow(insertHookIndex) {
                        opcode == Opcode.IPUT_OBJECT
                                && getReference<FieldReference>()?.type == "Ljava/lang/String;"
                    }
                ).registerA

                // Find a free temporary register.
                val register = getInstruction<OneRegisterInstruction>(
                    // Immediately before is a StringBuilder append constant character.
                    indexOfFirstInstructionReversedOrThrow(insertHookIndex, Opcode.CONST_16)
                ).registerA

                // Verify the temp register will not clobber the method result register.
                if (stringBuilderRegister == register) {
                    throw PatchException("Free register will clobber StringBuilder register")
                }

                val commonInstructions = """
                    invoke-static { v$identifierRegister, v$stringBuilderRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                    move-result v$register
                    if-eqz v$register, :unfiltered
                """

                addInstructionsWithLabels(
                    insertHookIndex,
                    if (VersionCheckPatch.is_19_18_or_greater) """
                        $commonInstructions
                        
                        # Return null, and the ComponentContextParserFingerprint hook 
                        # handles returning an empty component.
                        const/4 v$register, 0x0
                        return-object v$register
                    """
                    else """
                        $commonInstructions
                        
                        # Exact same code as ComponentContextParserFingerprint hook,
                        # but with the free register of this method.
                        move-object/from16 v$register, p1
                        invoke-static { v$register }, $builderMethodDescriptor
                        move-result-object v$register
                        iget-object v$register, v$register, $emptyComponentField
                        return-object v$register
                    """,
                    ExternalLabel("unfiltered", getInstruction(insertHookIndex))
                )
            }
        }

        // endregion
    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.replaceInstruction(0, "const/16 v0, $filterCount")
}
