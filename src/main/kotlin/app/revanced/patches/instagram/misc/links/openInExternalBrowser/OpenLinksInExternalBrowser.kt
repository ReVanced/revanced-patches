package app.revanced.patches.instagram.misc.links.openInExternalBrowser


import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.instagram.misc.integrations.IntegrationsPatch
import app.revanced.patches.instagram.misc.links.openInExternalBrowser.fingerprints.OpenLinksInExternalBrowserFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.ImmutableField

@Patch(
    name = "Open links in external browser",
    dependencies = [IntegrationsPatch::class],
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false,
    requiresIntegrations = true
)
object OpenLinksInExternalBrowser:BytecodePatch(
    setOf(OpenLinksInExternalBrowserFingerprint)
) {
    private const val FIELD_NAME = "URL_FIELD"
    private const val FUNC_CLASS = "Lapp/revanced/integrations/instagram/links/ExternalBrowser;->"
    private const val FUNC_CALL = """
        invoke-static{v0}, $FUNC_CLASS openInExternalBrowser(Ljava/lang/String;)Z
        move-result v0
    """

    override fun execute(context: BytecodeContext) {
        val result = OpenLinksInExternalBrowserFingerprint.result ?: throw OpenLinksInExternalBrowserFingerprint.exception

        val cls = result.mutableClass

        //create a field of type String to hold url
        cls.fields.add(ImmutableField(
            result.classDef.type,
            FIELD_NAME,
            "Ljava/lang/String;",
            AccessFlags.PUBLIC or AccessFlags.PUBLIC,
            null,
            null,
            null
        ).toMutable())


        val FIELD_ACCESS = "${result.classDef.type}->$FIELD_NAME:Ljava/lang/String;"
        
        ////set url
        //the last parameter in constructor is the url
        val methods = cls.methods
        val methodConst = methods.first { it.name == "<init>" }
        val if_eqz = methodConst.getInstructions().first { it.opcode == Opcode.IF_EQZ }
        val if_eqz_loc = if_eqz.location.index

        //registry of the url string
        val reg = methodConst.parameters.size

        methodConst.addInstruction(if_eqz_loc+1,"""
           iput-object p$reg, p0, $FIELD_ACCESS
        """.trimIndent())

        ////Hooking the call method
        //finding the method
        val method = methods.last { it.returnType == "V" && it.parameters.size == 0 }
        val const_4 = method.getInstructions().first{it.opcode == Opcode.CONST_4 }


        //call the openInExternalBrowser method
        // if it returns true, return void
        // if it returns false, proceed as usual
        method.addInstructionsWithLabels(0,"""
            iget-object v0, p0, $FIELD_ACCESS
            $FUNC_CALL
            if-eqz v0, :revanced
            return-void
        """.trimIndent(),ExternalLabel("revanced",const_4))

    }
}