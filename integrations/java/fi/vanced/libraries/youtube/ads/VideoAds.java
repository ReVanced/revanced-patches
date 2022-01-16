package fi.vanced.libraries.youtube.ads;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.channelName;
import static fi.vanced.libraries.youtube.ui.SlimButtonContainer.adBlockButton;
import static fi.vanced.utils.VancedUtils.getPreferences;
import static fi.vanced.utils.VancedUtils.parseJson;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import fi.razerman.youtube.XGlobals;
import fi.vanced.libraries.youtube.player.ChannelModel;
import fi.vanced.libraries.youtube.player.VideoInformation;
import fi.vanced.utils.ObjectSerializer;
import fi.vanced.utils.SharedPrefUtils;

public class VideoAds {
    public static final String TAG = "VI - VideoAds";
    public static final String PREFERENCES_NAME = "channel-whitelist";
    public static boolean isEnabled;
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1";
    private static final String YT_API_KEY = "replaceMeWithTheYouTubeAPIKey";
    private static ArrayList<ChannelModel> whiteList;
    private static Thread fetchThread = null;

    static {
        whiteList = parseWhitelist(YouTubeTikTokRoot_Application.getAppContext());
        isEnabled = SharedPrefUtils.getBoolean(YouTubeTikTokRoot_Application.getAppContext(), "youtube", "vanced_videoadwhitelisting_enabled", false);
    }

    // Call to this needs to be injected in YT code
    public static void setChannelName(String channelName) {
        if (debug) {
            Log.d(TAG, "channel name set to " + channelName);
        }
        VideoInformation.channelName = channelName;

        if (!isEnabled) return;

        if (adBlockButton != null) {
            adBlockButton.changeEnabled(getShouldShowAds());
        }
    }

    // Call to this needs to be injected in YT code (CURRENTLY NOT USED)
    public static void newVideoLoaded(String videoId) {
        if (debug) {
            Log.d(TAG, "newVideoLoaded - " + videoId);
        }

        try {
            if (fetchThread != null && fetchThread.getState() != Thread.State.TERMINATED) {
                if (debug) {
                    Log.d(TAG, "Interrupting the thread. Current state " + fetchThread.getState());
                }
                fetchThread.interrupt();
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error in the fetch thread", ex);
        }

        fetchThread = new Thread(() -> {
            try {
                if (debug) {
                    Log.d(TAG, "Fetching channelId for " + videoId);
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(YT_API_URL + "/player?key=" + YT_API_KEY).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(2 * 1000);

                // TODO: Actually fetch the version
                String jsonInputString = "{\"context\": {\"client\": { \"clientName\": \"Android\", \"clientVersion\": \"16.49.37\" } }, \"videoId\": \"" + videoId + "\"}";
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                if (connection.getResponseCode() == 200) {
                    JSONObject json = new JSONObject(parseJson(connection));
                    JSONObject videoInfo = json.getJSONObject("videoDetails");
                    ChannelModel channelModel = new ChannelModel(videoInfo.getString("author"), videoInfo.getString("channelId"));
                    if (debug) {
                        Log.d(TAG, "channelId " + channelModel.getChannelId() + " fetched for author " + channelModel.getAuthor());
                    }
                }
                else if (debug) {
                    Log.d(TAG, "player fetch response was " + connection.getResponseCode());
                }
            }
            catch (Exception ex) {
                Log.e(TAG, "Failed to fetch channelId", ex);
                return;
            }
        });

        fetchThread.start();
    }

    public static boolean getShouldShowAds() {
        if (!isEnabled) return false;

        if (channelName == null || channelName.isEmpty() || channelName.trim().isEmpty()) {
            if (XGlobals.debug) {
                Log.d(TAG, "getShouldShowAds skipped because channelId was null");
            }

            return false;
        }

        for (ChannelModel channelModel: whiteList) {
            if (channelModel.getAuthor().equals(channelName)) {
                if (XGlobals.debug) {
                    Log.d(TAG, "Video ad whitelist for " + channelName);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean addToWhitelist(Context context, String channelName, String channelId) {
        try {
            // Check that the channel doesn't exist already (can happen if for example the channel changes the name)
            // If it exists, remove it
            Iterator<ChannelModel> iterator = whiteList.iterator();
            while(iterator.hasNext())
            {
                ChannelModel value = iterator.next();
                if (value.getChannelId().equals(channelId))
                {
                    if (XGlobals.debug) {
                        Log.d(TAG, String.format("Tried whitelisting an existing channel again. Old info (%1$s | %2$s) - New info (%3$s | %4$s)",
                                value.getAuthor(), value.getChannelId(), channelName, channelId));
                    }
                    iterator.remove();
                    break;
                }
            }

            whiteList.add(new ChannelModel(channelName, channelId));
            updateWhitelist(context);
            return true;
        }
        catch (Exception ex) {
            Log.d(TAG, "Unable to add " + channelName + " with id " + channelId + " to whitelist");
        }

        return false;
    }

    public static boolean removeFromWhitelist(Context context, String channelName) {
        try {
            //whiteList.removeIf(x -> x.getAuthor().equals(channelName)); // Requires Android N

            Iterator<ChannelModel> iterator = whiteList.iterator();
            while(iterator.hasNext())
            {
                ChannelModel value = iterator.next();
                if (value.getAuthor().equals(channelName))
                {
                    iterator.remove();
                    break;
                }
            }
            updateWhitelist(context);
            return true;
        }
        catch (Exception ex) {
            Log.d(TAG, "Unable to remove " + channelName + " from whitelist");
        }

        return false;
    }

    private static void updateWhitelist(Context context) {
        if (context == null) return;

        SharedPreferences preferences = getPreferences(context, PREFERENCES_NAME);
        SharedPreferences.Editor editor = preferences.edit();

        try {
            editor.putString("channels", ObjectSerializer.serialize(whiteList));
        } catch (IOException e) {
            e.printStackTrace();
        }

        editor.apply();
    }

    private static ArrayList<ChannelModel> parseWhitelist(Context context) {
        if (context == null) return new ArrayList<>();

        SharedPreferences preferences = getPreferences(context, PREFERENCES_NAME);
        try {
            String channels = preferences.getString("channels", null);
            if (channels == null) {
                if (debug) {
                    Log.d(TAG, "channels string was null for ad whitelisting");
                }

                return new ArrayList<>();
            }

            ArrayList<ChannelModel> channelModels = (ArrayList<ChannelModel>) ObjectSerializer.deserialize(channels);
            if (debug) {
                Log.d(TAG, channels);
                for (ChannelModel channelModel: channelModels) {
                    Log.d(TAG, "Ad whitelisted " + channelModel.getAuthor() + " with id of " + channelModel.getChannelId());
                }
            }

            return channelModels;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
