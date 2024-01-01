package app.revanced.integrations.youtube.patches.spoof.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.patches.spoof.StoryboardRenderer;
import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static app.revanced.integrations.youtube.patches.spoof.requests.PlayerRoutes.*;

public class StoryboardRendererRequester {

    /**
     * For videos that have no storyboard.
     * Usually for low resolution videos as old as YouTube itself.
     * Does not include paid videos where the renderer fetch fails.
     */
    private static final StoryboardRenderer emptyStoryboard
            = new StoryboardRenderer(null, false, null);

    private StoryboardRendererRequester() {
    }

    private static void randomlyWaitIfLocallyDebugging() {
        final boolean randomlyWait = false; // Enable to simulate slow connection responses.
        if (randomlyWait) {
            final long maximumTimeToRandomlyWait = 10000;
            Utils.doNothingForDuration(maximumTimeToRandomlyWait);
        }
    }

    private static void handleConnectionError(@NonNull String toastMessage, @Nullable Exception ex,
                                              boolean showToastOnIOException) {
        if (showToastOnIOException) Utils.showToastShort(toastMessage);
        Logger.printInfo(() -> toastMessage, ex);
    }

    @Nullable
    private static JSONObject fetchPlayerResponse(@NonNull String requestBody, boolean showToastOnIOException) {
        final long startTime = System.currentTimeMillis();
        try {
            Utils.verifyOffMainThread();
            Objects.requireNonNull(requestBody);

            final byte[] innerTubeBody = requestBody.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(GET_STORYBOARD_SPEC_RENDERER);
            connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);

            final int responseCode = connection.getResponseCode();
            randomlyWaitIfLocallyDebugging();
            if (responseCode == 200) return Requester.parseJSONObject(connection);

            // Always show a toast for this, as a non 200 response means something is broken.
            handleConnectionError("Spoof storyboard not available: " + responseCode,
                    null, showToastOnIOException || BaseSettings.DEBUG_TOAST_ON_ERROR.get());
            connection.disconnect();
        } catch (SocketTimeoutException ex) {
            handleConnectionError("Spoof storyboard temporarily not available (API timed out)",
                    ex, showToastOnIOException);
        } catch (IOException ex) {
            handleConnectionError("Spoof storyboard temporarily not available: " + ex.getMessage(),
                    ex, showToastOnIOException);
        } catch (Exception ex) {
            Logger.printException(() -> "Spoof storyboard fetch failed", ex); // Should never happen.
        } finally {
            Logger.printDebug(() -> "Request took: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        return null;
    }

    private static boolean isPlayabilityStatusOk(@NonNull JSONObject playerResponse) {
        try {
            return playerResponse.getJSONObject("playabilityStatus").getString("status").equals("OK");
        } catch (JSONException e) {
            Logger.printDebug(() -> "Failed to get playabilityStatus for response: " + playerResponse);
        }

        return false;
    }

    /**
     * Fetches the storyboardRenderer from the innerTubeBody.
     * @param innerTubeBody The innerTubeBody to use to fetch the storyboardRenderer.
     * @return StoryboardRenderer or null if playabilityStatus is not OK.
     */
    @Nullable
    private static StoryboardRenderer getStoryboardRendererUsingBody(@NonNull String innerTubeBody,
                                                                     boolean showToastOnIOException) {
        final JSONObject playerResponse = fetchPlayerResponse(innerTubeBody, showToastOnIOException);
        if (playerResponse != null && isPlayabilityStatusOk(playerResponse))
            return getStoryboardRendererUsingResponse(playerResponse);

        return null;
    }

    @Nullable
    private static StoryboardRenderer getStoryboardRendererUsingResponse(@NonNull JSONObject playerResponse) {
        try {
            Logger.printDebug(() -> "Parsing response: " + playerResponse);
            if (!playerResponse.has("storyboards")) {
                Logger.printDebug(() -> "Using empty storyboard");
                return emptyStoryboard;
            }
            final JSONObject storyboards = playerResponse.getJSONObject("storyboards");
            final boolean isLiveStream = storyboards.has("playerLiveStoryboardSpecRenderer");
            final String storyboardsRendererTag = isLiveStream
                    ? "playerLiveStoryboardSpecRenderer"
                    : "playerStoryboardSpecRenderer";

            final var rendererElement = storyboards.getJSONObject(storyboardsRendererTag);
            StoryboardRenderer renderer = new StoryboardRenderer(
                    rendererElement.getString("spec"),
                    isLiveStream,
                    rendererElement.has("recommendedLevel")
                            ? rendererElement.getInt("recommendedLevel")
                            : null
            );

            Logger.printDebug(() -> "Fetched: " + renderer);

            return renderer;
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to get storyboardRenderer", e);
        }

        return null;
    }

    @Nullable
    public static StoryboardRenderer getStoryboardRenderer(@NonNull String videoId) {
        Objects.requireNonNull(videoId);

        var renderer = getStoryboardRendererUsingBody(
                String.format(ANDROID_INNER_TUBE_BODY, videoId), false);
        if (renderer == null) {
            Logger.printDebug(() -> videoId + " not available using Android client");
            renderer = getStoryboardRendererUsingBody(
                    String.format(TV_EMBED_INNER_TUBE_BODY, videoId, videoId), true);
            if (renderer == null) {
                Logger.printDebug(() -> videoId + " not available using TV embedded client");
            }
        }

        return renderer;
    }
}