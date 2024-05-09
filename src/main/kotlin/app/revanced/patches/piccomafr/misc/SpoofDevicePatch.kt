package app.revanced.patches.piccomafr.misc

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.piccomafr.misc.fingerprints.GetAndroidIDFingerprint
import app.revanced.util.exception


private val HEXADECIMAL_PATTERN: Regex = Regex("[A-Fa-f0-9]{16}")

@Patch(
     name = "Spoof Android Device ID",
    description = "The Authentication of accounts in Piccoma only rely on the Android ID associated " +
            "to the account. So you could edit this to copy one of your existing devices.",
     compatiblePackages = [CompatiblePackage(
         "com.piccomaeurope.fr",
         [
             "6.4.0", "6.4.1", "6.4.2", "6.4.3", "6.4.4", "6.4.5",
             "6.5.0", "6.5.1", "6.5.2", "6.5.3", "6.5.4",
             "6.6.0", "6.6.1", "6.6.2"
         ],
     )],
    use = false
 )
 @Suppress("unused")
 object SpoofDevicePatch : BytecodePatch(
     setOf(GetAndroidIDFingerprint),
 ) {

     private var spoofedAndroidID =
         stringPatchOption(
             key = "spoofedAndroidID",
             default = "0011223344556677",
             title = "Spoofed Android ID",
             // description = "Android ID to use",
             required = true
         ) {
             it!!.matches(HEXADECIMAL_PATTERN)
         }

     override fun execute(context: BytecodeContext) {
         val spoofedAndroidID = spoofedAndroidID

         GetAndroidIDFingerprint.result?.mutableMethod?.addInstructions(
         0,
         """
            const-string v0, "$spoofedAndroidID"
            return-object v0
         """
        )?: throw GetAndroidIDFingerprint.exception
     }
}
