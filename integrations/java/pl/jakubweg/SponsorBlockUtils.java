package pl.jakubweg;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static pl.jakubweg.PlayerController.VERBOSE;
import static pl.jakubweg.PlayerController.getCurrentVideoId;
import static pl.jakubweg.PlayerController.getLastKnownVideoTime;
import static pl.jakubweg.PlayerController.sponsorSegmentsOfCurrentVideo;
import static pl.jakubweg.SponsorBlockSettings.sponsorBlockSkipSegmentsUrl;
import static pl.jakubweg.StringRef.sf;
import static pl.jakubweg.StringRef.str;

@SuppressWarnings({"LongLogTag"})
public abstract class SponsorBlockUtils {
    public static final String TAG = "jakubweg.SponsorBlockUtils";
    public static final String DATE_FORMAT = "HH:mm:ss.SSS";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
    private static final int sponsorBtnId = 1234;
    private static final View.OnClickListener sponsorBlockBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NewSegmentHelperLayout.toggle();
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
            SponsorBlockSettings.SegmentInfo segmentType = SponsorBlockSettings.SegmentInfo.valuesWithoutPreview()[which];
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

            SponsorBlockSettings.SegmentInfo[] values = SponsorBlockSettings.SegmentInfo.valuesWithoutPreview();
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
    private static boolean isShown = false;
    private static WeakReference<ImageView> sponsorBlockBtn = new WeakReference<>(null);
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

    public static void showButton() {
        if (isShown) return;
        isShown = true;
        View i = sponsorBlockBtn.get();
        if (i == null) return;
        i.setVisibility(VISIBLE);
        i.bringToFront();
        i.requestLayout();
        i.invalidate();
    }

    public static void hideButton() {
        if (!isShown) return;
        isShown = false;
        View i = sponsorBlockBtn.get();
        if (i != null)
            i.setVisibility(GONE);
    }

    @SuppressLint("LongLogTag")
    public static void addImageButton(final Activity activity, final int attemptsWhenFail) {
        if (VERBOSE)
            Log.d(TAG, "addImageButton activity=" + activity + ",attemptsWhenFail=" + attemptsWhenFail);

        if (activity == null)
            return;

        final View existingSponsorBtn = activity.findViewById(sponsorBtnId);
        if (existingSponsorBtn != null) {
            if (VERBOSE)
                Log.d(TAG, "addImageButton: sponsorBtn exists");
            if (SponsorBlockSettings.isAddNewSegmentEnabled)
                showButton();
            return;
        }

        String packageName = activity.getPackageName();
        Resources R = activity.getResources();
        shareBtnId = R.getIdentifier("player_share_button", "id", packageName);
//        final int addToBtnId = R.getIdentifier("player_addto_button", "id", packageName);
        final int addToBtnId = R.getIdentifier("live_chat_overlay_button", "id", packageName);
        int titleViewId = R.getIdentifier("player_video_title_view", "id", packageName);
//        final int iconId = R.getIdentifier("player_fast_forward", "drawable", packageName);
        final int iconId = R.getIdentifier("ic_sb_logo", "drawable", packageName);


        final View addToBtn = activity.findViewById(addToBtnId);
        final ImageView shareBtn = activity.findViewById(shareBtnId);
        final TextView titleView = activity.findViewById(titleViewId);

        if (addToBtn == null || shareBtn == null || titleView == null) {
            if (VERBOSE)
                Log.e(TAG, String.format("one of following is null: addToBtn=%s shareBtn=%s titleView=%s",
                        addToBtn, shareBtn, titleView));

            if (attemptsWhenFail > 0)
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (VERBOSE)
                            Log.i(TAG, "Retrying addImageButton");
                        addImageButton(PlayerController.playerActivity.get(), attemptsWhenFail - 1);
                    }
                }, 5000);
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {

                    Class<?> touchImageViewClass = Class.forName("com.google.android.libraries.youtube.common.ui.TouchImageView");
                    Constructor<?> constructor = touchImageViewClass.getConstructor(Context.class);
                    final ImageView instance = ((ImageView) constructor.newInstance(activity));
                    instance.setImageResource(iconId);
                    instance.setId(sponsorBtnId);

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(shareBtn.getLayoutParams());
                    layoutParams.addRule(RelativeLayout.LEFT_OF, addToBtnId);

                    instance.setLayoutParams(layoutParams);
                    ((ViewGroup) shareBtn.getParent()).addView(instance, 0);


                    instance.setPadding(shareBtn.getPaddingLeft(),
                            shareBtn.getPaddingTop(),
                            shareBtn.getPaddingRight(),
                            shareBtn.getPaddingBottom());


                    RelativeLayout.LayoutParams titleViewLayoutParams = (RelativeLayout.LayoutParams) titleView.getLayoutParams();
                    titleViewLayoutParams.addRule(RelativeLayout.START_OF, sponsorBtnId);
                    titleView.requestLayout();

                    instance.setClickable(true);
                    instance.setFocusable(true);
                    Drawable.ConstantState constantState = shareBtn.getBackground().mutate().getConstantState();
                    if (constantState != null)
                        instance.setBackground(constantState.newDrawable());

                    instance.setOnClickListener(sponsorBlockBtnListener);
                    sponsorBlockBtn = new WeakReference<>(instance);
                    isShown = true;
                    if (!SponsorBlockSettings.isAddNewSegmentEnabled)
                        hideButton();
                    if (VERBOSE)
                        Log.i(TAG, "Image Button added");
                } catch (Exception e) {
                    Log.e(TAG, "Error while adding button", e);
                }
            }
        });
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
            Toast.makeText(context, "Mark two locations on the time bar first", Toast.LENGTH_SHORT).show();
        }
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
                    SponsorBlockSettings.SegmentInfo.Preview, null);

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
        if (v.getId() != shareBtnId || !SponsorBlockSettings.isAddNewSegmentEnabled) return;
//        if (VERBOSE)
//            Log.d(TAG, "VISIBILITY CHANGED of view " + v);
        ImageView sponsorBtn = sponsorBlockBtn.get();
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

        return sponsorSegments.toArray(new SponsorSegment[0]);
    }

    public static void sendViewCountRequest(SponsorSegment segment) {
        try {
            URL url = new URL(SponsorBlockSettings.getSponsorBlockViewedUrl(segment.UUID));

            Log.d("sponsorblock", "requesting: " + url.getPath());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.getInputStream().close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
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
