package fi.vanced.libraries.youtube.whitelisting;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.channelName;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.vanced.libraries.youtube.player.ChannelModel;
import fi.vanced.utils.ObjectSerializer;
import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;

public class Whitelist {
    private static final String TAG = "VI - Whitelisting";
    private static final Map<WhitelistType, List<ChannelModel>> whitelistMap = parseWhitelist(YouTubeTikTokRoot_Application.getAppContext());
    private static final Map<WhitelistType, Boolean> enabledMap = parseEnabledMap(YouTubeTikTokRoot_Application.getAppContext());

    private Whitelist() {}

    public static boolean shouldShowAds() {
        return isWhitelisted(WhitelistType.ADS);
    }

    public static boolean shouldShowSegments() {
        return !isWhitelisted(WhitelistType.SPONSORBLOCK);
    }

    private static Map<WhitelistType, List<ChannelModel>> parseWhitelist(Context context) {
        if (context == null) {
            return Collections.emptyMap();
        }
        WhitelistType[] whitelistTypes = WhitelistType.values();
        Map<WhitelistType, List<ChannelModel>> whitelistMap = new HashMap<>(whitelistTypes.length);

        for (WhitelistType whitelistType : whitelistTypes) {
            SharedPreferences preferences = VancedUtils.getPreferences(context, whitelistType.getPreferencesName());
            String serializedChannels = preferences.getString("channels", null);
            if (serializedChannels == null) {
                if (debug) {
                    Log.d(TAG, String.format("channels string was null for %s whitelisting", whitelistType));
                }
                return Collections.emptyMap();
            }
            try {
                List<ChannelModel> deserializedChannels = (List<ChannelModel>) ObjectSerializer.deserialize(serializedChannels);
                if (debug) {
                    Log.d(TAG, serializedChannels);
                    for (ChannelModel channel : deserializedChannels) {
                        Log.d(TAG, String.format("Whitelisted channel %s (%s) for type %s", channel.getAuthor(), channel.getChannelId(), whitelistType));
                    }
                }
                whitelistMap.put(whitelistType, deserializedChannels);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return whitelistMap;
    }

    private static Map<WhitelistType, Boolean> parseEnabledMap(Context context) {
        WhitelistType[] whitelistTypes = WhitelistType.values();
        Map<WhitelistType, Boolean> enabledMap = new HashMap<>(whitelistTypes.length);
        for (WhitelistType whitelistType : whitelistTypes) {
            enabledMap.put(whitelistType, SharedPrefUtils.getBoolean(context, "youtube", whitelistType.getPreferenceEnabledName()));
        }
        return enabledMap;
    }

    private static boolean isWhitelisted(WhitelistType whitelistType) {
        boolean isEnabled = enabledMap.get(whitelistType);
        if (!isEnabled) {
            return false;
        }
        if (channelName == null || channelName.trim().isEmpty()) {
            if (debug) {
                Log.d(TAG, String.format("Can't check whitelist status for %s because channel name was missing", whitelistType));
            }
            return false;
        }
        List<ChannelModel> whitelistedChannels = whitelistMap.get(whitelistType);
        for (ChannelModel channel : whitelistedChannels) {
            if (channel.getAuthor().equals(channelName)) {
                if (debug) {
                    Log.d(TAG, String.format("Whitelist for channel %s for type %s", channelName, whitelistType));
                }
                return true;
            }
        }
        return false;
    }
}