package app.revanced.extension.shared.spoof.requests;

import static app.revanced.extension.shared.ByteTrieSearch.convertStringsToBytes;
import static app.revanced.extension.shared.Utils.isNotEmpty;
import static app.revanced.extension.shared.spoof.requests.PlayerRoutes.GET_PLAYER_STREAMING_DATA;
import static app.revanced.extension.shared.spoof.requests.PlayerRoutes.GET_REEL_STREAMING_DATA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.innertube.PlayerResponseOuterClass;
import app.revanced.extension.shared.innertube.PlayerResponseOuterClass.PlayerResponse;
import app.revanced.extension.shared.innertube.PlayerResponseOuterClass.StreamingData;
import app.revanced.extension.shared.innertube.ReelItemWatchResponseOuterClass.ReelItemWatchResponse;
import app.revanced.extension.shared.requests.Route;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.spoof.ClientType;

/**
 * Video streaming data. Fetching is tied to the behavior YT uses,
 * where this class fetches the streams only when YT fetches.
 * <p>
 * Effectively the cache expiration of these fetches is the same as the stock app,
 * since the stock app would not use expired streams and therefor
 * the extension replace stream hook is called only if YT
 * did use its own client streams.
 */
public class StreamingDataRequest {

    private static volatile  ClientType[] clientOrderToUse = ClientType.values();

    public static void setClientOrderToUse(List<ClientType> availableClients, ClientType preferredClient) {
        Objects.requireNonNull(preferredClient);

        int availableClientSize = availableClients.size();
        if (!availableClients.contains(preferredClient)) {
            availableClientSize++;
        }

        clientOrderToUse = new ClientType[availableClientSize];
        clientOrderToUse[0] = preferredClient;

        int i = 1;
        for (ClientType c : availableClients) {
            if (c != preferredClient) {
                clientOrderToUse[i++] = c;
            }
        }

        Logger.printDebug(() -> "Available spoof clients: " + Arrays.toString(clientOrderToUse));
    }

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String[] REQUEST_HEADER_KEYS = {
            AUTHORIZATION_HEADER, // Available only to logged-in users.
            "X-GOOG-API-FORMAT-VERSION",
            "X-Goog-Visitor-Id"
    };

    /**
     * TCP connection and HTTP read timeout.
     */
    private static final int HTTP_TIMEOUT_MILLISECONDS = 10 * 1000;

    /**
     * Any arbitrarily large value, but must be at least twice {@link #HTTP_TIMEOUT_MILLISECONDS}
     */
    private static final int MAX_MILLISECONDS_TO_WAIT_FOR_FETCH = 20 * 1000;

    /**
     * Cache limit must be greater than the maximum number of videos open at once,
     * which theoretically is more than 4 (3 Shorts + one regular minimized video).
     * But instead use a much larger value, to handle if a video viewed a while ago
     * is somehow still referenced. Each stream is a small array of Strings
     * so memory usage is not a concern.
     */
    private static final Map<String, StreamingDataRequest> cache = Collections.synchronizedMap(
            Utils.createSizeRestrictedMap(50));

    /**
     * Strings found in the response if the video is a livestream.
     */
    private static final ByteTrieSearch liveStreamBufferSearch = new ByteTrieSearch(
            convertStringsToBytes(
                    "yt_live_broadcast",
                    "yt_premiere_broadcast"
            )
    );

    private static volatile ClientType lastSpoofedClientType;

    public static String getLastSpoofedClientName() {
        ClientType client = lastSpoofedClientType;
        return client == null ? "Unknown" : client.friendlyName;
    }

    private final String videoId;

    private final Future<byte[]> future;

    private StreamingDataRequest(String videoId, Map<String, String> playerHeaders) {
        Objects.requireNonNull(playerHeaders);
        this.videoId = videoId;
        this.future = Utils.submitOnBackgroundThread(() -> fetch(videoId, playerHeaders));
    }

    public static void fetchRequest(String videoId, Map<String, String> fetchHeaders) {
        // Always fetch, even if there is an existing request for the same video.
        cache.put(videoId, new StreamingDataRequest(videoId, fetchHeaders));
    }

    @Nullable
    public static StreamingDataRequest getRequestForVideoId(String videoId) {
        return cache.get(videoId);
    }

    private static void handleConnectionError(String toastMessage, @Nullable Exception ex, boolean showToast) {
        if (showToast) Utils.showToastShort(toastMessage);
        Logger.printInfo(() -> toastMessage, ex);
    }

    private static void handleDebugToast(String toastMessage, ClientType clientType) {
        if (BaseSettings.DEBUG.get() && BaseSettings.DEBUG_TOAST_ON_ERROR.get()) {
            Utils.showToastShort(String.format(toastMessage, clientType));
        }
    }

