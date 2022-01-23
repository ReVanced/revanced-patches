package pl.jakubweg.requests;

import static pl.jakubweg.SponsorBlockUtils.timeWithoutSegments;
import static pl.jakubweg.SponsorBlockUtils.videoHasSegments;
import static pl.jakubweg.StringRef.str;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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

import fi.vanced.utils.requests.Requester;
import fi.vanced.utils.requests.Route;
import pl.jakubweg.SponsorBlockSettings;
import pl.jakubweg.SponsorBlockUtils;
import pl.jakubweg.SponsorBlockUtils.VoteOption;
import pl.jakubweg.objects.SponsorSegment;
import pl.jakubweg.objects.UserStats;

public class SBRequester {
    private static final String SPONSORBLOCK_API_URL = "https://sponsor.ajay.app/api/";
    private static final String TIME_TEMPLATE = "%.3f";

    private SBRequester() {}

    public static synchronized SponsorSegment[] getSegments(String videoId) {
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.GET_SEGMENTS, videoId, SponsorBlockSettings.sponsorBlockUrlCategories);
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                JSONArray responseArray = Requester.getJSONArray(connection);
                int length = responseArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject obj = (JSONObject) responseArray.get(i);
                    JSONArray segment = obj.getJSONArray("segment");
                    long start = (long) (segment.getDouble(0) * 1000);
                    long end = (long) (segment.getDouble(1) * 1000);
                    String category = obj.getString("category");
                    String uuid = obj.getString("UUID");

                    SponsorBlockSettings.SegmentInfo segmentCategory = SponsorBlockSettings.SegmentInfo.byCategoryKey(category);
                    if (segmentCategory != null && segmentCategory.behaviour.showOnTimeBar) {
                        SponsorSegment sponsorSegment = new SponsorSegment(start, end, segmentCategory, uuid);
                        segments.add(sponsorSegment);
                    }
                }
                videoHasSegments = true;
                timeWithoutSegments = SponsorBlockUtils.getTimeWithoutSegments(segments.toArray(new SponsorSegment[0]));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return segments.toArray(new SponsorSegment[0]);
    }

    public static void submitSegments(String videoId, String uuid, float startTime, float endTime, String category, Runnable toastRunnable) {
        try {
            String start = String.format(Locale.US, TIME_TEMPLATE, startTime);
            String end = String.format(Locale.US, TIME_TEMPLATE, endTime);
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.SUBMIT_SEGMENTS, videoId, uuid, start, end, category);
            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200:
                    SponsorBlockUtils.messageToToast = str("submit_succeeded");
                    break;
                case 409:
                    SponsorBlockUtils.messageToToast = str("submit_failed_duplicate");
                    break;
                case 403:
                    SponsorBlockUtils.messageToToast = str("submit_failed_forbidden");
                    break;
                case 429:
                    SponsorBlockUtils.messageToToast = str("submit_failed_rate_limit");
                    break;
                default:
                    SponsorBlockUtils.messageToToast = str("submit_failed_unknown_error", responseCode, connection.getResponseMessage());
                    break;
            }
            new Handler(Looper.getMainLooper()).post(toastRunnable);
            connection.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendViewCountRequest(SponsorSegment segment) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.VIEWED_SEGMENT, segment.UUID);
            connection.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void voteForSegment(SponsorSegment segment, VoteOption voteOption, Context context, Runnable toastRunnable, String... args) {
        try {
            String segmentUuid = segment.UUID;
            String uuid = SponsorBlockSettings.uuid;
            String vote = Integer.toString(voteOption == VoteOption.UPVOTE ? 1 : 0);

            Toast.makeText(context, str("vote_started"), Toast.LENGTH_SHORT).show();

            HttpURLConnection connection = voteOption == VoteOption.CATEGORY_CHANGE
                    ? getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_CATEGORY, segmentUuid, uuid, args[0])
                    : getConnectionFromRoute(SBRoutes.VOTE_ON_SEGMENT_QUALITY, segmentUuid, uuid, vote);
            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200:
                    SponsorBlockUtils.messageToToast = str("vote_succeeded");
                    break;
                case 403:
                    SponsorBlockUtils.messageToToast = str("vote_failed_forbidden");
                    break;
                default:
                    SponsorBlockUtils.messageToToast = str("vote_failed_unknown_error", responseCode, connection.getResponseMessage());
                    break;
            }
            new Handler(Looper.getMainLooper()).post(toastRunnable);
            connection.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void retrieveUserStats(PreferenceCategory category, Preference loadingPreference) {
        if (!SponsorBlockSettings.isSponsorBlockEnabled) {
            loadingPreference.setTitle(str("stats_sb_disabled"));
            return;
        }

        new Thread(() -> {
            try {
                JSONObject json = getJSONObject(SBRoutes.GET_USER_STATS, SponsorBlockSettings.uuid);
                UserStats stats = new UserStats(json.getString("userName"), json.getDouble("minutesSaved"), json.getInt("segmentCount"),
                        json.getInt("viewCount"));
                SponsorBlockUtils.addUserStats(category, loadingPreference, stats);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void setUsername(String username, Runnable toastRunnable) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(SBRoutes.CHANGE_USERNAME, SponsorBlockSettings.uuid, username);
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                SponsorBlockUtils.messageToToast = str("stats_username_changed");
            }
            else {
                SponsorBlockUtils.messageToToast = str("stats_username_change_unknown_error", responseCode, connection.getResponseMessage());
            }
            new Handler(Looper.getMainLooper()).post(toastRunnable);
            connection.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(SPONSORBLOCK_API_URL, route, params);
    }

    private static JSONObject getJSONObject(Route route, String... params) throws Exception {
        return Requester.getJSONObject(getConnectionFromRoute(route, params));
    }
}