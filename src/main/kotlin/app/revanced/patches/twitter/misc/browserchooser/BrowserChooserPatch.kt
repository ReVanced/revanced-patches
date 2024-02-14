package app.revanced.patches.twitter.misc.browserchooser

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.browserchooser.fingerprints.OpenLinkFingerprint
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.Opcode
import java.lang.IllegalStateException

@Patch(
    name = "Open browser chooser on opening links",
    description = "Instead of open the link directly in one of the installed browsers",
    compatiblePackages = [CompatiblePackage("com.twitter.android")]
)
@Suppress("unused")
object BrowserChooserPatch : BytecodePatch(
    setOf(OpenLinkFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        val result = OpenLinkFingerprint.result
            ?: throw IllegalStateException("Fingerprint not found")

        val inject = """
            invoke-static {p0, p1}, Lapp/revanced/integrations/twitter/patches/hook/patch/browserchooser/BrowserChooserHook->open(Landroid/content/Context;Landroid/content/Intent;)V
            return-void
        """.trimIndent()

        result.mutableMethod.addInstructions(0, inject)

    }
}
