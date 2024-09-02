package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints.LiveShareUrlFingerprint
import app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints.PostShareClassFinderFingerprint
import app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints.StoryShareUrlFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.dexbacked.reference.DexBackedTypeReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from links before they are shared.",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false,
    requiresIntegrations = true,
)
@Suppress("unused")
object SanitizeSharingLinksPatch : BytecodePatch(
    setOf(StoryShareUrlFingerprint, LiveShareUrlFingerprint, PostShareClassFinderFingerprint),
) {
    private const val FUNC_CALL = "Lapp/revanced/integrations/instagram/links/ShareLink;->sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;"

    override fun execute(context: BytecodeContext) {
        fun addFunction(method: MutableMethod) {
            val index = method.getInstructions().first { it.opcode == Opcode.IPUT_OBJECT }.location.index - 2
            // link register.
            val register = method.getInstruction<OneRegisterInstruction>(index).registerA

            method.addInstructions(
                index + 1,
                """
            invoke-static{v$register}, $FUNC_CALL
            move-result-object v$register
                """.trimIndent(),
            )
        }

        // sanitize share link of stories.
        addFunction(StoryShareUrlFingerprint.resultOrThrow().mutableMethod)
        // sanitize share link of live.
        addFunction(LiveShareUrlFingerprint.resultOrThrow().mutableMethod)

        // finding post share url class.
        PostShareClassFinderFingerprint.resultOrThrow().let { it ->
            it.mutableMethod.apply {
                val classIndex = getInstructions().last { it.opcode == Opcode.CONST_CLASS }.location.index
                val className = (getInstruction<BuilderInstruction21c>(classIndex).reference as DexBackedTypeReference).type
                val parseJsonMethod = context.findClass(className)!!.mutableClass.methods.first { it.name == "parseFromJson" }
                // sanitize share link of posts & reels.
                addFunction(parseJsonMethod)
            }
        }
    }
}
