package app.revanced.extension.primevideo.ads;

import com.amazon.avod.fsm.SimpleTrigger;
import com.amazon.avod.media.ads.AdBreak;
import com.amazon.avod.media.ads.internal.state.AdBreakTrigger;
import com.amazon.avod.media.ads.internal.state.AdEnabledPlayerTriggerType;
import com.amazon.avod.media.playback.VideoPlayer;
import com.amazon.avod.media.ads.internal.state.ServerInsertedAdBreakState;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public final class SkipAdsPatch {
    public static void enterServerInsertedAdBreakState(ServerInsertedAdBreakState state, AdBreakTrigger trigger, VideoPlayer player) {
        try {
            AdBreak adBreak = trigger.getBreak();

            // There are two scenarios when entering the original method:
            //  1. Player naturally entered an ad break while watching a video.
            //  2. User is skipped/scrubbed to a position on the timeline. If seek position is past an ad break,
            //     user is forced to watch an ad before continuing.
            //
            // Scenario 2 is indicated by trigger.getSeekStartPosition() != null, so skip directly to the scrubbing
            // target. Otherwise, just calculate when the ad break should end and skip to there.
            if (trigger.getSeekStartPosition() != null)
                player.seekTo(trigger.getSeekTarget().getTotalMilliseconds());
            else
                player.seekTo(player.getCurrentPosition() + adBreak.getDurationExcludingAux().getTotalMilliseconds());

            // Send "end of ads" trigger to state machine so everything doesn't get whacky.
            state.doTrigger(new SimpleTrigger(AdEnabledPlayerTriggerType.NO_MORE_ADS_SKIP_TRANSITION));
        } catch (Exception ex) {
            Logger.printException(() -> "Failed skipping ads", ex);
        }
    }
}