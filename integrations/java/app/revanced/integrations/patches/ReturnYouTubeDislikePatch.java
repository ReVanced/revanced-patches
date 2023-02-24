package app.revanced.integrations.patches;

import android.text.Spanned;
import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Used by app.revanced.patches.youtube.layout.returnyoutubedislike.patch.ReturnYouTubeDislikePatch
 */
public class ReturnYouTubeDislikePatch {

    /**
     * Injection point
     */
    public static void newVideoLoaded(String videoId) {
        ReturnYouTubeDislike.newVideoLoaded(videoId);
    }

    /**
     * Injection point
     *
     * Called when a litho text component is created
     */
    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        ReturnYouTubeDislike.onComponentCreated(conversionContext, textRef);
    }

    /**
     * Injection point
     *
     * Called when a Shorts dislike Spannable is created
     */
    public static Spanned onShortsComponentCreated(Spanned dislike) {
        return ReturnYouTubeDislike.onShortsComponentCreated(dislike);
    }

    /**
     * Injection point
     *
     * Called when the like/dislike button is clicked
     *
     * @param vote -1 (dislike), 0 (none) or 1 (like)
     */
    public static void sendVote(int vote) {
        ReturnYouTubeDislike.sendVote(vote);
    }
}
