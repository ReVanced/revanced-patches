package app.revanced.patches.instagram.patches.links.openInExternalBrowser

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.instagram.misc.integrations.IntegrationsPatch
import app.revanced.patches.instagram.patches.links.openInExternalBrowser.fingerprints.OpenLinksInExternalBrowserFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Open links in external browser",
    dependencies = [IntegrationsPatch::class],
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object OpenLinksInExternalBrowser : BytecodePatch(
    setOf(OpenLinksInExternalBrowserFingerprint),
) {
    private const val INVOKE_INTEGRATIONS_METHOD_INSTRUCTIONS = """
        invoke-static{v0}, Lapp/revanced/integrations/instagram/links/ExternalBrowser;->openInExternalBrowser(Ljava/lang/String;)Z
        move-result v0
    """

    override fun execute(context: BytecodeContext) {
        OpenLinksInExternalBrowserFingerprint.resultOrThrow().let { it ->
            it.mutableClass.apply {
                val className = it.classDef.type

                // Get the method that returns the url.
                val getUrlMethod = methods.first { it.returnType == "Ljava/lang/String;" && it.parameters.size == 0 }.name
                val getUrlMethodInstruction = """
                    invoke-virtual {p0}, $className->$getUrlMethod()Ljava/lang/String;
                    move-result-object v0
                """.trimIndent()

                // Hooking the call method.
                // Finding the method where browser call happens.
                val browserCallMethod = methods.last { it.returnType == "V" && it.parameters.size == 0 }

                // Call the openInExternalBrowser method.
                // If it returns true, return void.
                // If it returns false, proceed as usual.
                browserCallMethod.addInstructionsWithLabels(
                    0,
                    """
                        $getUrlMethodInstruction
                        $INVOKE_INTEGRATIONS_METHOD_INSTRUCTIONS
                        if-eqz v0, :revanced
                        return-void
                    """,
                    ExternalLabel("revanced", browserCallMethod.getInstructions().first { it.opcode == Opcode.CONST_4 }),
                )
            }
        }
    }
}
