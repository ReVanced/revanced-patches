package app.revanced.patches.instagram.misc.links.sanitizeSharingLinks


import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.instagram.misc.links.sanitizeSharingLinks.fingerprints.LiveShareUrlFingerprint
import app.revanced.patches.instagram.misc.links.sanitizeSharingLinks.fingerprints.PostShareClassFinderFingerprint
import app.revanced.patches.instagram.misc.links.sanitizeSharingLinks.fingerprints.StoryShareUrlFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.dexbacked.reference.DexBackedTypeReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from links before they are shared.",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false,
    requiresIntegrations = true
)
object SanitizeSharingLinksPatch:BytecodePatch(
    setOf(StoryShareUrlFingerprint, LiveShareUrlFingerprint,PostShareClassFinderFingerprint)
) {
    private const val FUNC_CALL = "Lapp/revanced/integrations/instagram/links/ShareLink;->sanitizeUrl(Ljava/lang/String;)Ljava/lang/String;"

    override fun execute(context: BytecodeContext) {
        //stories
        addFunction(StoryShareUrlFingerprint)
        //live
        addFunction(LiveShareUrlFingerprint)

        //finding post share url class
        val result = PostShareClassFinderFingerprint.result ?: throw PostShareClassFinderFingerprint.exception
        val method = result.mutableMethod
        val instructions = method.getInstructions()
        val loc = instructions.last { it.opcode == Opcode.CONST_CLASS }.location.index

        val className = (method.getInstruction<BuilderInstruction21c>(loc).reference as DexBackedTypeReference).type
        val parseJsonMethod = context.findClass(className)!!.mutableClass.methods.first { it.name == "parseFromJson" }
        //posts & reels
        addCode(parseJsonMethod)

    }

    private fun addFunction(fingerprint: MethodFingerprint) {
        val result = fingerprint.result?:throw fingerprint.exception
        val method = result.mutableMethod
        addCode(method)

    }

    private fun addCode(method: MutableMethod){
        val loc = method.getInstructions().first { it.opcode == Opcode.IPUT_OBJECT }.location.index - 2
        //link register
        val reg = method.getInstruction<OneRegisterInstruction>(loc).registerA

        method.addInstructions(loc+1,"""
            invoke-static{v$reg}, $FUNC_CALL
            move-result-object v$reg
        """.trimIndent())
    }
}