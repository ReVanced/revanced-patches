package app.revanced.patches.idaustria.detection.signature.patch

import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.idaustria.detection.signature.fingerprints.SpoofSignatureFingerprint

@Patch
@Name("spoof-signature")
@Description("Spoofs the signature of the app.")
@Compatibility([Package("at.gv.oe.app", arrayOf("2.5.2"))])
@Version("0.0.1")
class SpoofSignaturePatch : BytecodePatch(
    listOf(SpoofSignatureFingerprint)
) {
    companion object {
        const val EXPECTED_SIGNATURE = "OpenSSLRSAPublicKey{modulus=ac3e6fd6050aa7e0d6010ae58190404cd89a56935b44f6fee" +
                "067c149768320026e10b24799a1339e414605e448e3f264444a327b9ae292be2b62ad567dd1800dbed4a88f718a33dc6db6b" +
                "f5178aa41aa0efff8a3409f5ca95dbfccd92c7b4298966df806ea7a0204a00f0e745f6d9f13bdf24f3df715d7b62c1600906" +
                "15de1c8a956b9286764985a3b3c060963c435fb9481a5543aaf0671fc2dba6c5c2b17d1ef1d85137f14dc9bbdf3490288087" +
                "324cd48341cce64fabf6a9b55d1a7bf23b2fcdff451fd85bf0c7feb0a5e884d7c5c078e413149566a12a686e6efa70ae5161" +
                "a0201307692834cda336c55157fef125e67c01c1359886f94742105596b42a790404bbcda5dad6a65f115aaff5e45ef3c28b" +
                "2316ff6cef07aa49a45aa58c07bf258051b13ef449ccb37a3679afd5cfb9132f70bb9d931a937897544f90c3bcc80ed012e9" +
                "f6ba020b8cdc23f8c29ac092b88f0e370ff9434e4f0f359e614ae0868dc526fa41e4b7596533e8d10279b66e923ecd9f0b20" +
                "0def55be2c1f6f9c72c92fb45d7e0a9ac571cb38f0a9a37bb33ea06f223fde8c7a92e8c47769e386f9799776e8f110c21df2" +
                "77ef1be61b2c01ebdabddcbf53cc4b6fd9a3c445606ee77b3758162c80ad8f8137b3c6864e92db904807dcb2be9d7717dd21" +
                "bf42c121d620ddfb7914f7a95c713d9e1c1b7bdb4a03d618e40cf7e9e235c0b5687e03b7ab3,publicExponent=10001}"
    }

    override fun execute(context: BytecodeContext): PatchResult {
        SpoofSignatureFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
                const-string v0, "$EXPECTED_SIGNATURE"
                return-object v0 
            """
        )
        return PatchResultSuccess()
    }
}
