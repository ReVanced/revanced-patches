package app.revanced.patches.piccomafr.misc

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Spoof Android device ID` by creatingBytecodePatch(
    description = "Spoofs the Android device ID used by the app for account authentication." +
        "This can be used to copy the account to another device.",
    use = false,
) {
    compatibleWith(
        "com.piccomaeurope.fr"(
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
        ),
    )

    val androidDeviceId by stringOption(
        name = "Android device ID",
        default = "0011223344556677",
        description = "The Android device ID to spoof to.",
        required = true,
    ) { it!!.matches("[A-Fa-f0-9]{16}".toRegex()) }

    apply {
        getAndroidIdMethod.returnEarly(androidDeviceId!!)
    }
}
