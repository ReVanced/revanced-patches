package fi.vanced.libraries.youtube.ryd;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static fi.vanced.libraries.youtube.player.VideoInformation.dislikeCount;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_RYD_ENABLED;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_NAME;
import static fi.vanced.utils.VancedUtils.getIdentifier;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Locale;
import java.util.Objects;

import fi.vanced.libraries.youtube.ryd.requests.RYDRequester;
import fi.vanced.utils.SharedPrefUtils;

public class ReturnYouTubeDislikes {
    public static boolean isEnabled;
    public static final String TAG = "VI - RYD";
    private static View _dislikeView = null;
    private static Thread _dislikeFetchThread = null;
    private static Thread _votingThread = null;
    private static Registration registration;
    private static Voting voting;
    private static boolean likeActive;
    private static boolean dislikeActive;
    private static int votingValue = 0; // 1 = like, -1 = dislike, 0 = no vote
    private static CompactDecimalFormat compactNumberFormatter;

    static {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        isEnabled = SharedPrefUtils.getBoolean(Objects.requireNonNull(context), PREFERENCES_NAME, PREFERENCES_KEY_RYD_ENABLED, false);
        if (isEnabled) {
            registration = new Registration(context);
            voting = new Voting(context, registration);
        }

        Locale locale = context.getResources().getConfiguration().locale;
        if (debug) {
            Log.d(TAG, "locale - " + locale);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            compactNumberFormatter = CompactDecimalFormat.getInstance(
                    locale,
                    CompactDecimalFormat.CompactStyle.SHORT
            );
        }
    }

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
        if (registration == null) {
            registration = new Registration(YouTubeTikTokRoot_Application.getAppContext());
        }
        if (voting == null) {
            voting = new Voting(YouTubeTikTokRoot_Application.getAppContext(), registration);
        }
    }

    public static void newVideoLoaded(String videoId) {
        if (debug) {
            Log.d(TAG, "newVideoLoaded - " + videoId);
        }

        dislikeCount = null;
        if (!isEnabled) return;

        try {
            if (_dislikeFetchThread != null && _dislikeFetchThread.getState() != Thread.State.TERMINATED) {
                if (debug) {
                    Log.d(TAG, "Interrupting the thread. Current state " + _dislikeFetchThread.getState());
                }
                _dislikeFetchThread.interrupt();
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error in the dislike fetch thread", ex);
        }

        _dislikeFetchThread = new Thread(() -> RYDRequester.fetchDislikes(videoId));
        _dislikeFetchThread.start();
    }

    // Call to this needs to be injected in YT code
    public static void setLikeTag(View view) {
        if (!isEnabled) return;

        setTag(view, "like");
    }

    public static void setLikeTag(View view, boolean active) {
        if (!isEnabled) return;

        likeActive = active;
        if (likeActive) {
            votingValue = 1;
        }
        if (debug) {
            Log.d(TAG, "Like tag active " + likeActive);
        }
        setTag(view, "like");
    }

    // Call to this needs to be injected in YT code
    public static void setDislikeTag(View view) {
        if (!isEnabled) return;

        _dislikeView = view;
        setTag(view, "dislike");
    }

    public static void setDislikeTag(View view, boolean active) {
        if (!isEnabled) return;

        dislikeActive = active;
        if (dislikeActive) {
            votingValue = -1;
        }
        _dislikeView = view;
        if (debug) {
            Log.d(TAG, "Dislike tag active " + dislikeActive);
        }
        setTag(view, "dislike");
    }

    // Call to this needs to be injected in YT code
    public static CharSequence onSetText(View view, CharSequence originalText) {
        if (!isEnabled) return originalText;
        return handleOnSetText(view, originalText);
    }

    // Call to this needs to be injected in YT code
    public static void onClick(View view, boolean inactive) {
        if (!isEnabled) return;

        handleOnClick(view, inactive);
    }

    private static CharSequence handleOnSetText(View view, CharSequence originalText) {
        if (!isEnabled) return originalText;

        try {
            CharSequence tag = (CharSequence) view.getTag();
            if (debug) {
                Log.d(TAG, "handleOnSetText - " + tag + " - original text - " + originalText);
            }
            if (tag == null) return originalText;

            if (tag == "like") {
                return originalText;
            }
            else if (tag == "dislike") {
                return dislikeCount != null ? formatDislikes(dislikeCount) : originalText;
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error while handling the setText", ex);
        }

        return originalText;
    }

    public static void trySetDislikes(String dislikeCount) {
        if (!isEnabled) return;

        try {
            // Try to set normal video dislike count
            if (_dislikeView == null) {
                if (debug) { Log.d(TAG, "_dislikeView was null"); }
                return;
            }

            View buttonView = _dislikeView.findViewById(getIdentifier("button_text", "id"));
            if (buttonView == null) {
                if (debug) { Log.d(TAG, "buttonView was null"); }
                return;
            }
            TextView button = (TextView) buttonView;
            button.setText(dislikeCount);
            if (debug) {
                Log.d(TAG, "trySetDislikes - " + dislikeCount);
            }
        }
        catch (Exception ex) {
            if (debug) {
                Log.e(TAG, "Error while trying to set dislikes text", ex);
            }
        }
    }

    private static void handleOnClick(View view, boolean previousState) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (!isEnabled || SharedPrefUtils.getBoolean(Objects.requireNonNull(context),"youtube","user_signed_out",true)) return;

        try {
            String tag = (String) view.getTag();
            if (debug) {
                Log.d(TAG, "handleOnClick - " + tag + " - previousState - " + previousState);
            }
            if (tag == null) return;

            // If active status was removed, vote should be none
            if (previousState) { votingValue = 0; }
            if (tag.equals("like")) {

                // Like was activated
                if (!previousState) { votingValue = 1; likeActive = true; }
                else { likeActive = false; }

                // Like was activated and dislike was previously activated
                if (!previousState && dislikeActive) { dislikeCount--; trySetDislikes(formatDislikes(dislikeCount)); }
                dislikeActive = false;
            }
            else if (tag.equals("dislike")) {
                likeActive = false;

                // Dislike was activated
                if (!previousState) { votingValue = -1; dislikeActive = true; dislikeCount++; }
                // Dislike was removed
                else { dislikeActive = false; dislikeCount--; }
                trySetDislikes(formatDislikes(dislikeCount));
            }
            else {
                // Unknown tag
                return;
            }

            if (debug) {
                Log.d(TAG, "New vote status - " + votingValue);
                Log.d(TAG, "Like button " + likeActive + " | Dislike button " + dislikeActive);
            }

            sendVote(votingValue);
        }
        catch (Exception ex) {
            Log.e(TAG, "Error while handling the onClick", ex);
        }
    }

    private static void sendVote(int vote) {
        if (!isEnabled) return;

        if (debug) {
            Log.d(TAG, "sending vote - " + vote + " for video " + currentVideoId);
        }

        try {
            if (_votingThread != null && _votingThread.getState() != Thread.State.TERMINATED) {
                if (debug) {
                    Log.d(TAG, "Interrupting the thread. Current state " + _votingThread.getState());
                }
                _votingThread.interrupt();
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error in the voting thread", ex);
        }

        _votingThread = new Thread(() -> {
            try {
                boolean result = voting.sendVote(currentVideoId, vote);
                if (debug) {
                    Log.d(TAG, "sendVote status " + result);
                }
            }
            catch (Exception ex) {
                Log.e(TAG, "Failed to send vote", ex);
            }
        });
        _votingThread.start();
    }

    private static void setTag(View view, String tag) {
        if (!isEnabled) return;

        try {
            if (view == null) {
                if (debug) {
                    Log.d(TAG, "View was empty");
                }
                return;
            }

            if (debug) {
                Log.d(TAG, "setTag - " + tag);
            }

            view.setTag(tag);
        }
        catch (Exception ex) {
            Log.e(TAG, "Error while trying to set tag to view", ex);
        }
    }

    public static String formatDislikes(int dislikes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && compactNumberFormatter != null) {
            final String formatted = compactNumberFormatter.format(dislikes);
            if (debug) {
                Log.d(TAG, "Formatting dislikes - " + dislikes + " - " + formatted);
            }

            return formatted;
        }

        if (debug) {
            Log.d(TAG, "Couldn't format dislikes, using the unformatted count - " + dislikes);
        }
        return String.valueOf(dislikes);
    }
}
