package app.revanced.integrations.sponsorblock.requests;

import static android.text.Html.fromHtml;
import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.timeWithoutSegments;
import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.videoHasSegments;
import static app.revanced.integrations.sponsorblock.StringRef.str;
import static app.revanced.integrations.utils.ReVancedUtils.runOnMainThread;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.revanced.integrations.whitelist.requests.Requester;
import app.revanced.integrations.whitelist.requests.Route;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.PlayerController;
import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils.VoteOption;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.objects.UserStats;

public class SBRequester {
    private static final String TIME_TEMPLATE = "%.3f";

    private SBRequester() {
    }

    public static synchronized SponsorSegment[] getSegments(String videoId) {
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.GET_SEGMENTS, videoId, SponsorBlockSettings.sponsorBlockUrlCategories);
            int responseCode = connection.getResponseCode();
            runVipCheck();

            if (responseCode == 200) {
                JSONArray responseArray = Requester.getJSONArray(connection);
                int length = responseArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject obj = (JSONObject) responseArray.get(i);
                    JSONArray segment = obj.getJSONArray("segment");
                    long start = (long) (segment.getDouble(0) * 1000);
                    long end = (long) (segment.getDouble(1) * 1000);

                    long minDuration = (long) (SettingsEnum.SB_MIN_DURATION.getFloat() * 1000);
                    if ((end - start) < minDuration)
                        continue;

                    String category = obj.getString("category");
                    String uuid = obj.getString("UUID");
                    boolean locked = obj.getInt("locked") == 1;

                    SponsorBlockSettings.SegmentInfo segmentCategory = SponsorBlockSettings.SegmentInfo.byCategoryKey(category);
                    if (segmentCategory != null && segmentCategory.behaviour.showOnTimeBar) {
                        SponsorSegment sponsorSegment = new SponsorSegment(start, end, segmentCategory, uuid, locked);
                        segments.add(sponsorSegment);
                    }
                }
                if (!segments.isEmpty()) {
                    videoHasSegments = true;
                    timeWithoutSegments = SponsorBlockUtils.getTimeWithoutSegments(segments.toArray(new SponsorSegment[0]));
                }
            }
            connection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return segments.toArray(new SponsorSegment[0]);
    }

    public static void submitSegments(String videoId, String uuid, float startTime, float endTime, String category, Runnable toastRunnable) {
        try {
            String start = String.format(Locale.US, TIME_TEMPLATE, startTime);
            String end = String.format(Locale.US, TIME_TEMPLATE, endTime);
            String duration = String.valueOf(PlayerController.getCurrentVideoLength() / 1000);
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.SUBMIT_SEGMENTS, videoId, uuid, start, end, category, duration);
            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200:
                    SponsorBlockUtils.messageToToast = str("submit_succeeded");
                    break;
                case 409:
                    SponsorBlockUtils.messageToToast = str("submit_failed_duplicate");
                    break;
                case 403:
                    SponsorBlockUtils.messageToToast = str("submit_failed_forbidden", Requester.parseErrorJson(connection));
                    break;
                case 429:
                    SponsorBlockUtils.messageToToast = str("submit_failed_rate_limit");
                    break;
                case 400:
                    SponsorBlockUtils.messageToToast = str("submit_failed_invalid", Requester.parseErrorJson(connection));
                    break;
                default:
                    SponsorBlockUtils.messageToToast = str("submit_failed_unknown_error", responseCode, connection.getResponseMessage());
                    break;
            }
            runOnMainThread(toastRunnable);
            connection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendViewCountRequest(SponsorSegment segment) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.VIEWED_SEGMENT, segment.UUID);
            connection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void voteForSegment(SponsorSegment segment, VoteOption voteOption, Context context, String... args) {
        new Thread(() -> {
            try {
                String segmentUuid = segment.UUID;
                String uuid = SettingsEnum.SB_UUID.getString();
                String vote = Integer.toString(voteOption == VoteOption.UPVOTE ? 1 : 0);

                runOnMainThread(() -> Toast.makeText(context, str("vote_started"), Toast.LENGTH_SHORT).show());

                HttpURLConnection connection = voteOption == VoteOption.CATEGORY_CHANGE
                        ? getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_CATEGORY, segmentUuid, uuid, args[0])
                        : getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_QUALITY, segmentUuid, uuid, vote);
                int responseCode = connection.getResponseCode();

                switch (responseCode) {
                    case 200:
                        SponsorBlockUtils.messageToToast = str("vote_succeeded");
                        break;
                    case 403:
                        SponsorBlockUtils.messageToToast = str("vote_failed_forbidden", Requester.parseErrorJson(connection));
                        break;
                    default:
                        SponsorBlockUtils.messageToToast = str("vote_failed_unknown_error", responseCode, connection.getResponseMessage());
                        break;
                }
                runOnMainThread(() -> Toast.makeText(context, SponsorBlockUtils.messageToToast, Toast.LENGTH_LONG).show());
                connection.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void retrieveUserStats(PreferenceCategory category, Preference loadingPreference) {
        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            loadingPreference.setTitle(str("stats_sb_disabled"));
            return;
        }

        new Thread(() -> {
            try {
                JSONObject json = getJSONObject(SBRoutes.GET_USER_STATS, SettingsEnum.SB_UUID.getString());
                UserStats stats = new UserStats(json.getString("userName"), json.getDouble("minutesSaved"), json.getInt("segmentCount"),
                        json.getInt("viewCount"));
                SponsorBlockUtils.addUserStats(category, loadingPreference, stats);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void setUsername(String username, EditTextPreference preference, Runnable toastRunnable) {
        new Thread(() -> {
            try {
                HttpURLConnection connection = getConnectionFromRoute(SBRoutes.CHANGE_USERNAME, SettingsEnum.SB_UUID.getString(), username);
                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    SponsorBlockUtils.messageToToast = str("stats_username_changed");
                    runOnMainThread(() -> {
                        preference.setTitle(fromHtml(str("stats_username", username)));
                        preference.setText(username);
                    });
                } else {
                    SponsorBlockUtils.messageToToast = str("stats_username_change_unknown_error", responseCode, connection.getResponseMessage());
                }
                runOnMainThread(toastRunnable);
                connection.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void runVipCheck() {
        long now = System.currentTimeMillis();
        if (now < (SettingsEnum.SB_LAST_VIP_CHECK.getLong() + TimeUnit.DAYS.toMillis(3))) {
            return;
        }
        try {
            JSONObject json = getJSONObject(SBRoutes.IS_USER_VIP, SettingsEnum.SB_UUID.getString());
            boolean vip = json.getBoolean("vip");
            SettingsEnum.SB_IS_VIP.saveValue(vip);
            SettingsEnum.SB_LAST_VIP_CHECK.saveValue(now);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(SettingsEnum.SB_API_URL.getString(), route, params);
    }

    private static JSONObject getJSONObject(Route route, String... params) throws Exception {
        return Requester.getJSONObject(getConnectionFromRoute(route, params));
    }
}
