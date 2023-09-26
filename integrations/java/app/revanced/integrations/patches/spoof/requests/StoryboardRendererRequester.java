package app.revanced.integrations.patches.spoof.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.patches.spoof.StoryboardRenderer;
import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static app.revanced.integrations.patches.spoof.requests.PlayerRoutes.*;

public class StoryboardRendererRequester {
    private StoryboardRendererRequester() {
    }

    @Nullable
    private static JSONObject fetchPlayerResponse(@NonNull String requestBody) {
        try {
            ReVancedUtils.verifyOffMainThread();
            Objects.requireNonNull(requestBody);

            final byte[] innerTubeBody = requestBody.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(GET_STORYBOARD_SPEC_RENDERER);
            connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return Requester.parseJSONObject(connection);

            LogHelper.printException(() -> "API not available: " + responseCode);
            connection.disconnect();
        } catch (SocketTimeoutException ex) {
            LogHelper.printException(() -> "API timed out", ex);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to fetch storyboard URL", ex);
        }

        return null;
    }

    private static boolean isPlayabilityStatusOk(@NonNull JSONObject playerResponse) {
        try {
            return playerResponse.getJSONObject("playabilityStatus").getString("status").equals("OK");
        } catch (JSONException e) {
            LogHelper.printException(() -> "Failed to get playabilityStatus", e);
        }

        return false;
    }

    /**
     * Fetches the storyboardRenderer from the innerTubeBody.
     * @param innerTubeBody The innerTubeBody to use to fetch the storyboardRenderer.
     * @return StoryboardRenderer or null if playabilityStatus is not OK.
     */
    @Nullable
    private static StoryboardRenderer getStoryboardRendererUsingBody(@NonNull String innerTubeBody) {
        final JSONObject playerResponse = fetchPlayerResponse(innerTubeBody);
        if (playerResponse != null && isPlayabilityStatusOk(playerResponse))
            return getStoryboardRendererUsingResponse(playerResponse);

        return null;
    }

    @Nullable
    private static StoryboardRenderer getStoryboardRendererUsingResponse(@NonNull JSONObject playerResponse) {
        try {
            final JSONObject storyboards = playerResponse.getJSONObject("storyboards");
            final String storyboardsRendererTag = storyboards.has("playerLiveStoryboardSpecRenderer")
                    ? "playerLiveStoryboardSpecRenderer"
                    : "playerStoryboardSpecRenderer";

            final var rendererElement = storyboards.getJSONObject(storyboardsRendererTag);
            StoryboardRenderer renderer = new StoryboardRenderer(
                    rendererElement.getString("spec"),
                    rendererElement.getInt("recommendedLevel")
            );

            LogHelper.printDebug(() -> "Fetched: " + renderer);

            return renderer;
        } catch (JSONException e) {
            LogHelper.printException(() -> "Failed to get storyboardRenderer", e);
        }

        return null;
    }

    @Nullable
    public static StoryboardRenderer getStoryboardRenderer(@NonNull String videoId) {
        try {
            Objects.requireNonNull(videoId);

            var renderer = getStoryboardRendererUsingBody(String.format(ANDROID_INNER_TUBE_BODY, videoId));
            if (renderer == null) {
                LogHelper.printDebug(() -> videoId + " not available using android client");
                renderer = getStoryboardRendererUsingBody(String.format(TV_EMBED_INNER_TUBE_BODY, videoId, videoId));
                if (renderer == null) {
                    LogHelper.printDebug(() -> videoId + " not available using tv embedded client");
                }
            }

            return renderer;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to fetch storyboard URL", ex);
        }

        return null;
    }
}