package app.revanced.patches.instagram.patches.interaction.links.tracking

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.instagram.patches.interaction.links.tracking.fingerprints.*
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from links before they are shared.",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    requiresIntegrations = true,
)
@Suppress("unused")
object SanitizeSharingLinksPatch : BytecodePatch(
    setOf(
        StoryShareUrlFingerprint,
        LiveShareUrlFingerprint,
        PostShareClassFinderFingerprint,
        ProfileShareUrlFingerprint,
        HighlightsShareUrlFingerprint,
    ),
) {
    private const val INVOKE_INTEGRATIONS_METHOD_INSTRUCTION =
        "Lapp/revanced/integrations/instagram/links/ShareLink;->sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;"

    override fun execute(context: BytecodeContext) {
        fun sanitizeUrl(method: MutableMethod) = method.apply {
            val index = getInstructions().first { it.opcode == Opcode.IPUT_OBJECT }.location.index - 2
            // Register where the link is present.
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                    invoke-static{ v$register }, $INVOKE_INTEGRATIONS_METHOD_INSTRUCTION
                    move-result-object v$register
                """,
            )
        }

        fun sanitizeUrl(fingerprint: MethodFingerprint) = sanitizeUrl(fingerprint.resultOrThrow().mutableMethod)

        // Sanitize share link of stories.
        sanitizeUrl(StoryShareUrlFingerprint)
        // Sanitize share link of live.
        sanitizeUrl(LiveShareUrlFingerprint)
        // Sanitize share link of profile.
        sanitizeUrl(ProfileShareUrlFingerprint)
        // Sanitize share link of highlights.
        sanitizeUrl(HighlightsShareUrlFingerprint)
        // Sanitize share link of posts & reels.
        PostShareClassFinderFingerprint.resultOrThrow().mutableMethod.let { method ->
            val classIndex = method.getInstructions().last { it.opcode == Opcode.CONST_CLASS }.location.index
            val className = method.getInstruction(classIndex).getReference<TypeReference>()!!.type
            val parseJsonMethod = context.findClass(className)!!.mutableClass.methods.first {
                it.name == "parseFromJson"
            }

            sanitizeUrl(parseJsonMethod)
        }
    }
}
