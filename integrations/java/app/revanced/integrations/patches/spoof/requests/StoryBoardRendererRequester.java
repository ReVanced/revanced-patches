package app.revanced.integrations.patches.spoof.requests;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.patches.spoof.StoryboardRenderer;
import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static app.revanced.integrations.patches.spoof.requests.PlayerRoutes.POST_STORYBOARD_SPEC_RENDERER;

public class StoryBoardRendererRequester {
    private static final String INNER_TUBE_BODY =
            "{" +
                    "\"context\": " +
                    "{" +
                    "\"client\": " +
                    "{ " +
                    "\"clientName\": \"ANDROID\", \"clientVersion\": \"18.37.36\", \"platform\": \"MOBILE\", " +
                    "\"osName\": \"Android\", \"osVersion\": \"12\", \"androidSdkVersion\": 31 " +
                    "} " +
                    "}, " +
                    "\"videoId\": \"%s\"" +
                    "}";

    private StoryBoardRendererRequester() {
    }

    @Nullable
    public static StoryboardRenderer fetchStoryboardRenderer(@NonNull String videoId) {
        try {
            ReVancedUtils.verifyOffMainThread();
            Objects.requireNonNull(videoId);

            final byte[] innerTubeBody = String.format(INNER_TUBE_BODY, videoId).getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = PlayerRoutes.getPlayerResponseConnectionFromRoute(POST_STORYBOARD_SPEC_RENDERER);
            connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);

            final int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                final JSONObject playerResponse = Requester.parseJSONObject(connection);

                if (!playerResponse.has("storyboards")) {
                    // Video is age restricted or paid.
                    LogHelper.printDebug(() -> "Video has no public storyboard: " + videoId);
                    return null;
                }
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
            } else {
                LogHelper.printException(() -> "API not available: " + responseCode);
                connection.disconnect();
            }
        } catch (SocketTimeoutException ex) {
            LogHelper.printException(() -> "API timed out", ex);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to fetch StoryBoard URL", ex);
        }

        return null;
    }
}