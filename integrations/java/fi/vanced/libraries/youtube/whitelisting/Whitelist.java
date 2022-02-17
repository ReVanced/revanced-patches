package fi.vanced.libraries.youtube.whitelisting;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.channelName;
import static fi.vanced.libraries.youtube.ui.SlimButtonContainer.adBlockButton;
import static fi.vanced.libraries.youtube.ui.SlimButtonContainer.sbWhitelistButton;
import static fi.vanced.utils.VancedUtils.getPreferences;
import static pl.jakubweg.StringRef.str;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.vanced.libraries.youtube.player.ChannelModel;
import fi.vanced.libraries.youtube.player.VideoInformation;
import fi.vanced.utils.ObjectSerializer;
import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;

public class Whitelist {
    private static final String TAG = "VI - Whitelisting";
    private static final Map<WhitelistType, ArrayList<ChannelModel>> whitelistMap = parseWhitelist(YouTubeTikTokRoot_Application.getAppContext());
    private static final Map<WhitelistType, Boolean> enabledMap = parseEnabledMap(YouTubeTikTokRoot_Application.getAppContext());

    private Whitelist() {}

    // injected calls

    public static boolean shouldShowAds() {
        return isWhitelisted(WhitelistType.ADS);
    }

    public static void setChannelName(String channelName) {
        if (debug) {
            Log.d(TAG, "channel name set to " + channelName);
        }
        VideoInformation.channelName = channelName;

        if (enabledMap.get(WhitelistType.ADS) && adBlockButton != null) {
            adBlockButton.changeEnabled(shouldShowAds());
        }
        if (enabledMap.get(WhitelistType.SPONSORBLOCK) && sbWhitelistButton != null) {
            sbWhitelistButton.changeEnabled(isChannelSBWhitelisted());
        }
    }

    // the rest

    public static boolean isChannelSBWhitelisted() {
        return isWhitelisted(WhitelistType.SPONSORBLOCK);
    }

    private static Map<WhitelistType, ArrayList<ChannelModel>> parseWhitelist(Context context) {
        if (context == null) {
            return Collections.emptyMap();
        }
        WhitelistType[] whitelistTypes = WhitelistType.values();
        Map<WhitelistType, ArrayList<ChannelModel>> whitelistMap = new EnumMap<>(WhitelistType.class);

        for (WhitelistType whitelistType : whitelistTypes) {
            SharedPreferences preferences = VancedUtils.getPreferences(context, whitelistType.getPreferencesName());
            String serializedChannels = preferences.getString("channels", null);
            if (serializedChannels == null) {
                if (debug) {
                    Log.d(TAG, String.format("channels string was null for %s whitelisting", whitelistType));
                }
                whitelistMap.put(whitelistType, new ArrayList<>());
                continue;
            }
            try {
                ArrayList<ChannelModel> deserializedChannels = (ArrayList<ChannelModel>) ObjectSerializer.deserialize(serializedChannels);
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
        if (context == null) {
            return Collections.emptyMap();
        }
        Map<WhitelistType, Boolean> enabledMap = new EnumMap<>(WhitelistType.class);
        for (WhitelistType whitelistType : WhitelistType.values()) {
            enabledMap.put(whitelistType, SharedPrefUtils.getBoolean(context, whitelistType.getSharedPreferencesName(), whitelistType.getPreferenceEnabledName()));
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

    public static boolean addToWhitelist(WhitelistType whitelistType, Context context, ChannelModel channel) {
        ArrayList<ChannelModel> whitelisted = whitelistMap.get(whitelistType);
        for (ChannelModel whitelistedChannel : whitelisted) {
            String channelId = channel.getChannelId();
            if (whitelistedChannel.getChannelId().equals(channelId)) {
                if (debug) {
                    Log.d(TAG, String.format("Tried whitelisting an existing channel again. Old info (%1$s | %2$s) - New info (%3$s | %4$s)",
                            whitelistedChannel.getAuthor(), channelId, channelName, channelId));
                }
                return true;
            }
        }
        whitelisted.add(channel);
        return updateWhitelist(whitelistType, whitelisted, context);
    }

    public static void removeFromWhitelist(WhitelistType whitelistType, Context context, String channelName) {
        ArrayList<ChannelModel> channels = whitelistMap.get(whitelistType);
        Iterator<ChannelModel> iterator = channels.iterator();
        while (iterator.hasNext()) {
            ChannelModel channel = iterator.next();
            if (channel.getAuthor().equals(channelName)) {
                iterator.remove();
                break;
            }
        }
        boolean success = updateWhitelist(whitelistType, channels, context);
        String friendlyName = whitelistType.getFriendlyName();
        if (success) {
            Toast.makeText(context, str("vanced_whitelisting_removed", channelName, friendlyName), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, str("vanced_whitelisting_remove_failed", channelName, friendlyName), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean updateWhitelist(WhitelistType whitelistType, ArrayList<ChannelModel> channels, Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences preferences = getPreferences(context, whitelistType.getPreferencesName());
        SharedPreferences.Editor editor = preferences.edit();

        try {
            editor.putString("channels", ObjectSerializer.serialize(channels));
            editor.apply();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setEnabled(WhitelistType whitelistType, boolean enabled) {
        enabledMap.put(whitelistType, enabled);
    }
}