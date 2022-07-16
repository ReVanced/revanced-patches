package app.revanced.integrations.patches;

import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.ryd.ReturnYouTubeDislikes;

/**
 * Used by app.revanced.patches.youtube.layout.returnyoutubedislikes.patch.RYDPatch
 */
public class ReturnYouTubeDislikesPatch {

    /**
     * Called when the video id changes
     */
    public static void newVideoLoaded(String videoId) {
        ReturnYouTubeDislikes.newVideoLoaded(videoId);
    }

    /**
     * Called when a litho text component is created
     */
    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        ReturnYouTubeDislikes.onComponentCreated(conversionContext, textRef);
    }

    /**
     * Called when the like/dislike button is clicked
     * @param vote -1 (dislike), 0 (none) or 1 (like)
     */
    public static void sendVote(int vote) {
        ReturnYouTubeDislikes.sendVote(vote);
    }
}
