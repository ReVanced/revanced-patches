package app.revanced.integrations.all.connectivity.wifi.spoof;

import android.app.PendingIntent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

public class SpoofWifiPatch {

    // Used to check what the (real or fake) active network is (take a look at `hasTransport`).
    private static ConnectivityManager CONNECTIVITY_MANAGER;

    // If Wifi is not enabled, these are types that would pretend to be Wifi for android.net.Network (lower index = higher priority).
    // This does not apply to android.net.NetworkInfo, because we can pretend that Wifi is always active there.
    //
    // VPN should be a fallback, because Reverse Tethering uses VPN.
    private static final int[] FAKE_FALLBACK_NETWORKS = { NetworkCapabilities.TRANSPORT_ETHERNET, NetworkCapabilities.TRANSPORT_VPN };

    // In order to initialize our own ConnectivityManager, if it isn't initialized yet.
    public static Object getSystemService(Context context, String name) {
        Object result = context.getSystemService(name);
        if (CONNECTIVITY_MANAGER == null) {
            if (Context.CONNECTIVITY_SERVICE.equals(name)) {
                CONNECTIVITY_MANAGER = (ConnectivityManager) result;
            } else {
                CONNECTIVITY_MANAGER = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            }
        }
        return result;
    }

    // In order to initialize our own ConnectivityManager, if it isn't initialized yet.
    public static Object getSystemService(Context context, Class<?> serviceClass) {
        Object result = context.getSystemService(serviceClass);
        if (CONNECTIVITY_MANAGER == null) {
            if (serviceClass == ConnectivityManager.class) {
                CONNECTIVITY_MANAGER = (ConnectivityManager) result;
            } else {
                CONNECTIVITY_MANAGER = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            }
        }
        return result;
    }