    @Nullable
    private static HttpURLConnection send(ClientType clientType,
                                          String videoId,
                                          Map<String, String> playerHeaders,
                                          boolean showErrorToasts) {
        Objects.requireNonNull(clientType);
        Objects.requireNonNull(videoId);
        Objects.requireNonNull(playerHeaders);

        final long startTime = System.currentTimeMillis();

        try {
            Route.CompiledRoute route = clientType.usePlayerEndpoint ?
                    GET_PLAYER_STREAMING_DATA : GET_REEL_STREAMING_DATA;

            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(route, clientType);
            connection.setConnectTimeout(HTTP_TIMEOUT_MILLISECONDS);
            connection.setReadTimeout(HTTP_TIMEOUT_MILLISECONDS);

            boolean authHeadersIncludes = false;

            for (String key : REQUEST_HEADER_KEYS) {
                String value = playerHeaders.get(key);

                if (value != null) {
                    if (key.equals(AUTHORIZATION_HEADER)) {
                        if (!clientType.useAuth) {
                            Logger.printDebug(() -> "Not including request header: " + key);
                            continue;
                        }
                        authHeadersIncludes = true;
                    }

                    Logger.printDebug(() -> "Including request header: " + key);
                    connection.setRequestProperty(key, value);
                }
            }

            if (!authHeadersIncludes && clientType.useAuth) {
                Logger.printDebug(() -> "Skipping client since user is not logged in: " + clientType
                        + " videoId: " + videoId);
                return null;
            }

            Logger.printDebug(() -> "Fetching video streams for: " + videoId + " using client: " + clientType);

            String innerTubeBody = PlayerRoutes.createInnertubeBody(clientType, videoId);
            byte[] requestBody = innerTubeBody.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(requestBody.length);
            connection.getOutputStream().write(requestBody);

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return connection;

            // This situation likely means the patches are outdated.
            // Use a toast message that suggests updating.
            handleConnectionError("Playback error (App is outdated?) " + clientType + ": "
                            + responseCode + " response: " + connection.getResponseMessage(),
                    null, showErrorToasts);
        } catch (SocketTimeoutException ex) {
            handleConnectionError("Connection timeout", ex, showErrorToasts);
        } catch (IOException ex) {
            handleConnectionError("Network error", ex, showErrorToasts);
        } catch (Exception ex) {
            Logger.printException(() -> "send failed", ex);
        } finally {
            Logger.printDebug(() -> "video: " + videoId + " took: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return null;
    }

    private static byte[] fetch(String videoId, Map<String, String> playerHeaders) {
        final boolean debugEnabled = BaseSettings.DEBUG.get();

        // Retry with different client if empty response body is received.
        int i = 0;
        for (ClientType clientType : clientOrderToUse) {
            // Show an error if the last client type fails, or if debug is enabled then show for all attempts.
            final boolean showErrorToast = (++i == clientOrderToUse.length) || debugEnabled;

            HttpURLConnection connection = send(clientType, videoId, playerHeaders, showErrorToast);
            if (connection != null) {
                byte[] playerResponseBuffer = buildPlayerResponseBuffer(clientType, connection);
                if (playerResponseBuffer != null) {
                    lastSpoofedClientType = clientType;

                    return playerResponseBuffer;
                }
            }
        }

        lastSpoofedClientType = null;
        handleConnectionError("Could not fetch any client streams", null, true);
        return null;
    }

    @Nullable
    private static byte[] buildPlayerResponseBuffer(ClientType clientType,
                                                    HttpURLConnection connection) {
        // gzip encoding doesn't response with content length (-1),
        // but empty response body does.
        if (connection.getContentLength() == 0) {
            handleDebugToast("Debug: Ignoring empty spoof stream client (%s)", clientType);
            return null;
        }

        try (InputStream inputStream = connection.getInputStream()) {
            PlayerResponse playerResponse = clientType.usePlayerEndpoint
                    ? PlayerResponse.parseFrom(inputStream)
                    : ReelItemWatchResponse.parseFrom(inputStream).getPlayerResponse();

            var playabilityStatus = playerResponse.getPlayabilityStatus();
            if (playabilityStatus.getStatus() != PlayerResponseOuterClass.Status.OK) {
                handleDebugToast("Debug: Ignoring unplayable video (%s)", clientType);
                String reason = playabilityStatus.getReason();
                if (isNotEmpty(reason)) {
                    Logger.printDebug(() -> String.format("Debug: Ignoring unplayable video (%s), reason: %s", clientType, reason));
                }

                return null;
            }

            PlayerResponse.Builder responseBuilder = playerResponse.toBuilder();
            if (!playerResponse.hasStreamingData()) {
                handleDebugToast("Debug: Ignoring empty streaming data (%s)", clientType);
                return null;
            }

            // Android Studio only supports the HLS protocol for live streams.
            // HLS protocol can theoretically be played with ExoPlayer,
            // but the related code has not yet been implemented.
            // If DASH protocol is not available, the client will be skipped.
            StreamingData streamingData = playerResponse.getStreamingData();
            if (streamingData.getAdaptiveFormatsCount() == 0) {
                handleDebugToast("Debug: Ignoring empty adaptiveFormat (%s)", clientType);
                return null;
            }

            return responseBuilder.build().toByteArray();
        } catch (IOException ex) {
            Logger.printException(() -> "Failed to write player response to buffer array", ex);
            return null;
        }
    }

    public boolean fetchCompleted() {
        return future.isDone();
    }

    @Nullable
    public byte[] getStream() {
        try {
            return future.get(MAX_MILLISECONDS_TO_WAIT_FOR_FETCH, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            Logger.printInfo(() -> "getStream timed out", ex);
        } catch (InterruptedException ex) {
            Logger.printException(() -> "getStream interrupted", ex);
            Thread.currentThread().interrupt(); // Restore interrupt status flag.
        } catch (ExecutionException ex) {
            Logger.printException(() -> "getStream failure", ex);
        }

        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "StreamingDataRequest{" + "videoId='" + videoId + '\'' + '}';
    }
}
