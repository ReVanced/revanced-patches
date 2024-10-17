package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.twitter.misc.links.fingerprints.LinkBuilderFingerprint
import app.revanced.patches.twitter.misc.links.fingerprints.LinkResourceGetterFingerprint
import app.revanced.patches.twitter.misc.links.fingerprints.LinkSharingDomainFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Change link sharing domain",
    description = "Replaces the domain name of Twitter links when sharing them.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
)
@Suppress("unused")
object ChangeLinkSharingDomainPatch : BytecodePatch(
    setOf(
        LinkBuilderFingerprint,
        LinkResourceGetterFingerprint,
        LinkSharingDomainFingerprint,
    ),
) {
    private var domainName by stringPatchOption(
        key = "domainName",
        default = "fxtwitter.com",
        title = "Domain name",
        description = "The domain name to use when sharing links.",
        required = true,
    )

    // This method is used to build the link that is shared when the "Share via..." button is pressed.
    private const val FORMAT_METHOD_RESOURCE_REFERENCE =
        "Lapp/revanced/integrations/twitter/patches/links/ChangeLinkSharingDomainPatch;->" +
            "formatResourceLink([Ljava/lang/Object;)Ljava/lang/String;"

    // This method is used to build the link that is shared when the "Copy link" button is pressed.
    private const val FORMAT_METHOD_REFERENCE =
        "Lapp/revanced/integrations/twitter/patches/links/ChangeLinkSharingDomainPatch;->" +
            "formatLink(JLjava/lang/String;)Ljava/lang/String;"

    override fun execute(context: BytecodeContext) {
        LinkSharingDomainFingerprint.result?.let {
            val replacementIndex = it.scanResult.stringsScanResult!!.matches.first().index
            val domainRegister = it.mutableMethod.getInstruction<OneRegisterInstruction>(replacementIndex).registerA
            it.mutableMethod.replaceInstruction(
                replacementIndex,
                "const-string v$domainRegister, \"https://$domainName\"",
            )
        } ?: throw LinkSharingDomainFingerprint.exception

        // Replace the domain name when copying a link with "Copy link" button.
        LinkBuilderFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0,
                    """
                        invoke-static { p0, p1, p2 }, $FORMAT_METHOD_REFERENCE
                        move-result-object p0
                        return-object p0
                    """,
                )
            }
        } ?: throw LinkBuilderFingerprint.exception

        // Used in the Share via... dialog.
        LinkResourceGetterFingerprint.result?.mutableMethod?.apply {
            val constWithParameterName = indexOfFirstInstructionOrThrow {
                getReference<StringReference>()?.string?.contains("id.toString()") == true
            }

            // Format the link with the new domain name register (2 instructions above the const-string).
            val formatLinkCallIndex = constWithParameterName - 2
            val formatLinkCall = getInstruction<Instruction35c>(formatLinkCallIndex)

            // Replace the original method call with the new method call.
            replaceInstruction(
                formatLinkCallIndex,
                "invoke-static { v${formatLinkCall.registerE} }, $FORMAT_METHOD_RESOURCE_REFERENCE",
            )
        } ?: throw LinkResourceGetterFingerprint.exception
    }
}
