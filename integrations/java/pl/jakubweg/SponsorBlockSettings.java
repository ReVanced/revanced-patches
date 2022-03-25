package pl.jakubweg;

import static pl.jakubweg.StringRef.sf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SponsorBlockSettings {

    public static final String PREFERENCES_NAME = "sponsor-block";
    public static final String PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP = "show-toast";
    public static final String PREFERENCES_KEY_COUNT_SKIPS = "count-skips";
    public static final String PREFERENCES_KEY_UUID = "uuid";
    public static final String PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP = "new-segment-step-accuracy";
    public static final String PREFERENCES_KEY_MIN_DURATION = "sb-min-duration";
    public static final String PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED = "sb-enabled";
    public static final String PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN = "sb_hint_shown";
    public static final String PREFERENCES_KEY_SEEN_GUIDELINES = "sb-seen-gl";
    public static final String PREFERENCES_KEY_NEW_SEGMENT_ENABLED = "sb-new-segment-enabled";
    public static final String PREFERENCES_KEY_VOTING_ENABLED = "sb-voting-enabled";
    public static final String PREFERENCES_KEY_SKIPPED_SEGMENTS = "sb-skipped-segments";
    public static final String PREFERENCES_KEY_SKIPPED_SEGMENTS_TIME = "sb-skipped-segments-time";
    public static final String PREFERENCES_KEY_SHOW_TIME_WITHOUT_SEGMENTS = "sb-length-without-segments";
    public static final String PREFERENCES_KEY_CATEGORY_COLOR_SUFFIX = "_color";
    public static final String PREFERENCES_KEY_BROWSER_BUTTON = "sb-browser-button";
    public static final String PREFERENCES_KEY_IS_VIP = "sb-is-vip";
    public static final String PREFERENCES_KEY_LAST_VIP_CHECK = "sb-last-vip-check";
    public static final String PREFERENCES_KEY_API_URL = "sb-api-url";

    public static final SegmentBehaviour DefaultBehaviour = SegmentBehaviour.IGNORE;
    public static final String DEFAULT_SERVER_URL = "https://sponsor.ajay.app";
    public static final String DEFAULT_API_URL = DEFAULT_SERVER_URL + "/api/";

    public static boolean isSponsorBlockEnabled = false;
    public static boolean seenGuidelinesPopup = false;
    public static boolean isAddNewSegmentEnabled = false;
    public static boolean isVotingEnabled = true;
    public static boolean showToastWhenSkippedAutomatically = true;
    public static boolean countSkips = true;
    public static boolean showTimeWithoutSegments = true;
    public static boolean vip = false;
    public static long lastVipCheck = 0;
    public static int adjustNewSegmentMillis = 150;
    public static float minDuration = 0f;
    public static String uuid = "<invalid>";
    public static String apiUrl = DEFAULT_API_URL;
    public static String sponsorBlockUrlCategories = "[]";
    public static int skippedSegments;
    public static long skippedTime;

    @SuppressWarnings("unused")
    @Deprecated
    public SponsorBlockSettings(Context ignored) {
        Log.e("jakubweg.Settings", "Do not call SponsorBlockSettings constructor!");
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void setSeenGuidelines(Context context) {
        SponsorBlockSettings.seenGuidelinesPopup = true;
        getPreferences(context).edit().putBoolean(PREFERENCES_KEY_SEEN_GUIDELINES, true).apply();
    }

    public static void update(Context context) {
        if (context == null) return;

        SharedPreferences preferences = getPreferences(context);
        isSponsorBlockEnabled = preferences.getBoolean(PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED, isSponsorBlockEnabled);
        seenGuidelinesPopup = preferences.getBoolean(PREFERENCES_KEY_SEEN_GUIDELINES, seenGuidelinesPopup);

        if (!isSponsorBlockEnabled) {
            SkipSegmentView.hide();
            NewSegmentHelperLayout.hide();
            SponsorBlockUtils.hideShieldButton();
            SponsorBlockUtils.hideVoteButton();
            PlayerController.sponsorSegmentsOfCurrentVideo = null;
        } else { /*isAddNewSegmentEnabled*/
            SponsorBlockUtils.showShieldButton();
        }

        isAddNewSegmentEnabled = preferences.getBoolean(PREFERENCES_KEY_NEW_SEGMENT_ENABLED, isAddNewSegmentEnabled);
        if (!isAddNewSegmentEnabled) {
            NewSegmentHelperLayout.hide();
            SponsorBlockUtils.hideShieldButton();
        } else {
            SponsorBlockUtils.showShieldButton();
        }

        isVotingEnabled = preferences.getBoolean(PREFERENCES_KEY_VOTING_ENABLED, isVotingEnabled);
        if (!isVotingEnabled)
            SponsorBlockUtils.hideVoteButton();
        else
            SponsorBlockUtils.showVoteButton();

        SegmentBehaviour[] possibleBehaviours = SegmentBehaviour.values();
        final ArrayList<String> enabledCategories = new ArrayList<>(possibleBehaviours.length);
        for (SegmentInfo segment : SegmentInfo.values()) {
            String categoryColor = preferences.getString(segment.key + PREFERENCES_KEY_CATEGORY_COLOR_SUFFIX, SponsorBlockUtils.formatColorString(segment.defaultColor));
            segment.setColor(Color.parseColor(categoryColor));

            SegmentBehaviour behaviour = null;
            String value = preferences.getString(segment.key, null);
            if (value != null) {
                for (SegmentBehaviour possibleBehaviour : possibleBehaviours) {
                    if (possibleBehaviour.key.equals(value)) {
                        behaviour = possibleBehaviour;
                        break;
                    }
                }
            }
            if (behaviour != null) {
                segment.behaviour = behaviour;
            }
            else {
                behaviour = segment.behaviour;
            }

            if (behaviour.showOnTimeBar && segment != SegmentInfo.UNSUBMITTED)
                enabledCategories.add(segment.key);
        }

        //"[%22sponsor%22,%22outro%22,%22music_offtopic%22,%22intro%22,%22selfpromo%22,%22interaction%22,%22preview%22]";
        if (enabledCategories.isEmpty())
            sponsorBlockUrlCategories = "[]";
        else
            sponsorBlockUrlCategories = "[%22" + TextUtils.join("%22,%22", enabledCategories) + "%22]";

        skippedSegments = preferences.getInt(PREFERENCES_KEY_SKIPPED_SEGMENTS, skippedSegments);
        skippedTime = preferences.getLong(PREFERENCES_KEY_SKIPPED_SEGMENTS_TIME, skippedTime);

        showToastWhenSkippedAutomatically = preferences.getBoolean(PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP, showToastWhenSkippedAutomatically);
        String tmp1 = preferences.getString(PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP, null);
        if (tmp1 != null)
            adjustNewSegmentMillis = Integer.parseInt(tmp1);

        String minTmp = preferences.getString(PREFERENCES_KEY_MIN_DURATION, null);
        if (minTmp != null)
            minDuration = Float.parseFloat(minTmp);

        countSkips = preferences.getBoolean(PREFERENCES_KEY_COUNT_SKIPS, countSkips);
        showTimeWithoutSegments = preferences.getBoolean(PREFERENCES_KEY_SHOW_TIME_WITHOUT_SEGMENTS, showTimeWithoutSegments);
        vip = preferences.getBoolean(PREFERENCES_KEY_IS_VIP, false);

        String vipCheckTmp = preferences.getString(PREFERENCES_KEY_LAST_VIP_CHECK, null);
        if (vipCheckTmp != null)
            lastVipCheck = Long.parseLong(vipCheckTmp);

        apiUrl = preferences.getString(PREFERENCES_KEY_API_URL, DEFAULT_API_URL);

        uuid = preferences.getString(PREFERENCES_KEY_UUID, null);
        if (uuid == null) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            preferences.edit().putString(PREFERENCES_KEY_UUID, uuid).apply();
        }
    }

    public enum SegmentBehaviour {
        SKIP_AUTOMATICALLY("skip", 2, sf("skip_automatically"), true, true),
        MANUAL_SKIP("manual-skip", 1, sf("skip_showbutton"), false, true),
        IGNORE("ignore", -1, sf("skip_ignore"), false, false);

        public final String key;
        public final int desktopKey;
        public final StringRef name;
        public final boolean skip;
        public final boolean showOnTimeBar;

        SegmentBehaviour(String key,
                         int desktopKey,
                         StringRef name,
                         boolean skip,
                         boolean showOnTimeBar) {
            this.key = key;
            this.desktopKey = desktopKey;
            this.name = name;
            this.skip = skip;
            this.showOnTimeBar = showOnTimeBar;
        }

        public static SegmentBehaviour byDesktopKey(int desktopKey) {
            for (SegmentBehaviour behaviour : values()) {
                if (behaviour.desktopKey == desktopKey) {
                    return behaviour;
                }
            }
            return null;
        }
    }

    public enum SegmentInfo {
        SPONSOR("sponsor", sf("segments_sponsor"), sf("skipped_sponsor"), sf("segments_sponsor_sum"), SegmentBehaviour.SKIP_AUTOMATICALLY, 0xFF00d400),
        INTRO("intro", sf("segments_intermission"), sf("skipped_intermission"), sf("segments_intermission_sum"), DefaultBehaviour, 0xFF00ffff),
        OUTRO("outro", sf("segments_endcards"), sf("skipped_endcard"), sf("segments_endcards_sum"), DefaultBehaviour, 0xFF0202ed),
        INTERACTION("interaction", sf("segments_subscribe"), sf("skipped_subscribe"), sf("segments_subscribe_sum"), DefaultBehaviour, 0xFFcc00ff),
        SELF_PROMO("selfpromo", sf("segments_selfpromo"), sf("skipped_selfpromo"), sf("segments_selfpromo_sum"), DefaultBehaviour, 0xFFffff00),
        MUSIC_OFFTOPIC("music_offtopic", sf("segments_nomusic"), sf("skipped_nomusic"), sf("segments_nomusic_sum"), DefaultBehaviour, 0xFFff9900),
        PREVIEW("preview", sf("segments_preview"), sf("skipped_preview"), sf("segments_preview_sum"), DefaultBehaviour, 0xFF008fd6),
        FILLER("filler", sf("segments_filler"), sf("skipped_filler"), sf("segments_filler_sum"), SegmentBehaviour.IGNORE, 0xFF7300FF),
        UNSUBMITTED("unsubmitted", StringRef.empty, sf("skipped_unsubmitted"), StringRef.empty, SegmentBehaviour.SKIP_AUTOMATICALLY, 0xFFFFFFFF);

        private static final SegmentInfo[] mValuesWithoutUnsubmitted = new SegmentInfo[]{
                SPONSOR,
                INTRO,
                OUTRO,
                INTERACTION,
                SELF_PROMO,
                MUSIC_OFFTOPIC,
                PREVIEW,
                FILLER
        };
        private static final Map<String, SegmentInfo> mValuesMap = new HashMap<>(values().length);

        static {
            for (SegmentInfo value : valuesWithoutUnsubmitted())
                mValuesMap.put(value.key, value);
        }

        public final String key;
        public final StringRef title;
        public final StringRef skipMessage;
        public final StringRef description;
        public final Paint paint;
        public final int defaultColor;
        public int color;
        public SegmentBehaviour behaviour;

        SegmentInfo(String key,
                    StringRef title,
                    StringRef skipMessage,
                    StringRef description,
                    SegmentBehaviour behaviour,
                    int defaultColor) {

            this.key = key;
            this.title = title;
            this.skipMessage = skipMessage;
            this.description = description;
            this.behaviour = behaviour;
            this.defaultColor = defaultColor;
            this.color = defaultColor;
            this.paint = new Paint();
        }

        public static SegmentInfo[] valuesWithoutUnsubmitted() {
            return mValuesWithoutUnsubmitted;
        }

        public static SegmentInfo byCategoryKey(String key) {
            return mValuesMap.get(key);
        }

        public void setColor(int color) {
            color = color & 0xFFFFFF;
            this.color = color;
            paint.setColor(color);
            paint.setAlpha(255);
        }

        public CharSequence getTitleWithDot() {
            return Html.fromHtml(String.format("<font color=\"#%06X\">⬤</font> %s", color, title));
        }
    }
}