    // Simply always return Wifi as active network.
    public static NetworkInfo getActiveNetworkInfo(ConnectivityManager connectivityManager) {
        for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return networkInfo;
            }
        }
        return connectivityManager.getActiveNetworkInfo();
    }

    // Pretend Wifi is always connected.
    public static boolean isConnected(NetworkInfo networkInfo) {
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return networkInfo.isConnected();
    }

    // Pretend Wifi is always connected.
    public static boolean isConnectedOrConnecting(NetworkInfo networkInfo) {
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return networkInfo.isConnectedOrConnecting();
    }

    // Pretend Wifi is always available.
    public static boolean isAvailable(NetworkInfo networkInfo) {
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return networkInfo.isAvailable();
    }

    // Pretend Wifi is always connected.
    public static NetworkInfo.State getState(NetworkInfo networkInfo) {
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NetworkInfo.State.CONNECTED;
        }
        return networkInfo.getState();
    }

    // Pretend Wifi is always connected.
    public static NetworkInfo.DetailedState getDetailedState(NetworkInfo networkInfo) {
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NetworkInfo.DetailedState.CONNECTED;
        }
        return networkInfo.getDetailedState();
    }

    // Pretend Wifi is enabled, so connection isn't metered.
    public static boolean isActiveNetworkMetered(ConnectivityManager connectivityManager) {
        return false;
    }

    // Returns the Wifi network, if Wifi is enabled.
    // Otherwise if one of our fallbacks has a connection, return them.
    // And as a last resort, return the default active network.
    public static Network getActiveNetwork(ConnectivityManager connectivityManager) {
        Network[] prioritizedNetworks = new Network[FAKE_FALLBACK_NETWORKS.length];
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) {
                continue;
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return network;
            }
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                for (int i = 0; i < FAKE_FALLBACK_NETWORKS.length; i++) {
                    int transportType = FAKE_FALLBACK_NETWORKS[i];
                    if (networkCapabilities.hasTransport(transportType)) {
                        prioritizedNetworks[i] = network;
                        break;
                    }
                }
            }
        }
        for (Network network : prioritizedNetworks) {
            if (network != null) {
                return network;
            }
        }
        return connectivityManager.getActiveNetwork();
    }

    // If the given network is a real or fake Wifi connection, return a Wifi network.
    // Otherwise fallback to default implementation.
    public static NetworkInfo getNetworkInfo(ConnectivityManager connectivityManager, Network network) {
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        if (networkCapabilities != null && hasTransport(networkCapabilities, NetworkCapabilities.TRANSPORT_WIFI)) {
            for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return networkInfo;
                }
            }
        }
        return connectivityManager.getNetworkInfo(network);
    }

    // If we are checking if the NetworkCapabilities use Wifi, return yes if
    //  - it is a real Wifi connection,
    //  - or the NetworkCapabilities are from a network pretending being a Wifi network.
    // Otherwise fallback to default implementation.
    public static boolean hasTransport(NetworkCapabilities networkCapabilities, int transportType) {
        if (transportType == NetworkCapabilities.TRANSPORT_WIFI) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true;
            }
            if (CONNECTIVITY_MANAGER != null) {
                Network activeNetwork = getActiveNetwork(CONNECTIVITY_MANAGER);
                NetworkCapabilities activeNetworkCapabilities = CONNECTIVITY_MANAGER.getNetworkCapabilities(activeNetwork);
                if (activeNetworkCapabilities != null) {
                    for (int fallbackTransportType : FAKE_FALLBACK_NETWORKS) {
                        if (activeNetworkCapabilities.hasTransport(fallbackTransportType) && networkCapabilities.hasTransport(fallbackTransportType)) {
                            return true;
                        }
                    }
                }
            }
        }
        return networkCapabilities.hasTransport(transportType);
    }

    // If the given network is a real or fake Wifi connection, pretend it has a connection (and some other things).
    public static boolean hasCapability(NetworkCapabilities networkCapabilities, int capability) {
        if (hasTransport(networkCapabilities, NetworkCapabilities.TRANSPORT_WIFI) && (
                capability == NetworkCapabilities.NET_CAPABILITY_INTERNET
                        || capability == NetworkCapabilities.NET_CAPABILITY_FOREGROUND
                        || capability == NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED
                        || capability == NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                        || capability == NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED
                        || capability == NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING
                        || capability == NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED
                        || capability == NetworkCapabilities.NET_CAPABILITY_NOT_VPN
                        || capability == NetworkCapabilities.NET_CAPABILITY_TRUSTED
                        || capability == NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return true;
        }
        return networkCapabilities.hasCapability(capability);
    }

    // If it waits for Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void registerBestMatchingNetworkCallback(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback, Handler handler) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.of(handler),
                () -> connectivityManager.registerBestMatchingNetworkCallback(request, networkCallback, handler)
        );
    }

    // If it waits for Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void registerDefaultNetworkCallback(ConnectivityManager connectivityManager, ConnectivityManager.NetworkCallback networkCallback) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.empty(),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.empty(),
                () -> connectivityManager.registerDefaultNetworkCallback(networkCallback)
        );
    }

    // If it waits for Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void registerDefaultNetworkCallback(ConnectivityManager connectivityManager, ConnectivityManager.NetworkCallback networkCallback, Handler handler) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.empty(),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.of(handler),
                () -> connectivityManager.registerDefaultNetworkCallback(networkCallback, handler)
        );
    }

    // If it waits for Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    public static void registerNetworkCallback(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.empty(),
                () -> connectivityManager.registerNetworkCallback(request, networkCallback)
        );
    }

    // If it waits for Wifi connectivity, pretend it is fulfilled immediately.
    public static void registerNetworkCallback(ConnectivityManager connectivityManager, NetworkRequest request, PendingIntent operation) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.empty(),
                Utils.Option.of(operation),
                Utils.Option.empty(),
                () -> connectivityManager.registerNetworkCallback(request, operation)
        );
    }

    // If it waits for Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void registerNetworkCallback(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback, Handler handler) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.of(handler),
                () -> connectivityManager.registerNetworkCallback(request, networkCallback, handler)
        );
    }

    // If it requests Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    public static void requestNetwork(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.empty(),
                () -> connectivityManager.requestNetwork(request, networkCallback)
        );
    }

    // If it requests Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void requestNetwork(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback, int timeoutMs) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.empty(),
                () -> connectivityManager.requestNetwork(request, networkCallback, timeoutMs)
        );
    }

    // If it requests Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void requestNetwork(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback, Handler handler) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.of(handler),
                () -> connectivityManager.requestNetwork(request, networkCallback, handler)
        );
    }

    // If it requests Wifi connectivity, pretend it is fulfilled immediately.
    public static void requestNetwork(ConnectivityManager connectivityManager, NetworkRequest request, PendingIntent operation) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.empty(),
                Utils.Option.of(operation),
                Utils.Option.empty(),
                () -> connectivityManager.requestNetwork(request, operation)
        );
    }

    // If it requests Wifi connectivity, pretend it is fulfilled immediately if we have an active network.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void requestNetwork(ConnectivityManager connectivityManager, NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback, Handler handler, int timeoutMs) {
        Utils.networkCallback(
                connectivityManager,
                Utils.Option.of(request),
                Utils.Option.of(networkCallback),
                Utils.Option.empty(),
                Utils.Option.of(handler),
                () -> connectivityManager.requestNetwork(request, networkCallback, handler, timeoutMs)
        );
    }

    public static void unregisterNetworkCallback(ConnectivityManager connectivityManager, ConnectivityManager.NetworkCallback networkCallback) {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (IllegalArgumentException ignore) {
            // ignore: NetworkCallback was not registered
        }
    }

    public static void unregisterNetworkCallback(ConnectivityManager connectivityManager, PendingIntent operation) {
        try {
            connectivityManager.unregisterNetworkCallback(operation);
        } catch (IllegalArgumentException ignore) {
            // ignore: PendingIntent was not registered
        }
    }

    private static class Utils {
        private static class Option<T> {
            private final T value;
            private final boolean isPresent;

            private Option(T value, boolean isPresent) {
                this.value = value;
                this.isPresent = isPresent;
            }

            private static <T> Option<T> of(T value) {
                return new Option<>(value, true);
            }

            private static <T> Option<T> empty() {
                return new Option<>(null, false);
            }
        }

        private static void networkCallback(
                ConnectivityManager connectivityManager,
                Option<NetworkRequest> request,
                Option<ConnectivityManager.NetworkCallback> networkCallback,
                Option<PendingIntent> operation,
                Option<Handler> handler,
                Runnable fallback
        ) {
            if(!request.isPresent || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && request.value != null && requestsWifiNetwork(request.value))) {
                Runnable runnable = null;
                if (networkCallback.isPresent && networkCallback.value != null) {
                    Network network = activeWifiNetwork(connectivityManager);
                    if (network != null) {
                        runnable = () -> networkCallback.value.onAvailable(network);
                    }
                } else if (operation.isPresent && operation.value != null) {
                    runnable = () -> {
                        try {
                            operation.value.send();
                        } catch (PendingIntent.CanceledException ignore) {}
                    };
                }
                if (runnable != null) {
                    if (handler.isPresent) {
                        if (handler.value != null) {
                            handler.value.post(runnable);
                            return;
                        }
                    } else {
                        runnable.run();
                        return;
                    }
                }
            }
            fallback.run();
        }

        // Returns an active (maybe fake) Wifi network if there is one, otherwise null.
        private static Network activeWifiNetwork(ConnectivityManager connectivityManager) {
            Network network = getActiveNetwork(connectivityManager);
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null && hasTransport(networkCapabilities, NetworkCapabilities.TRANSPORT_WIFI)) {
                return network;
            }
            return null;
        }

        // Whether a Wifi network with connection is requested.
        @RequiresApi(api = Build.VERSION_CODES.P)
        private static boolean requestsWifiNetwork(NetworkRequest request) {
            return request.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    && (request.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        || request.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        }
    }
}
