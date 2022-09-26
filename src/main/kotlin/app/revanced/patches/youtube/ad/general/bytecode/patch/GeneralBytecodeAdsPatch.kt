package app.revanced.patches.youtube.ad.general.bytecode.patch

import app.revanced.extensions.injectHideCall
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.addMethod
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.findMutableMethodOf
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.insertBlocks
import app.revanced.patches.youtube.ad.general.bytecode.extensions.MethodExtensions.toDescriptor
import app.revanced.patches.youtube.ad.general.bytecode.utils.MethodUtils.createMutableMethod
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
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
@DependsOn([ResourceMappingResourcePatch::class, IntegrationsPatch::class, SettingsPatch::class])
@Name("general-ads")
@Description("Removes general ads.")
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralBytecodeAdsPatch : BytecodePatch() {
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
    ).map { name ->
        ResourceMappingResourcePatch.resourceMappings.single { it.name == name }.id
    }

    private val stringReferences = arrayOf(
        "Claiming to use more elements than provided",
        "loadVideo() called on LocalDirector in wrong state",
        "LoggingProperties are not in proto format"
    )

    override fun execute(data: BytecodeData): PatchResult {
        SettingsPatch.PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_home_ads_removal",
                StringResource("revanced_home_ads_removal_title", "Remove home ads"),
                true,
                StringResource("revanced_home_ads_removal_summary_on", "Home ads are hidden"),
                StringResource("revanced_home_ads_removal_summary_off", "Home ads are shown")
            ),
            SwitchPreference(
                "revanced_adremover_ad_removal",
                StringResource("revanced_adremover_ad_removal_enabled_title", "Remove general ads"),
                true,
                StringResource("revanced_adremover_ad_removal_enabled_summary_on", "General ads are hidden"),
                StringResource("revanced_adremover_ad_removal_enabled_summary_off", "General ads are shown")
            ),
            SwitchPreference(
                "revanced_adremover_merchandise",
                StringResource("revanced_adremover_merchandise_enabled_title", "Remove merchandise banners"),
                true,
                StringResource("revanced_adremover_merchandise_enabled_summary_on", "Merchandise banners are hidden"),
                StringResource("revanced_adremover_merchandise_enabled_summary_off", "Merchandise banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_community_posts_removal",
                StringResource("revanced_adremover_community_posts_enabled_title", "Remove community posts"),
                true,
                StringResource("revanced_adremover_community_posts_enabled_summary_on", "Community posts are hidden"),
                StringResource("revanced_adremover_community_posts_enabled_summary_off", "Community posts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_compact_banner_removal",
                StringResource("revanced_adremover_compact_banner_enabled_title", "Remove compact banners"),
                true,
                StringResource("revanced_adremover_compact_banner_enabled_summary_on", "Compact banners are hidden"),
                StringResource("revanced_adremover_compact_banner_enabled_summary_off", "Compact banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_comments_removal",
                StringResource("revanced_adremover_comments_enabled_title", "Remove comments section"),
                false,
                StringResource("revanced_adremover_comments_enabled_summary_on", "Comment section is hidden"),
                StringResource("revanced_adremover_comments_enabled_summary_off", "Comment section is shown")
            ),
            SwitchPreference(
                "revanced_adremover_movie",
                StringResource("revanced_adremover_movie_enabled_title", "Remove movies section"),
                true,
                StringResource("revanced_adremover_movie_enabled_summary_on", "Movies section is hidden"),
                StringResource("revanced_adremover_movie_enabled_summary_off", "Movies section is shown")
            ),
            SwitchPreference(
                "revanced_adremover_feed_survey",
                StringResource("revanced_adremover_feed_survey_enabled_title", "Remove feed surveys"),
                true,
                StringResource("revanced_adremover_feed_survey_enabled_summary_on", "Feed surveys are hidden"),
                StringResource("revanced_adremover_feed_survey_enabled_summary_off", "Feed surveys are shown")
            ),
            SwitchPreference(
                "revanced_adremover_shorts_shelf",
                StringResource("revanced_adremover_shorts_shelf_enabled_title", "Remove shorts shelf"),
                true,
                StringResource("revanced_adremover_shorts_shelf_enabled_summary_on", "Shorts shelves are hidden"),
                StringResource("revanced_adremover_shorts_shelf_enabled_summary_off", "Shorts shelves are shown")
            ),
            SwitchPreference(
                "revanced_adremover_community_guidelines",
                StringResource("revanced_adremover_community_guidelines_enabled_title", "Remove community guidelines"),
                true,
                StringResource("revanced_adremover_community_guidelines_enabled_summary_on", "Community guidelines are hidden"),
                StringResource("revanced_adremover_community_guidelines_enabled_summary_off", "Community guidelines are shown")
            ),
            SwitchPreference(
                "revanced_adremover_emergency_box_removal",
                StringResource("revanced_adremover_emergency_box_enabled_title", "Remove emergency boxes"),
                true,
                StringResource("revanced_adremover_emergency_box_enabled_summary_on", "Emergency boxes are hidden"),
                StringResource("revanced_adremover_emergency_box_enabled_summary_off", "Emergency boxes are shown")
            ),
            SwitchPreference(
                "revanced_adremover_info_panel",
                StringResource("revanced_adremover_info_panel_enabled_title", "Remove info panels"),
                true,
                StringResource("revanced_adremover_info_panel_enabled_summary_on", "Merchandise banners are hidden"),
                StringResource("revanced_adremover_info_panel_enabled_summary_off", "Merchandise banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_medical_panel",
                StringResource("revanced_adremover_medical_panel_enabled_title", "Remove medical panels"),
                true,
                StringResource("revanced_adremover_medical_panel_enabled_summary_on", "Medical panels are hidden"),
                StringResource("revanced_adremover_medical_panel_enabled_summary_off", "Medical panels are shown")
            ),
            SwitchPreference(
                "revanced_adremover_paid_content",
                StringResource("revanced_adremover_paid_content_enabled_title", "Remove paid content"),
                true,
                StringResource("revanced_adremover_paid_content_enabled_summary_on", "Paid content is hidden"),
                StringResource("revanced_adremover_paid_content_enabled_summary_off", "Paid content is shown")
            ),
            SwitchPreference(
                "revanced_adremover_suggested",
                StringResource("revanced_adremover_suggested_enabled_title", "Remove personal suggestions"),
                true,
                StringResource("revanced_adremover_suggested_enabled_summary_on", "Personal suggestions are hidden"),
                StringResource("revanced_adremover_suggested_enabled_summary_off", "Personal suggestions are shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_suggestions",
                StringResource("revanced_adremover_hide_suggestions_enabled_title", "Hide suggestions"),
                true,
                StringResource("revanced_adremover_hide_suggestions_enabled_summary_on", "Suggestions are hidden"),
                StringResource("revanced_adremover_hide_suggestions_enabled_summary_off", "Suggestions are shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_latest_posts",
                StringResource("revanced_adremover_hide_latest_posts_enabled_title", "Hide latest posts"),
                true,
                StringResource("revanced_adremover_hide_latest_posts_enabled_summary_on", "Latest posts are hidden"),
                StringResource("revanced_adremover_hide_latest_posts_enabled_summary_off", "Latest posts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_channel_guidelines",
                StringResource("revanced_adremover_hide_channel_guidelines_enabled_title", "Hide channel guidelines"),
                true,
                StringResource("revanced_adremover_hide_channel_guidelines_enabled_summary_on", "Channel guidelines are hidden"),
                StringResource("revanced_adremover_hide_channel_guidelines_enabled_summary_off", "Channel guidelines are shown")
            ),
        )

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

                                    //ToDo: Add Settings toggle for whatever this is
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
                                    //ToDo: Add setting here
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
                                    val proxiedClass = proxy.resolve()

                                    // add getIsEmpty method
                                    proxiedClass.addGetIsEmptyMethod()

                                    // get required method to patch and get references from
                                    val lithoMethod = getLithoMethod(proxiedClass)
                                        ?: return PatchResultError("Could not find required litho method to patch.")
                                    val lithoMethodImplementation = lithoMethod.implementation!!

                                    // create and add getTemplateName method
                                    val getTemplateMethod =
                                        proxiedClass.createGetTemplateNameMethod(lithoMethodImplementation)
                                    proxiedClass.addMethod(getTemplateMethod)

                                    val lithoInstructions = lithoMethodImplementation.instructions
                                    val thisType = proxiedClass.type
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
                                    """.toInstructions(lithoMethod)
                                    val block2 = """
                                        move-object/from16 v1, p3
                                        iget-object v2, v1, $templateNameParameterType->b:Ljava/nio/ByteBuffer;
                                        invoke-static {v0, v2}, Lapp/revanced/integrations/patches/GeneralBytecodeAdsPatch;->containsAd(Ljava/lang/String;Ljava/nio/ByteBuffer;)Z
                                        move-result v1
                                    """.toInstructions(lithoMethod)
                                    val block3 = """
                                        move-object/from16 v2, p1
                                        invoke-static {v2}, $descriptor1
                                        move-result-object v0
                                        iget-object v0, v0, $descriptor2
                                        return-object v0
                                    """.toInstructions(lithoMethod)

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
        """.toInstructions()
        val block2 = """
              invoke-static {p0}, ${descriptors[2]}
              move-result-object p0
              invoke-virtual {p0}, ${descriptors[3]}
              move-result-object v0
              invoke-static {v0}, ${this.type}->getIsEmpty(Ljava/lang/String;)Z
              move-result v0
        """.toInstructions()
        val block3 = """
              invoke-virtual {p0}, ${descriptors[3]}
              move-result-object p0
              return-object p0
        """.toInstructions()

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
