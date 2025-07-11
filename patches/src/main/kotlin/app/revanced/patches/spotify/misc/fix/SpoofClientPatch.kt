package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.shared.misc.hex.HexPatchBuilder
import app.revanced.patches.shared.misc.hex.hexPatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.returnEarly

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/fix/SpoofClientPatch;"

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to fix various functions of the app.",
) {
    val requestListenerPort by intOption(
        key = "requestListenerPort",
        default = 4345,
        title = "Request listener port",
        description = "The port to use for the listener that intercepts and handles spoofed requests. " +
                "Port must be between 0 and 65535. " +
                "Do not change this option, if you do not know what you are doing.",
        validator = {
            it!!
            !(it < 0 || it > 65535)
        }
    )

    val clientVersion by stringOption(
        key = "clientVersion",
        default = "iphone-9.0.58.558.g200011c",
        title = "Client version",
        description = "The client version used for spoofing the client token. " +
                "Do not change this option, if you do not know what you are doing."
    )

    val hardwareMachine by stringOption(
        key = "hardwareMachine",
        default = "iPhone16,1",
        title = "Hardware machine",
        description = "The hardware machine used for spoofing the client token. " +
                "Do not change this option, if you do not know what you are doing."
    )

    val systemVersion by stringOption(
        key = "systemVersion",
        default = "17.7.2",
        title = "System version",
        description = "The system version used for spoofing the client token. " +
                "Do not change this option, if you do not know what you are doing."
    )

    dependsOn(
        sharedExtensionPatch,
        hexPatch(ignoreMissingTargetFiles = true, block = fun HexPatchBuilder.() {
            listOf(
                "arm64-v8a",
                "armeabi-v7a",
                "x86",
                "x86_64"
            ).forEach { architecture ->
                "https://clienttoken.spotify.com/v1/clienttoken" to
                        "http://127.0.0.1:$requestListenerPort/v1/clienttoken" inFile
                        "lib/$architecture/liborbit-jni-spotify.so"
            }
        })
    )

    compatibleWith("com.spotify.music")

    execute {
        // region Spoof client.

        loadOrbitLibraryFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, $requestListenerPort
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->launchListener(I)V
            """
        )

        mapOf(
            "getClientVersion" to clientVersion!!,
            "getSystemVersion" to systemVersion!!,
            "getHardwareMachine" to hardwareMachine!!
        ).forEach { (methodName, value) ->
            extensionFixConstantsFingerprint.classDef.methods.single { it.name == methodName }.returnEarly(value)
        }

        // endregion

        // region Disable verdicts.

        // Early return to block sending bad verdicts to the API.
        runIntegrityVerificationFingerprint.method.returnEarly()

        // endregion
    }
}
