package fi.vanced.libraries.youtube.ryd;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.currentVideoId;
import static fi.vanced.utils.VancedUtils.getIdentifier;
import static fi.vanced.utils.VancedUtils.parseJson;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

import static fi.vanced.libraries.youtube.player.VideoInformation.dislikeCount;

public class ReturnYouTubeDislikes {
    public static final String RYD_API_URL = "https://returnyoutubedislikeapi.com";
    private static final String TAG = "VI - RYD";
    private static View _dislikeView = null;
    private static Thread _dislikeFetchThread = null;
    private static Thread _votingThread = null;
    private static Registration registration;
    private static Voting voting;
    private static boolean likeActive;
    private static boolean dislikeActive;
    private static int votingValue = 0; // 1 = like, -1 = dislike, 0 = no vote

    static {
        registration = new Registration(YouTubeTikTokRoot_Application.getAppContext());
        voting = new Voting(YouTubeTikTokRoot_Application.getAppContext(), registration);
    }

    public static void newVideoLoaded(String videoId) {
        if (debug) {
            Log.d(TAG, "newVideoLoaded - " + videoId);
        }

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

        _dislikeFetchThread = new Thread(() -> {
            try {
                if (debug) {
                    Log.d(TAG, "Fetching dislikes for " + videoId);
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(RYD_API_URL + "/votes?videoId=" + videoId).openConnection();
                connection.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");
                connection.setConnectTimeout(5 * 1000);
                if (connection.getResponseCode() == 200) {
                    JSONObject json = new JSONObject(parseJson(connection));
                    dislikeCount = json.getInt("dislikes");
                    if (debug) {
                        Log.d(TAG, "dislikes fetched - " + dislikeCount);
                    }

                    // Set the dislikes
                    new Handler(Looper.getMainLooper()).post(new Runnable () {
                        @Override
                        public void run () {
                            trySetDislikes(String.valueOf(dislikeCount));
                        }
                    });
                }
                else if (debug) {
                    Log.d(TAG, "dislikes fetch response was " + connection.getResponseCode());
                }
            }
            catch (Exception ex) {
                dislikeCount = null;
                Log.e(TAG, "Failed to fetch dislikes", ex);
                return;
            }
        });
        _dislikeFetchThread.start();
    }

    // Call to this needs to be injected in YT code
    public static void setLikeTag(View view) {
        setTag(view, "like");
    }

    public static void setLikeTag(View view, boolean active) {
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
        _dislikeView = view;
        setTag(view, "dislike");
    }

    public static void setDislikeTag(View view, boolean active) {
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
        return handleOnSetText(view, originalText);
    }

    // Call to this needs to be injected in YT code
    public static void onClick(View view, boolean inactive) {
        handleOnClick(view, inactive);
    }

    private static CharSequence handleOnSetText(View view, CharSequence originalText) {
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
                return dislikeCount != null ? String.valueOf(dislikeCount) : originalText;
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Error while handling the setText", ex);
        }

        return originalText;
    }

    private static void trySetDislikes(String dislikeCount) {
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
        try {
            String tag = (String) view.getTag();
            if (debug) {
                Log.d(TAG, "handleOnClick - " + tag + " - previousState - " + previousState);
            }
            if (tag == null) return;

            // If active status was removed, vote should be none
            if (previousState) { votingValue = 0; }
            if (tag == "like") {
                dislikeActive = false;

                // Like was activated
                if (!previousState) { votingValue = 1; likeActive = true; }
                else { likeActive = false; }

                // Like was activated and dislike was previously activated
                if (!previousState && dislikeActive) { dislikeCount--; trySetDislikes(String.valueOf(dislikeCount)); }
            }
            else if (tag == "dislike") {
                likeActive = false;

                // Dislike was activated
                if (!previousState) { votingValue = -1; dislikeActive = true; dislikeCount++; }
                // Dislike was removed
                else { dislikeActive = false; dislikeCount--; }
                trySetDislikes(String.valueOf(dislikeCount));
            }
            else {
                // Unknown tag
                return;
            }

            if (debug) {
                Log.d(TAG, "New vote status - " + votingValue);
                Log.d(TAG, "Like button " + likeActive + " | Dislike button " + dislikeActive);
            }

            Toast.makeText(YouTubeTikTokRoot_Application.getAppContext(), "Voting value: " + votingValue, Toast.LENGTH_SHORT).show();

            sendVote(votingValue);
        }
        catch (Exception ex) {
            Log.e(TAG, "Error while handling the onClick", ex);
        }
    }

    private static void sendVote(int vote) {
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
                return;
            }
        });
        _votingThread.start();
    }

    private static void setTag(View view, String tag) {
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
}
