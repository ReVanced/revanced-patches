package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
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
    requiresIntegrations = true,
)
@Suppress("unused")
object SanitizeSharingLinksPatch : BytecodePatch(
    setOf(StoryShareUrlFingerprint, LiveShareUrlFingerprint, PostShareClassFinderFingerprint),
) {
    private const val INVOKE_INTEGRATIONS_METHOD_INSTRUCTION = "Lapp/revanced/integrations/instagram/links/ShareLink;->sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;"

    override fun execute(context: BytecodeContext) {
        fun addCode(method: MutableMethod) {
            val index = method.getInstructions().first { it.opcode == Opcode.IPUT_OBJECT }.location.index - 2
            // Register where the link is present.
            val register = method.getInstruction<OneRegisterInstruction>(index).registerA

            method.addInstructions(
                index + 1,
                """
                    invoke-static{v$register}, $INVOKE_INTEGRATIONS_METHOD_INSTRUCTION
                    move-result-object v$register
                """,
            )
        }

        fun addMethod(fingerprint: MethodFingerprint) {
            addCode(fingerprint.resultOrThrow().mutableMethod)
        }

        // Sanitize share link of stories.
        addMethod(StoryShareUrlFingerprint)
        // Sanitize share link of live.
        addMethod(LiveShareUrlFingerprint)

        // Finding post share url class.
        PostShareClassFinderFingerprint.resultOrThrow().let { it ->
            it.mutableMethod.apply {
                val classIndex = getInstructions().last { it.opcode == Opcode.CONST_CLASS }.location.index
                val className = (getInstruction<BuilderInstruction21c>(classIndex).reference as DexBackedTypeReference).type
                val parseJsonMethod = context.findClass(className)!!.mutableClass.methods.first { it.name == "parseFromJson" }
                // Sanitize share link of posts & reels.
                addCode(parseJsonMethod)
            }
        }
    }
}
