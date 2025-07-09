package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
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
        title = " Login request listener port",
        description = "The port to use for the listener that intercepts and handles login requests. " +
                "Port must be between 0 and 65535.",
        required = true,
        validator = {
            it!!
            !(it < 0 || it > 65535)
        }
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

        listOf(
            "Lcom/spotify/connectivity/ApplicationScopeConfiguration;",
            "Lcom/spotify/authentication/login5/Login5Configuration;",
            "Lcom/spotify/connectivity/AuthenticatedScopeConfiguration;",
            "Lcom/spotify/core/corefullimpl/FullAuthenticatedScopeConfiguration;",
        ).forEach {
            setClientIdFingerprint(it).method.addInstruction(
                0,
                "const-string p1, \"58bd3c95768941ea9eb4350aaa033eb3\""
            )
        }

        setUserAgentFingerprint.method.addInstruction(
            0,
            "const-string p1, \"Spotify/9.0.58 iOS/19 (iPad8,11)\""
        )

        // endregion

        // region Disable verdicts.

        // Early return to block sending bad verdicts to the API.
        runIntegrityVerificationFingerprint.method.returnEarly()

        // endregion
    }
}
