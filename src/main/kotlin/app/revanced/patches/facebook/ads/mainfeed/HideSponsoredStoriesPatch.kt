package app.revanced.patches.facebook.ads.mainfeed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.facebook.ads.mainfeed.fingerprints.BaseModelMapperFingerprint
import app.revanced.patches.facebook.ads.mainfeed.fingerprints.GetSponsoredDataModelTemplateFingerprint
import app.revanced.patches.facebook.ads.mainfeed.fingerprints.GetStoryVisibilityFingerprint
import app.revanced.util.exception
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

@Patch(
    name = "Hide 'Sponsored Stories'",
    compatiblePackages = [CompatiblePackage("com.facebook.katana")],
)
@Suppress("unused")
object HideSponsoredStoriesPatch : BytecodePatch(
    setOf(GetStoryVisibilityFingerprint, GetSponsoredDataModelTemplateFingerprint, BaseModelMapperFingerprint),
) {
    private const val GRAPHQL_STORY_TYPE = "Lcom/facebook/graphql/model/GraphQLStory;"

    override fun execute(context: BytecodeContext) {
        GetStoryVisibilityFingerprint.result?.apply {
            val sponsoredDataModelTemplateMethod = GetSponsoredDataModelTemplateFingerprint.resultOrThrow().method
            val baseModelMapperMethod = BaseModelMapperFingerprint.resultOrThrow().method
            val baseModelWithTreeType = baseModelMapperMethod.returnType

            // The "SponsoredDataModelTemplate" methods has the ids in its body to extract sponsored data
            // from GraphQL models, but targets the wrong derived type of "BaseModelWithTree". Since those ids
            // could change in future version, we need to extract them and call the base implementation directly.
            val getSponsoredDataHelperMethod = ImmutableMethod(
                classDef.type,
                "getSponsoredData",
                listOf(ImmutableMethodParameter(GRAPHQL_STORY_TYPE, null, null)),
                baseModelWithTreeType,
                AccessFlags.PRIVATE or AccessFlags.STATIC,
                null,
                null,
                MutableMethodImplementation(4),
            ).toMutable().apply {
                // Extract the ids of the original method. These ids seem to correspond to model types for
                // GraphQL data structure. They are then fed to a method of BaseModelWithTree that populate
                // and cast the requested GraphQL subtype. The Ids are found in the two first "CONST" instructions.
                val constInstructions = sponsoredDataModelTemplateMethod.implementation!!.instructions
                    .asSequence()
                    .filterIsInstance<Instruction31i>()
                    .take(2)
                    .toList()

                val storyTypeId = constInstructions[0].narrowLiteral
                val sponsoredDataTypeId = constInstructions[1].narrowLiteral

                addInstructions(
                    """ 
                        const-class v2, $baseModelWithTreeType
                        const v1, $storyTypeId
                        const v0, $sponsoredDataTypeId
                        invoke-virtual {p0, v2, v1, v0}, $baseModelMapperMethod
                        move-result-object v0
                        check-cast v0, $baseModelWithTreeType
                        return-object v0
                    """,
                )
            }

            mutableClass.methods.add(getSponsoredDataHelperMethod)

            // Check if the parameter type is GraphQLStory and if sponsoredDataModelGetter returns a non-null value.
            // If so, hide the story by setting the visibility to StoryVisibility.GONE.
            mutableMethod.addInstructionsWithLabels(
                scanResult.patternScanResult!!.startIndex,
                """
                    instance-of v0, p0, $GRAPHQL_STORY_TYPE
                    if-eqz v0, :resume_normal
                    invoke-static {p0}, $getSponsoredDataHelperMethod
                    move-result-object v0 
                    if-eqz v0, :resume_normal
                    const-string v0, "GONE"
                    return-object v0
                    :resume_normal
                    nop
                """,
            )
        } ?: throw GetStoryVisibilityFingerprint.exception
    }
}
