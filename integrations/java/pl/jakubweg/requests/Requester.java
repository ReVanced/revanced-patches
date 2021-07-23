package pl.jakubweg.requests;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pl.jakubweg.SponsorBlockSettings;
import pl.jakubweg.SponsorBlockUtils;
import pl.jakubweg.SponsorBlockUtils.VoteOption;
import pl.jakubweg.objects.SponsorSegment;
import pl.jakubweg.objects.UserStats;

import static pl.jakubweg.StringRef.str;

public class Requester {
    private static final String SPONSORBLOCK_API_URL = "https://sponsor.ajay.app/api/";
    private static final String TIME_TEMPLATE = "%.3f";

    private Requester() {}

    public static synchronized SponsorSegment[] getSegments(String videoId) {
        List<SponsorSegment> segments = new ArrayList<>();
        try {
            HttpURLConnection connection = getConnectionFromRoute(Route.GET_SEGMENTS, videoId, SponsorBlockSettings.sponsorBlockUrlCategories);
            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200:
                    JSONArray responseArray = new JSONArray(parseJson(connection));
                    int length = responseArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject obj = ((JSONObject) responseArray.get(i));
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
                    break;
                case 404:
                    break;
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return segments.toArray(new SponsorSegment[0]);
    }

    public static void submitSegments(String videoId, String uuid, float startTime, float endTime, String category, Runnable toastRunnable) {
        try {
            String start = String.format(TIME_TEMPLATE, startTime);
            String end = String.format(TIME_TEMPLATE, endTime);
            HttpURLConnection connection = getConnectionFromRoute(Route.SUBMIT_SEGMENTS, videoId, uuid, start, end, category);
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
            HttpURLConnection connection = getConnectionFromRoute(Route.VIEWED_SEGMENT, segment.UUID);
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
                    ? getConnectionFromRoute(Route.VOTE_ON_SEGMENT_CATEGORY, segmentUuid, uuid, args[0])
                    : getConnectionFromRoute(Route.VOTE_ON_SEGMENT_QUALITY, segmentUuid, uuid, vote);
            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200:
                    SponsorBlockUtils.messageToToast = str("vote_succeeded");
                    break;
                case 403:
                    SponsorBlockUtils.messageToToast = str("vote_failed_forbidden");
                    break;
                case 429:
                    SponsorBlockUtils.messageToToast = str("vote_failed_rate_limit");
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
                HttpURLConnection connection = getConnectionFromRoute(Route.GET_USER_STATS, SponsorBlockSettings.uuid);
                JSONObject json = new JSONObject(parseJson(connection));
                connection.disconnect();
                UserStats stats = new UserStats(json.getString("userName"), json.getDouble("minutesSaved"), json.getInt("segmentCount"),
                        json.getInt("viewCount"));
                SponsorBlockUtils.addUserStats(category, loadingPreference, stats);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void setUsername(String username) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(Route.CHANGE_USERNAME, SponsorBlockSettings.uuid, username);
            connection.disconnect();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        String url = SPONSORBLOCK_API_URL + route.compile(params).getCompiledRoute();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(route.getMethod().name());
        return connection;
    }

    private static String parseJson(HttpURLConnection connection) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        inputStream.close();
        return jsonBuilder.toString();
    }
}