package app.revanced.patches.all.connectivity.wifi.spoof

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.Instruction35cInfo
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

@Patch(
    name = "Spoof Wi-Fi connection",
    description = "Spoofs an existing Wi-Fi connection.",
    use = false,
    requiresIntegrations = true
)
@Suppress("unused")
object SpoofWifiPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX = "Lapp/revanced/integrations/all/connectivity/wifi/spoof/SpoofWifiPatch"
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "$INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX;"

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ) = filterMapInstruction35c<MethodCall>(
        INTEGRATIONS_CLASS_DESCRIPTOR_PREFIX,
        classDef,
        instruction,
        instructionIndex
    )

    override fun transform(mutableMethod: MutableMethod, entry: Instruction35cInfo) {
        val (methodType, instruction, instructionIndex) = entry
        methodType.replaceInvokeVirtualWithIntegrations(INTEGRATIONS_CLASS_DESCRIPTOR, mutableMethod, instruction, instructionIndex)
    }


    // Information about method calls we want to replace
    private enum class MethodCall(
        override val definedClassName: String,
        override val methodName: String,
        override val methodParams: Array<String>,
        override val returnType: String,
    ): IMethodCall {
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
            arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;", "Landroid/os/Handler;"),
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
            arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;", "Landroid/os/Handler;"),
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
            arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;", "Landroid/os/Handler;"),
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
            arrayOf("Landroid/net/NetworkRequest;", "Landroid/net/ConnectivityManager\$NetworkCallback;", "Landroid/os/Handler;", "I"),
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
        );
    }
}
