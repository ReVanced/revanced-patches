package app.revanced.patches.youtube.ad.general.bytecode.patch

import app.revanced.extensions.injectHideCall
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.data.implementation.proxy
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.addMethod
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.findMutableMethodOf
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.insertBlocks
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.toDescriptor
import app.revanced.patches.youtube.ad.general.bytecode.utils.MethodUtils.createMutableMethod
import app.revanced.patches.youtube.ad.general.resource.patch.GeneralResourceAdsPatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.*
import org.jf.dexlib2.iface.MethodImplementation
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction22c
import org.jf.dexlib2.iface.instruction.formats.Instruction31i
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference

@Patch
@Dependencies(
    dependencies = [ResourceIdMappingProviderResourcePatch::class, IntegrationsPatch::class, GeneralResourceAdsPatch::class]
)
@Name("general-ads")
@Description("Patch to remove general ads in bytecode.")
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralBytecodeAdsPatch : BytecodePatch(
    listOf()
) {
    // a constant used by litho
    private val lithoConstant = 0xaed2868

    // list of resource names to get the id of
    private val resourceIds = arrayOf(
        "ad_attribution",
        "reel_multiple_items_shelf",
        "info_cards_drawer_header",
        "endscreen_element_layout_video",
        "endscreen_element_layout_circle",
        "endscreen_element_layout_icon",
        "promoted_video_item_land",
        "promoted_video_item_full_bleed",
    ).map {
        ResourceIdMappingProviderResourcePatch.resourceMappings[it]!!
    }

    private val stringReferences = arrayOf(
        "Claiming to use more elements than provided",
        "loadVideo() called on LocalDirector in wrong state",
        "LoggingProperties are not in proto format"
    )

    override fun execute(data: BytecodeData): PatchResult {
        // iterating through all classes is expensive
        for (classDef in data.classes) {
            var mutableClass: MutableClass? = null

            method@ for (method in classDef.methods) {
                var mutableMethod: MutableMethod? = null

                if (method.implementation == null) continue@method

                val instructions = method.implementation!!.instructions
                instructions.forEachIndexed { index, instruction ->
                    when (instruction.opcode) {
                        Opcode.CONST -> {
                            // TODO: find a way to de-duplicate code.
                            //  The issue is we need to save mutableClass and mutableMethod to the existing fields
                            when ((instruction as Instruction31i).wideLiteral) {
                                resourceIds[0] -> { // general ads
                                    //  and is followed by an instruction with the mnemonic INVOKE_VIRTUAL
                                    val insertIndex = index + 1
                                    val invokeInstruction = instructions.elementAt(insertIndex)
                                    if (invokeInstruction.opcode != Opcode.INVOKE_VIRTUAL) return@forEachIndexed

                                    // create proxied method, make sure to not re-resolve() the current class
                                    if (mutableClass == null) mutableClass = data.proxy(classDef).resolve()
                                    if (mutableMethod == null) mutableMethod =
                                        mutableClass!!.findMutableMethodOf(method)

                                    // insert hide call to hide the view corresponding to the resource
                                    val viewRegister = (invokeInstruction as Instruction35c).registerC
                                    mutableMethod!!.implementation!!.injectHideCall(insertIndex, viewRegister)

                                }

                                resourceIds[1] -> { // reel ads
                                    //  and is followed by an instruction at insertIndex with the mnemonic IPUT_OBJECT
                                    val insertIndex = index + 4
                                    val iPutInstruction = instructions.elementAt(insertIndex)
                                    if (iPutInstruction.opcode != Opcode.IPUT_OBJECT) return@forEachIndexed

                                    // create proxied method, make sure to not re-resolve() the current class
                                    if (mutableClass == null) mutableClass = data.proxy(classDef).resolve()
                                    if (mutableMethod == null) mutableMethod =
                                        mutableClass!!.findMutableMethodOf(method)

                                    val viewRegister = (iPutInstruction as Instruction22c).registerA
                                    mutableMethod!!.implementation!!.injectHideCall(insertIndex, viewRegister)
                                }

                                resourceIds[2] -> { // info cards ads
                                    //  and is followed by an instruction with the mnemonic INVOKE_VIRTUAL
                                    val removeIndex = index - 1
                                    val invokeInstruction = instructions.elementAt(removeIndex)
                                    if (invokeInstruction.opcode != Opcode.INVOKE_VIRTUAL) return@forEachIndexed

                                    // create proxied method, make sure to not re-resolve() the current class
                                    if (mutableClass == null) mutableClass = data.proxy(classDef).resolve()
                                    if (mutableMethod == null) mutableMethod =
                                        mutableClass!!.findMutableMethodOf(method)

                                    mutableMethod!!.implementation!!.removeInstruction(removeIndex)
                                }

                                resourceIds[3], resourceIds[4], resourceIds[5] -> { // end screen ads
                                    //  and is followed by an instruction with the mnemonic IPUT_OBJECT
                                    val insertIndex = index + 7
                                    val invokeInstruction = instructions.elementAt(insertIndex)
                                    if (invokeInstruction.opcode != Opcode.IPUT_OBJECT) return@forEachIndexed

                                    // create proxied method, make sure to not re-resolve() the current class
                                    if (mutableClass == null) mutableClass = data.proxy(classDef).resolve()
                                    if (mutableMethod == null) mutableMethod =
                                        mutableClass!!.findMutableMethodOf(method)

                                    // TODO: dynamically get registers
                                    mutableMethod!!.addInstructions(
                                        insertIndex, """
                                                const/16 v1, 0x8
                                                invoke-virtual {v0,v1}, Landroid/widget/FrameLayout;->setVisibility(I)V
                                            """
                                    )
                                }

                                resourceIds[6] -> {
                                    //  and is followed by an instruction with the mnemonic INVOKE_DIRECT
                                    val insertIndex = index + 3
                                    val invokeInstruction = instructions.elementAt(insertIndex)
                                    if (invokeInstruction.opcode != Opcode.INVOKE_DIRECT) return@forEachIndexed

                                    // create proxied method, make sure to not re-resolve() the current class
                                    if (mutableClass == null) mutableClass = data.proxy(classDef).resolve()
                                    if (mutableMethod == null) mutableMethod =
                                        mutableClass!!.findMutableMethodOf(method)

                                    // insert hide call to hide the view corresponding to the resource
                                    val viewRegister = (invokeInstruction as Instruction35c).registerE
                                    mutableMethod!!.implementation!!.injectHideCall(insertIndex, viewRegister)
                                }

                                resourceIds[7] -> {
                                    // TODO, go to class, hide the inflated view
                                }
                            }
                        }

                        Opcode.CONST_STRING -> {

                            when (((instruction as Instruction21c).reference as StringReference).string) {
                                stringReferences[0] -> {
                                    val stringInstruction = instructions.elementAt(3)
                                    if (stringInstruction.opcode == Opcode.CONST_STRING) return@forEachIndexed

                                    // create proxied method, make sure to not re-resolve() the current class
                                    if (mutableClass == null) mutableClass = data.proxy(classDef).resolve()
                                    if (mutableMethod == null) mutableMethod =
                                        mutableClass!!.findMutableMethodOf(method)

                                    // return the method
                                    val insertIndex = 1 // after super constructor
                                    mutableMethod!!.implementation!!.addInstruction(
                                        insertIndex, BuilderInstruction10x(Opcode.RETURN_VOID)
                                    )
                                }

                                stringReferences[1] -> {
                                    // TODO: migrate video ads patch to here if necessary
                                }

                                stringReferences[2] -> { // Litho ads
                                    // create proxied method.
                                    val proxy = data.proxy(classDef)
                                    val mutableClass = proxy.resolve()

                                    // add getIsEmpty method
                                    mutableClass.addGetIsEmptyMethod()

                                    // get required method to patch and get references from
                                    val lithoMethod = getLithoMethod(mutableClass)
                                        ?: return PatchResultError("Could not find required litho method to patch.")
                                    val lithoMethodImplementation = lithoMethod.implementation!!

                                    // create and add getTemplateName method
                                    val getTemplateMethod =
                                        mutableClass.createGetTemplateNameMethod(lithoMethodImplementation)
                                    mutableClass.addMethod(getTemplateMethod)

                                    val lithoInstructions = lithoMethodImplementation.instructions
                                    val thisType = mutableClass.type
                                    val templateNameParameterType = getTemplateMethod.parameterTypes.first()

                                    // get reference descriptors
                                    val indexOfReference1 = lithoInstructions.indexOfFirst {
                                        it.opcode == Opcode.INVOKE_STATIC_RANGE
                                    }
                                    val descriptor1 =
                                        lithoInstructions.elementAt(indexOfReference1).toDescriptor<MethodReference>()
                                    val descriptor2 = lithoInstructions.elementAt(indexOfReference1 + 2)
                                        .toDescriptor<FieldReference>()

                                    // create label
                                    val lithoRemoveLabel = lithoMethodImplementation.newLabelForIndex(0)

                                    // create branch instructions
                                    val ifEqzFirstInstruction =
                                        BuilderInstruction21t(Opcode.IF_EQZ, 0, lithoRemoveLabel)
                                    val ifEqzSecondInstruction =
                                        BuilderInstruction21t(Opcode.IF_EQZ, 1, lithoRemoveLabel)

                                    // create blocks
                                    val block1 = """
                                        invoke-static/range {p3}, $thisType->getTemplateName($templateNameParameterType)Ljava/lang/String;
                                        move-result-object v0
                                    """.trimIndent().toInstructions(lithoMethod)
                                    val block2 = """
                                        move-object/from16 v1, p3
                                        iget-object v2, v1, $templateNameParameterType->b:Ljava/nio/ByteBuffer;
                                        invoke-static {v0, v2}, Lapp/revanced/integrations/patches/GeneralBytecodeAdsPatch;->containsAd(Ljava/lang/String;Ljava/nio/ByteBuffer;)Z
                                        move-result v1
                                    """.trimIndent().toInstructions(lithoMethod)
                                    val block3 = """
                                        move-object/from16 v2, p1
                                        invoke-static {v2}, $descriptor1
                                        move-result-object v0
                                        iget-object v0, v0, $descriptor2
                                        return-object v0
                                    """.trimIndent().toInstructions(lithoMethod)

                                    // insert blocks and branch instructions
                                    lithoMethodImplementation.insertBlocks(
                                        0,
                                        block1,
                                        listOf(ifEqzFirstInstruction),
                                        block2,
                                        listOf(ifEqzSecondInstruction),
                                        block3,
                                    )
                                }
                            }

                        }

                        else -> return@forEachIndexed
                    }
                }
            }
        }
        return PatchResultSuccess()
    }

    private fun getLithoMethod(mutableClass: MutableClass) = mutableClass.methods.firstOrNull {
        it.implementation?.instructions?.any { instruction ->
            instruction.opcode == Opcode.CONST && (instruction as Instruction31i).narrowLiteral == lithoConstant
        } ?: false
    }

    private fun MutableClass.addGetIsEmptyMethod() {
        val getIsEmptyImplementation = MutableMethodImplementation(1)

        // create target instructions
        val firstTargetInstruction = BuilderInstruction11n(Opcode.CONST_4, 0, 1)
        val secondTargetInstruction = BuilderInstruction11n(Opcode.CONST_4, 0, 0)

        // add instructions to the instruction list
        getIsEmptyImplementation.addInstructions(
            0, listOf(
                // BuilderInstruction21t(Opcode.IF_EQZ, 0, first),
                BuilderInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    0,
                    0,
                    0,
                    0,
                    0,
                    ImmutableMethodReference("Ljava/lang/String;", "isEmpty", null, "Z")
                ),
                BuilderInstruction11x(Opcode.MOVE_RESULT, 0),
                // BuilderInstruction21t(Opcode.IF_EQZ, 0, second),
                // BuilderInstruction10t(Opcode.GOTO, first),
                secondTargetInstruction,
                BuilderInstruction11x(Opcode.RETURN, 0),
                firstTargetInstruction,
                BuilderInstruction11x(Opcode.RETURN, 0),
            )
        )

        val getIsEmptyInstructions = getIsEmptyImplementation.instructions

        // create labels for the target instructions
        val firstLabel =
            getIsEmptyImplementation.newLabelForIndex(getIsEmptyInstructions.indexOf(firstTargetInstruction))
        val secondLabel =
            getIsEmptyImplementation.newLabelForIndex(getIsEmptyInstructions.indexOf(secondTargetInstruction))

        // create branch instructions to the labels
        val ifEqzFirstInstruction = BuilderInstruction21t(Opcode.IF_EQZ, 0, firstLabel)
        val ifEqzSecondInstruction = BuilderInstruction21t(Opcode.IF_EQZ, 0, secondLabel)
        val gotoInstruction = BuilderInstruction10t(Opcode.GOTO, firstLabel)

        // insert remaining branch instructions, order of adding those instructions is important
        getIsEmptyImplementation.addInstructions(
            2, listOf(
                ifEqzSecondInstruction, gotoInstruction
            )
        )
        getIsEmptyImplementation.addInstruction(
            0, ifEqzFirstInstruction
        )

        this.addMethod(
            createMutableMethod(
                this.type, "getIsEmpty", "Z", "Ljava/lang/String;", getIsEmptyImplementation
            )
        )
    }

    private fun MutableClass.createGetTemplateNameMethod(lithoMethodImplementation: MethodImplementation): MutableMethod {
        var counter = 1
        val descriptors = buildList {
            for (instruction in lithoMethodImplementation.instructions) {
                if (instruction !is ReferenceInstruction) continue
                if (counter++ > 4) break

                add(instruction.toDescriptor<MethodReference>())
            }
        }

        val getTemplateNameImplementation = MutableMethodImplementation(2)

        // create code blocks
        val block1 = """
            invoke-virtual {p0}, ${descriptors[0]}
            move-result-object p0
            const v0, $lithoConstant
            invoke-static {p0, v0}, ${descriptors[1]}
            move-result-object p0
        """.trimIndent().toInstructions()
        val block2 = """
              invoke-static {p0}, ${descriptors[2]}
              move-result-object p0
              invoke-virtual {p0}, ${descriptors[3]}
              move-result-object v0
              invoke-static {v0}, ${this.type}->getIsEmpty(Ljava/lang/String;)Z
              move-result v0
        """.trimIndent().toInstructions()
        val block3 = """
              invoke-virtual {p0}, ${descriptors[3]}
              move-result-object p0
              return-object p0
        """.trimIndent().toInstructions()

        // create target instruction
        val targetInstruction = BuilderInstruction11n(Opcode.CONST_4, 1, 0)
        // and remaining instruction
        val returnInstruction = BuilderInstruction11x(Opcode.RETURN_OBJECT, 1)

        // insert blocks and instructions
        getTemplateNameImplementation.insertBlocks(
            0,
            block1,
            block2,
            block3,
            listOf(
                targetInstruction, returnInstruction
            ),
        )

        // create label for target instruction
        val targetInstructionLabel =
            getTemplateNameImplementation.newLabelForIndex(getTemplateNameImplementation.instructions.size - 2)

        // create branch instructions to the label
        val ifEqzInstruction = BuilderInstruction21t(Opcode.IF_EQZ, 1, targetInstructionLabel)
        val ifNezInstruction = BuilderInstruction21t(Opcode.IF_NEZ, 0, targetInstructionLabel)

        // insert branch instructions
        getTemplateNameImplementation.addInstruction(
            block1.size, ifEqzInstruction
        )
        getTemplateNameImplementation.addInstruction(
            block1.size + block2.size + 1, ifNezInstruction
        )

        // create the method
        return createMutableMethod(
            this.type,
            "getTemplateName",
            "Ljava/lang/String;",
            descriptors[0].split("->")[0], // a bit weird to get the type this way,
            getTemplateNameImplementation
        )
    }
}
