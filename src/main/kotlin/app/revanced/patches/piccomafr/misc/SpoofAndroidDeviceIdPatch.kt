package app.revanced.patches.piccomafr.misc

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.piccomafr.misc.fingerprints.GetAndroidIDFingerprint
import app.revanced.util.exception

@Patch(
    name = "Spoof Android device ID",
    description = "Spoofs the Android device ID used by the app for account authentication. " +
        "This can be used to copy the account to another device.",
    compatiblePackages = [
        CompatiblePackage(
            "com.piccomaeurope.fr",
            [
                "6.4.0",
                "6.4.1",
                "6.4.2",
                "6.4.3",
                "6.4.4",
                "6.4.5",
                "6.5.0",
                "6.5.1",
                "6.5.2",
                "6.5.3",
                "6.5.4",
                "6.6.0",
                "6.6.1",
                "6.6.2",
            ],
        ),
    ],
    use = false,
)
@Suppress("unused")
object SpoofAndroidDeviceIdPatch : BytecodePatch(
    setOf(GetAndroidIDFingerprint),
) {
    private var androidDeviceId =
        stringPatchOption(
            key = "android-device-id",
            default = "0011223344556677",
            title = "Android device ID",
            description = "The Android device ID to spoof to.",
            required = true,
        ) { it!!.matches("[A-Fa-f0-9]{16}".toRegex()) }

    override fun execute(context: BytecodeContext) = GetAndroidIDFingerprint.result?.mutableMethod?.addInstructions(
        0,
        """
            const-string v0, "$androidDeviceId"
            return-object v0
        """,
    ) ?: throw GetAndroidIDFingerprint.exception
}
