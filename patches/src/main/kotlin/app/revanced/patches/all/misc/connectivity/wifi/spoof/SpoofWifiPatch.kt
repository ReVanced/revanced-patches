package app.revanced.patches.all.misc.connectivity.wifi.spoof

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch

private const val EXTENSION_CLASS_DESCRIPTOR_PREFIX =
    "Lapp/revanced/extension/all/misc/connectivity/wifi/spoof/SpoofWifiPatch"

private const val EXTENSION_CLASS_DESCRIPTOR = "$EXTENSION_CLASS_DESCRIPTOR_PREFIX;"

@Suppress("unused")
val spoofWifiPatch = bytecodePatch(
    name = "Spoof Wi-Fi connection",
    description = "Spoofs an existing Wi-Fi connection.",
    use = false,
) {
    extendWith("extensions/all/misc/connectivity/wifi/spoof/spoof-wifi.rve")

    dependsOn(
        transformInstructionsPatch(
            filterMap = { classDef, _, instruction, instructionIndex ->
                filterMapInstruction35c<MethodCall>(
                    EXTENSION_CLASS_DESCRIPTOR_PREFIX,
                    classDef,
                    instruction,
                    instructionIndex,
                )
            },
            transform = { method, entry ->
                val (methodType, instruction, instructionIndex) = entry
                methodType.replaceInvokeVirtualWithExtension(
                    EXTENSION_CLASS_DESCRIPTOR,
                    method,
                    instruction,
                    instructionIndex,
                )
            },
        ),
    )
}

// Information about method calls we want to replace
@Suppress("unused")
private enum class MethodCall(
    override val definedClassName: String,
    override val methodName: String,
    override val methodParams: Array<String>,
    override val returnType: String,
) : IMethodCall {
    GetSystemService1(
        "Landroid/content/Context;",
        "getSystemService",
        arrayOf("Ljava/lang/String;"),
        "Ljava/lang/Object;",
    ),
    GetSystemService2(
        "Landroid/content/Context;",
        "getSystemService",
        arrayOf("Ljava/lang/Class;"),
        "Ljava/lang/Object;",
    ),
    GetActiveNetworkInfo(
        "Landroid/net/ConnectivityManager;",
        "getActiveNetworkInfo",
        arrayOf(),
        "Landroid/net/NetworkInfo;",
    ),
    IsConnected(
        "Landroid/net/NetworkInfo;",
        "isConnected",
        arrayOf(),
        "Z",
    ),
    IsConnectedOrConnecting(
        "Landroid/net/NetworkInfo;",
        "isConnectedOrConnecting",
        arrayOf(),
        "Z",
    ),
    IsAvailable(
        "Landroid/net/NetworkInfo;",
        "isAvailable",
        arrayOf(),
        "Z",
    ),
    GetState(
        "Landroid/net/NetworkInfo;",
        "getState",
        arrayOf(),
        "Landroid/net/NetworkInfo\$State;",
    ),
    GetDetailedState(
        "Landroid/net/NetworkInfo;",
        "getDetailedState",
        arrayOf(),
        "Landroid/net/NetworkInfo\$DetailedState;",
    ),
    IsActiveNetworkMetered(
        "Landroid/net/ConnectivityManager;",
        "isActiveNetworkMetered",
        arrayOf(),
        "Z",
    ),
    GetActiveNetwork(
        "Landroid/net/ConnectivityManager;",
        "getActiveNetwork",
        arrayOf(),
        "Landroid/net/Network;",
    ),
    GetNetworkInfo(
        "Landroid/net/ConnectivityManager;",
        "getNetworkInfo",
        arrayOf("Landroid/net/Network;"),
        "Landroid/net/NetworkInfo;",
    ),
    HasTransport(
        "Landroid/net/NetworkCapabilities;",
        "hasTransport",
        arrayOf("I"),
        "Z",
    ),
    HasCapability(
        "Landroid/net/NetworkCapabilities;",
        "hasCapability",
        arrayOf("I"),
        "Z",
    ),
    RegisterBestMatchingNetworkCallback(
        "Landroid/net/ConnectivityManager;",
        "registerBestMatchingNetworkCallback",
        arrayOf(
            "Landroid/net/NetworkRequest;",
            "Landroid/net/ConnectivityManager\$NetworkCallback;",
            "Landroid/os/Handler;",
        ),
        "V",
    ),
    RegisterDefaultNetworkCallback1(
        "Landroid/net/ConnectivityManager;",
        "registerDefaultNetworkCallback",
        arrayOf("Landroid/net/ConnectivityManager\$NetworkCallback;"),
        "V",
    ),
    RegisterDefaultNetworkCallback2(
        "Landroid/net/ConnectivityManager;",
        "registerDefaultNetworkCallback",
        arrayOf("Landroid/net/ConnectivityManager\$NetworkCallback;", "Landroid/os/Handler;"),
        "V",
    ),
    RegisterNetworkCallback1(
        "Landroid/net/ConnectivityManager;",
        "registerNetworkCallback",
        arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;"),
        "V",
    ),
    RegisterNetworkCallback2(
        "Landroid/net/ConnectivityManager;",
        "registerNetworkCallback",
        arrayOf("Landroid/net/NetworkRequest;", "Landroid/app/PendingIntent;"),
        "V",
    ),
    RegisterNetworkCallback3(
        "Landroid/net/ConnectivityManager;",
        "registerNetworkCallback",
        arrayOf(
            "Landroid/net/NetworkRequest;",
            "Landroid/net/ConnectivityManager\$NetworkCallback;",
            "Landroid/os/Handler;",
        ),
        "V",
    ),
    RequestNetwork1(
        "Landroid/net/ConnectivityManager;",
        "requestNetwork",
        arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;"),
        "V",
    ),
    RequestNetwork2(
        "Landroid/net/ConnectivityManager;",
        "requestNetwork",
        arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;", "I"),
        "V",
    ),
    RequestNetwork3(
        "Landroid/net/ConnectivityManager;",
        "requestNetwork",
        arrayOf(
            "Landroid/net/NetworkRequest;",
            "Landroid/net/ConnectivityManager\$NetworkCallback;",
            "Landroid/os/Handler;",
        ),
        "V",
    ),
    RequestNetwork4(
        "Landroid/net/ConnectivityManager;",
        "requestNetwork",
        arrayOf("Landroid/net/NetworkRequest;", "Landroid/app/PendingIntent;"),
        "V",
    ),
    RequestNetwork5(
        "Landroid/net/ConnectivityManager;",
        "requestNetwork",
        arrayOf(
            "Landroid/net/NetworkRequest;",
            "Landroid/net/ConnectivityManager\$NetworkCallback;",
            "Landroid/os/Handler;",
            "I",
        ),
        "V",
    ),
    UnregisterNetworkCallback1(
        "Landroid/net/ConnectivityManager;",
        "unregisterNetworkCallback",
        arrayOf("Landroid/net/ConnectivityManager\$NetworkCallback;"),
        "V",
    ),
    UnregisterNetworkCallback2(
        "Landroid/net/ConnectivityManager;",
        "unregisterNetworkCallback",
        arrayOf("Landroid/app/PendingIntent;"),
        "V",
    ),
}
