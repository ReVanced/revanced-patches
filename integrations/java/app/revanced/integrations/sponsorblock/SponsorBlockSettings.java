package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.StringRef.sf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.SharedPrefHelper;

public class SponsorBlockSettings {

    public static final String CATEGORY_COLOR_SUFFIX = "_color";
    public static final SegmentBehaviour DefaultBehaviour = SegmentBehaviour.IGNORE;
    public static String sponsorBlockUrlCategories = "[]";

    public static void update(Context context) {
        if (context == null) return;

        SharedPreferences preferences = SharedPrefHelper.getPreferences(context, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK);

        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            SkipSegmentView.hide();
            NewSegmentHelperLayout.hide();
            SponsorBlockUtils.hideShieldButton();
            SponsorBlockUtils.hideVoteButton();
            PlayerController.sponsorSegmentsOfCurrentVideo = null;
        } else { /*isAddNewSegmentEnabled*/
            SponsorBlockUtils.showShieldButton();
        }

        if (!SettingsEnum.SB_NEW_SEGMENT_ENABLED.getBoolean()) {
            NewSegmentHelperLayout.hide();
            SponsorBlockUtils.hideShieldButton();
        } else {
            SponsorBlockUtils.showShieldButton();
        }


        if (!SettingsEnum.SB_VOTING_ENABLED.getBoolean())
            SponsorBlockUtils.hideVoteButton();
        else
            SponsorBlockUtils.showVoteButton();

        SegmentBehaviour[] possibleBehaviours = SegmentBehaviour.values();
        final ArrayList<String> enabledCategories = new ArrayList<>(possibleBehaviours.length);
        for (SegmentInfo segment : SegmentInfo.values()) {
            String categoryColor = preferences.getString(segment.key + CATEGORY_COLOR_SUFFIX, SponsorBlockUtils.formatColorString(segment.defaultColor));
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
            } else {
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

        String uuid = SettingsEnum.SB_UUID.getString();
        if (uuid == null) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            SettingsEnum.SB_UUID.saveValue(uuid);
        }
    }

    public enum SegmentBehaviour {
        SKIP_AUTOMATICALLY_ONCE("skip-once", 3, sf("skip_automatically_once"), true, true),
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
        INTRO("intro", sf("segments_intermission"), sf("skipped_intermission"), sf("segments_intermission_sum"), SegmentBehaviour.MANUAL_SKIP, 0xFF00ffff),
        OUTRO("outro", sf("segments_endcards"), sf("skipped_endcard"), sf("segments_endcards_sum"), SegmentBehaviour.MANUAL_SKIP, 0xFF0202ed),
        INTERACTION("interaction", sf("segments_subscribe"), sf("skipped_subscribe"), sf("segments_subscribe_sum"), SegmentBehaviour.SKIP_AUTOMATICALLY, 0xFFcc00ff),
        SELF_PROMO("selfpromo", sf("segments_selfpromo"), sf("skipped_selfpromo"), sf("segments_selfpromo_sum"), SegmentBehaviour.SKIP_AUTOMATICALLY, 0xFFffff00),
        MUSIC_OFFTOPIC("music_offtopic", sf("segments_nomusic"), sf("skipped_nomusic"), sf("segments_nomusic_sum"), SegmentBehaviour.MANUAL_SKIP, 0xFFff9900),
        PREVIEW("preview", sf("segments_preview"), sf("skipped_preview"), sf("segments_preview_sum"), DefaultBehaviour, 0xFF008fd6),
        FILLER("filler", sf("segments_filler"), sf("skipped_filler"), sf("segments_filler_sum"), DefaultBehaviour, 0xFF7300FF),
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
