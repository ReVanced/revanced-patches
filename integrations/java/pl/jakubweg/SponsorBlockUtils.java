package pl.jakubweg;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import fi.razerman.youtube.Helpers.XSwipeHelper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static fi.razerman.youtube.XGlobals.debug;
import static pl.jakubweg.PlayerController.VERBOSE;
import static pl.jakubweg.PlayerController.getCurrentVideoId;
import static pl.jakubweg.PlayerController.getLastKnownVideoTime;
import static pl.jakubweg.PlayerController.sponsorSegmentsOfCurrentVideo;
import static pl.jakubweg.SponsorBlockSettings.getSponsorBlockVoteUrl;
import static pl.jakubweg.SponsorBlockSettings.sponsorBlockSkipSegmentsUrl;
import static pl.jakubweg.SponsorBlockSettings.uuid;
import static pl.jakubweg.StringRef.str;

@SuppressWarnings({"LongLogTag"})
public abstract class SponsorBlockUtils {
    public static final String TAG = "jakubweg.SponsorBlockUtils";
    public static final String DATE_FORMAT = "HH:mm:ss.SSS";
    public static final String WITHOUT_SEGMENTS_FORMAT = " (m:ss)";
    public static final String WITHOUT_SEGMENTS_FORMAT_H = " (H:m:ss)";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
    public static final SimpleDateFormat withoutSegmentsFormatter = new SimpleDateFormat(WITHOUT_SEGMENTS_FORMAT);
    public static final SimpleDateFormat withoutSegmentsFormatterH = new SimpleDateFormat(WITHOUT_SEGMENTS_FORMAT_H);
    private static final int sponsorBtnId = 1234;
    public static final View.OnClickListener sponsorBlockBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (debug) {
                Log.d(TAG, "Shield button clicked");
            }
            NewSegmentHelperLayout.toggle();
        }
    };
    public static final View.OnClickListener voteButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (debug) {
                Log.d(TAG, "Vote button clicked");
            }
            SponsorBlockUtils.onVotingClicked(v.getContext());
        }
    };
    private static int shareBtnId = -1;
    private static long newSponsorSegmentDialogShownMillis;
    private static long newSponsorSegmentStartMillis = -1;
    private static long newSponsorSegmentEndMillis = -1;
    private static final DialogInterface.OnClickListener newSponsorSegmentDialogListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Context context = ((AlertDialog) dialog).getContext();
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    // start
                    newSponsorSegmentStartMillis = newSponsorSegmentDialogShownMillis;
                    Toast.makeText(context.getApplicationContext(), str("new_segment_time_start_set"), Toast.LENGTH_LONG).show();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    // end
                    newSponsorSegmentEndMillis = newSponsorSegmentDialogShownMillis;
                    Toast.makeText(context.getApplicationContext(), str("new_segment_time_end_set"), Toast.LENGTH_SHORT).show();
                    break;
            }
            dialog.dismiss();
        }
    };
    private static SponsorBlockSettings.SegmentInfo newSponsorBlockSegmentType;
    private static final DialogInterface.OnClickListener segmentTypeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SponsorBlockSettings.SegmentInfo segmentType = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted()[which];
            boolean enableButton;
            if (!segmentType.behaviour.showOnTimeBar) {
                Toast.makeText(
                        ((AlertDialog) dialog).getContext().getApplicationContext(),
                        str("new_segment_disabled_category"),
                        Toast.LENGTH_SHORT).show();
                enableButton = false;
            } else {
                Toast.makeText(
                        ((AlertDialog) dialog).getContext().getApplicationContext(),
                        segmentType.description.toString(),
                        Toast.LENGTH_SHORT).show();
                newSponsorBlockSegmentType = segmentType;
                enableButton = true;
            }

            ((AlertDialog) dialog)
                    .getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(enableButton);
        }
    };
    private static final DialogInterface.OnClickListener segmentReadyDialogButtonListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            NewSegmentHelperLayout.hide();
            Context context = ((AlertDialog) dialog).getContext();
            dialog.dismiss();

            SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
            CharSequence[] titles = new CharSequence[values.length];
            for (int i = 0; i < values.length; i++) {
//                titles[i] = values[i].title;
                titles[i] = values[i].getTitleWithDot();
            }

            newSponsorBlockSegmentType = null;
            new AlertDialog.Builder(context)
                    .setTitle(str("new_segment_choose_category"))
                    .setSingleChoiceItems(titles, -1, segmentTypeListener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, segmentCategorySelectedDialogListener)
                    .show()
                    .getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }
    };
    private static WeakReference<Context> appContext = new WeakReference<>(null);
    private static final DialogInterface.OnClickListener segmentCategorySelectedDialogListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            Context context = ((AlertDialog) dialog).getContext().getApplicationContext();
            Toast.makeText(context, str("submit_started"), Toast.LENGTH_SHORT).show();

            appContext = new WeakReference<>(context);
            new Thread(submitRunnable).start();
        }
    };
    private static String messageToToast = "";
    private static EditByHandSaveDialogListener editByHandSaveDialogListener = new EditByHandSaveDialogListener();
    private static final DialogInterface.OnClickListener editByHandDialogListener = new DialogInterface.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Context context = ((AlertDialog) dialog).getContext();

            final boolean isStart = DialogInterface.BUTTON_NEGATIVE == which;

            final EditText textView = new EditText(context);
            textView.setHint(DATE_FORMAT);
            if (isStart) {
                if (newSponsorSegmentStartMillis >= 0)
                    textView.setText(dateFormatter.format(new Date(newSponsorSegmentStartMillis)));
            } else {
                if (newSponsorSegmentEndMillis >= 0)
                    textView.setText(dateFormatter.format(new Date(newSponsorSegmentEndMillis)));
            }

            editByHandSaveDialogListener.settingStart = isStart;
            editByHandSaveDialogListener.editText = new WeakReference<>(textView);
            new AlertDialog.Builder(context)
                    .setTitle(str(isStart ? "new_segment_time_start" : "new_segment_time_end"))
                    .setView(textView)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(str("new_segment_now"), editByHandSaveDialogListener)
                    .setPositiveButton(android.R.string.ok, editByHandSaveDialogListener)
                    .show();

            dialog.dismiss();
        }
    };
    private static final DialogInterface.OnClickListener segmentVoteClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final Context context = ((AlertDialog) dialog).getContext();
            final SponsorSegment segment = sponsorSegmentsOfCurrentVideo[which];

            final VoteOption[] voteOptions = VoteOption.values();
            String[] items = new String[voteOptions.length];

            for (int i = 0; i < voteOptions.length; i++) {
                items[i] = voteOptions[i].title;
            }

            new AlertDialog.Builder(context)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            appContext = new WeakReference<>(context.getApplicationContext());
                            switch (voteOptions[which]) {
                                case UPVOTE:
                                    voteForSegment(segment, VoteOption.UPVOTE);
                                    break;
                                case DOWNVOTE:
                                    voteForSegment(segment, VoteOption.DOWNVOTE);
                                    break;
                                case CATEGORY_CHANGE:
                                    onNewCategorySelect(segment, context);
                                    break;
                            }
                        }
                    })
                    .show();
        }
    };
    private static Runnable toastRunnable = new Runnable() {
        @Override
        public void run() {
            Context context = appContext.get();
            if (context != null && messageToToast != null)
                Toast.makeText(context, messageToToast, Toast.LENGTH_LONG).show();
        }
    };
    private static final Runnable submitRunnable = new Runnable() {
        @Override
        public void run() {
            messageToToast = null;
            final String uuid = SponsorBlockSettings.uuid;
            final long start = newSponsorSegmentStartMillis;
            final long end = newSponsorSegmentEndMillis;
            final String videoId = getCurrentVideoId();
            final SponsorBlockSettings.SegmentInfo segmentType = SponsorBlockUtils.newSponsorBlockSegmentType;
            try {

                if (start < 0 || end < 0 || start >= end || segmentType == null || videoId == null || uuid == null) {
                    Log.e(TAG, "Unable to submit times, invalid parameters");
                    return;
                }

                URL url = new URL(String.format(Locale.US,
                        sponsorBlockSkipSegmentsUrl + "?videoID=%s&userID=%s&startTime=%.3f&endTime=%.3f&category=%s",
                        videoId, uuid, ((float) start) / 1000f, ((float) end) / 1000f, segmentType.key));

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                switch (connection.getResponseCode()) {
                    default:
                        messageToToast = String.format(str("submit_failed_unknown_error"), connection.getResponseCode(), connection.getResponseMessage());
                        break;
                    case 429:
                        messageToToast = str("submit_failed_rate_limit");
                        break;
                    case 403:
                        messageToToast = str("submit_failed_forbidden");
                        break;
                    case 409:
                        messageToToast = str("submit_failed_duplicate");
                        break;
                    case 200:
                        messageToToast = str("submit_succeeded");
                        break;
                }

                Log.i(TAG, "Segment submitted with status: " + connection.getResponseCode() + ", " + messageToToast);
                new Handler(Looper.getMainLooper()).post(toastRunnable);

                connection.disconnect();

                newSponsorSegmentEndMillis = newSponsorSegmentStartMillis = -1;
            } catch (Exception e) {
                Log.e(TAG, "Unable to submit segment", e);
            }

            if (videoId != null)
                PlayerController.executeDownloadSegments(videoId);
        }
    };

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private SponsorBlockUtils() {
    }

    public static void showShieldButton() {
        View i = ShieldButton._shieldBtn.get();
        if (i == null || !ShieldButton.shouldBeShown()) return;
        i.setVisibility(VISIBLE);
        i.bringToFront();
        i.requestLayout();
        i.invalidate();
    }

    public static void hideShieldButton() {
        View i = ShieldButton._shieldBtn.get();
        if (i != null)
            i.setVisibility(GONE);
    }

    public static void showVoteButton() {
        View i = VotingButton._votingButton.get();
        if (i == null || !VotingButton.shouldBeShown()) return;
        i.setVisibility(VISIBLE);
        i.bringToFront();
        i.requestLayout();
        i.invalidate();
    }

    public static void hideVoteButton() {
        View i = VotingButton._votingButton.get();
        if (i != null)
            i.setVisibility(GONE);
    }

    @SuppressLint("DefaultLocale")
    public static void onMarkLocationClicked(Context context) {
        newSponsorSegmentDialogShownMillis = PlayerController.getLastKnownVideoTime();

        new AlertDialog.Builder(context)
                .setTitle(str("new_segment_title"))
                .setMessage(String.format(str("new_segment_mark_time_as_question"),
                        newSponsorSegmentDialogShownMillis / 60000,
                        newSponsorSegmentDialogShownMillis / 1000 % 60,
                        newSponsorSegmentDialogShownMillis % 1000))
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(str("new_segment_mark_start"), newSponsorSegmentDialogListener)
                .setPositiveButton(str("new_segment_mark_end"), newSponsorSegmentDialogListener)
                .show();
    }

    @SuppressLint("DefaultLocale")
    public static void onPublishClicked(Context context) {
        if (newSponsorSegmentStartMillis >= 0 && newSponsorSegmentStartMillis < newSponsorSegmentEndMillis) {
            long length = (newSponsorSegmentEndMillis - newSponsorSegmentStartMillis) / 1000;
            long start = (newSponsorSegmentStartMillis) / 1000;
            long end = (newSponsorSegmentEndMillis) / 1000;
            new AlertDialog.Builder(context)
                    .setTitle(str("new_segment_confirm_title"))
                    .setMessage(String.format(str("new_segment_confirm_content"),
                            start / 60, start % 60,
                            end / 60, end % 60,
                            length / 60, length % 60))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, segmentReadyDialogButtonListener)
                    .show();
        } else {
            Toast.makeText(context, str("new_segment_mark_locations_first"), Toast.LENGTH_SHORT).show();
        }
    }

    public static void onVotingClicked(final Context context) {
        if (sponsorSegmentsOfCurrentVideo == null || sponsorSegmentsOfCurrentVideo.length == 0) {
            Toast.makeText(context.getApplicationContext(), str("vote_no_segments"), Toast.LENGTH_SHORT).show();
            return;
        }
        int segmentAmount = sponsorSegmentsOfCurrentVideo.length;
        List<CharSequence> titles = new ArrayList<>(segmentAmount); // I've replaced an array with a list to prevent null elements in the array as unsubmitted segments get filtered out
        for (int i = 0; i < segmentAmount; i++) {
            SponsorSegment segment = sponsorSegmentsOfCurrentVideo[i];
            if (segment.category == SponsorBlockSettings.SegmentInfo.Unsubmitted) {
                continue;
            }

            String start = dateFormatter.format(new Date(segment.start));
            String end = dateFormatter.format(new Date(segment.end));
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append(String.format("<b><font color=\"#%06X\">⬤</font> %s<br> %s to %s",
                    segment.category.color, segment.category.title, start, end));
            if (i + 1 != segmentAmount) // prevents trailing new line after last segment
                htmlBuilder.append("<br>");
            titles.add(Html.fromHtml(htmlBuilder.toString()));
        }

        new AlertDialog.Builder(context)
                .setItems(titles.toArray(new CharSequence[0]), segmentVoteClickListener)
                .show();
    }

    private static void onNewCategorySelect(final SponsorSegment segment, Context context) {
        final SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();
        CharSequence[] titles = new CharSequence[values.length];
        for (int i = 0; i < values.length; i++) {
            titles[i] = values[i].getTitleWithDot();
        }

        new AlertDialog.Builder(context)
                .setTitle(str("new_segment_choose_category"))
                .setItems(titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voteForSegment(segment, VoteOption.CATEGORY_CHANGE, values[which].key);
                    }
                })
                .show();
    }

    @SuppressLint("DefaultLocale")
    public static void onPreviewClicked(Context context) {
        if (newSponsorSegmentStartMillis >= 0 && newSponsorSegmentStartMillis < newSponsorSegmentEndMillis) {
//            Toast t = Toast.makeText(context, "Preview", Toast.LENGTH_SHORT);
//            t.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, t.getXOffset(), t.getYOffset());
//            t.show();
            PlayerController.skipToMillisecond(newSponsorSegmentStartMillis - 3000);
            final SponsorSegment[] original = PlayerController.sponsorSegmentsOfCurrentVideo;
            final SponsorSegment[] segments = original == null ? new SponsorSegment[1] : Arrays.copyOf(original, original.length + 1);

            segments[segments.length - 1] = new SponsorSegment(newSponsorSegmentStartMillis, newSponsorSegmentEndMillis,
                    SponsorBlockSettings.SegmentInfo.Unsubmitted, null);

            Arrays.sort(segments);
            sponsorSegmentsOfCurrentVideo = segments;
        } else {
            Toast.makeText(context, str("new_segment_mark_locations_first"), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
    public static void onEditByHandClicked(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(str("new_segment_edit_by_hand_title"))
                .setMessage(str("new_segment_edit_by_hand_content"))
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(str("new_segment_mark_start"), editByHandDialogListener)
                .setPositiveButton(str("new_segment_mark_end"), editByHandDialogListener)
                .show();
    }

    public static void notifyShareBtnVisibilityChanged(View v) {
        if (v.getId() != shareBtnId || !/*SponsorBlockSettings.isAddNewSegmentEnabled*/false) return;
//        if (VERBOSE)
//            Log.d(TAG, "VISIBILITY CHANGED of view " + v);
        ImageView sponsorBtn = ShieldButton._shieldBtn.get();
        if (sponsorBtn != null) {
            sponsorBtn.setVisibility(v.getVisibility());
        }
    }

    public synchronized static SponsorSegment[] getSegmentsForVideo(String videoId) {
        newSponsorSegmentEndMillis = newSponsorSegmentStartMillis = -1;

        ArrayList<SponsorSegment> sponsorSegments = new ArrayList<>();
        try {
            if (VERBOSE)
                Log.i(TAG, "Trying to download segments for videoId=" + videoId);

            URL url = new URL(SponsorBlockSettings.getSponsorBlockUrlWithCategories(videoId));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            switch (connection.getResponseCode()) {
                default:
                    Log.e(TAG, "Unable to download segments: Status: " + connection.getResponseCode() + " " + connection.getResponseMessage());
                    break;
                case 404:
                    Log.w(TAG, "No segments for this video (ERR404)");
                    break;
                case 200:
                    if (VERBOSE)
                        Log.i(TAG, "Received status 200 OK, parsing response...");

                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    connection.getInputStream().close();


                    JSONArray responseArray = new JSONArray(stringBuilder.toString());
                    int length = responseArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject obj = ((JSONObject) responseArray.get(i));
                        JSONArray segments = obj.getJSONArray("segment");
                        long start = (long) (segments.getDouble(0) * 1000);
                        long end = (long) (segments.getDouble(1) * 1000);
                        String category = obj.getString("category");
                        String UUID = obj.getString("UUID");

                        SponsorBlockSettings.SegmentInfo segmentCategory = SponsorBlockSettings.SegmentInfo.byCategoryKey(category);
                        if (segmentCategory != null && segmentCategory.behaviour.showOnTimeBar) {
                            SponsorSegment segment = new SponsorSegment(start, end, segmentCategory, UUID);
                            sponsorSegments.add(segment);
                        }
                    }

                    if (VERBOSE)
                        Log.v(TAG, "Parsing done");
                    break;
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "download segments failed", e);
        }

        View layout = XSwipeHelper.nextGenWatchLayout.findViewById(getIdentifier("player_overlays", "id"));
        View bar = layout.findViewById(getIdentifier("time_bar_total_time", "id"));

        ((TextView) bar).append(getTimeWithoutSegments());

        return sponsorSegments.toArray(new SponsorSegment[0]);
    }

    private static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    public static void sendViewCountRequest(SponsorSegment segment) {
        try {
            URL url = new URL(SponsorBlockSettings.getSponsorBlockViewedUrl(segment.UUID));

            Log.d("sponsorblock", "requesting: " + url.getPath());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void voteForSegment(SponsorSegment segment, VoteOption voteOption, String... args) {
        messageToToast = null;
        try {
            String voteUrl = voteOption == VoteOption.CATEGORY_CHANGE
                    ? getSponsorBlockVoteUrl(segment.UUID, uuid, args[0])
                    : getSponsorBlockVoteUrl(segment.UUID, uuid, voteOption == VoteOption.UPVOTE ? 1 : 0);
            URL url = new URL(voteUrl);

            Toast.makeText(appContext.get(), str("vote_started"), Toast.LENGTH_SHORT).show();
            Log.d("sponsorblock", "requesting: " + url.getPath());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            switch (connection.getResponseCode()) {
                default:
                    messageToToast = String.format(str("vote_failed_unknown_error"), connection.getResponseCode(), connection.getResponseMessage());
                    break;
                case 429:
                    messageToToast = str("vote_failed_rate_limit");
                    break;
                case 403:
                    messageToToast = str("vote_failed_forbidden");
                    break;
                case 200:
                    messageToToast = str("vote_succeeded");
                    break;
            }

            Log.i(TAG, "Voted for segment with status: " + connection.getResponseCode() + ", " + messageToToast);
            new Handler(Looper.getMainLooper()).post(toastRunnable);

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTimeWithoutSegments() {
        if (!SponsorBlockSettings.isSponsorBlockEnabled || sponsorSegmentsOfCurrentVideo == null) {
            return "";
        }
        long timeWithoutSegments = PlayerController.getCurrentVideoLength();
        for (SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
            timeWithoutSegments -= segment.end - segment.start;
        }
        Date date = new Date(timeWithoutSegments);
        return timeWithoutSegments >= 3600000 ? withoutSegmentsFormatterH.format(date) : withoutSegmentsFormatter.format(date);
    }

    private enum VoteOption {
        UPVOTE(str("vote_upvote")),
        DOWNVOTE(str("vote_downvote")),
        CATEGORY_CHANGE(str("vote_category"));

        public final String title;

        VoteOption(String title) {
            this.title = title;
        }
    }

    private static class EditByHandSaveDialogListener implements DialogInterface.OnClickListener {
        public boolean settingStart;
        public WeakReference<EditText> editText;

        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final EditText editText = this.editText.get();
            if (editText == null) return;
            Context context = ((AlertDialog) dialog).getContext();

            try {
                long time = (which == DialogInterface.BUTTON_NEUTRAL) ?
                        getLastKnownVideoTime() :
                        (Objects.requireNonNull(dateFormatter.parse(editText.getText().toString())).getTime());

                if (settingStart)
                    newSponsorSegmentStartMillis = Math.max(time, 0);
                else
                    newSponsorSegmentEndMillis = time;

                if (which == DialogInterface.BUTTON_NEUTRAL)
                    editByHandDialogListener.onClick(dialog, settingStart ?
                            DialogInterface.BUTTON_NEGATIVE :
                            DialogInterface.BUTTON_POSITIVE);
                else
                    Toast.makeText(context.getApplicationContext(), str("new_segment_edit_by_hand_saved"), Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {
                Toast.makeText(context.getApplicationContext(), str("new_segment_edit_by_hand_parse_error"), Toast.LENGTH_LONG).show();
            }
        }
    }
}
