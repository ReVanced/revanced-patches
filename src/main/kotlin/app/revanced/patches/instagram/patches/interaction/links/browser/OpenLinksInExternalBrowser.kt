package app.revanced.patches.instagram.patches.interaction.links.browser

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.instagram.misc.integrations.IntegrationsPatch
import app.revanced.patches.instagram.patches.interaction.links.browser.fingerprints.OpenLinksInExternalBrowserFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Open links in external browser",
    dependencies = [IntegrationsPatch::class],
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object OpenLinksInExternalBrowser : BytecodePatch(
    setOf(OpenLinksInExternalBrowserFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        OpenLinksInExternalBrowserFingerprint.resultOrThrow().let { it ->
            // Get the method that returns the url.
            val getUrlMethod = it.mutableClass.methods.first {
                it.returnType == "Ljava/lang/String;" && it.parameters.size == 0
            }.name

            // Patch the method that opens the link in the internal browser.
            it.mutableClass.methods.last { it.returnType == "V" && it.parameters.size == 0 }.apply {
                val continueInstruction = getInstructions().first()

                // Call the openInExternalBrowser method.
                // If it returns true, return void.
                // If it returns false, proceed as usual.
                addInstructionsWithLabels(
                    0,
                    """
                        invoke-virtual { p0 }, ${it.classDef}->$getUrlMethod()Ljava/lang/String;
                        move-result-object v0

                        invoke-static { v0 }, Lapp/revanced/integrations/instagram/links/ExternalBrowser;->openInExternalBrowser(Ljava/lang/String;)Z
                        move-result v0

                        if-eqz v0, :not_opened
                        return-void
                    """,
                    ExternalLabel("not_opened", continueInstruction),
                )
            }
        }
    }
}
