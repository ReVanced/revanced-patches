package app.revanced.extension.primevideo.ads;

import com.amazon.avod.fsm.SimpleTrigger;
import com.amazon.avod.media.ads.AdBreak;
import com.amazon.avod.media.ads.internal.state.AdBreakTrigger;
import com.amazon.avod.media.ads.internal.state.AdEnabledPlayerTriggerType;
import com.amazon.avod.media.playback.VideoPlayer;
import com.amazon.avod.media.ads.internal.state.ServerInsertedAdBreakState;

@SuppressWarnings("unused")
public final class SkipAdsPatch {
    public static void ServerInsertedAdBreakState_enter(ServerInsertedAdBreakState state, AdBreakTrigger trigger, VideoPlayer player) {
        AdBreak adBreak = trigger.getBreak();

        if (trigger.getSeekStartPosition() != null)
            // if scrubbing over ad, seek straight to it
            player.seekTo(trigger.getSeekTarget().getTotalMilliseconds());
        else
            // if naturally entering ad, seek to end of ad break
            player.seekTo(player.getCurrentPosition() + adBreak.getDurationExcludingAux().getTotalMilliseconds());

        // send "end of ads" trigger to state machine so everything doesn't get whacky
        state.doTrigger(new SimpleTrigger(AdEnabledPlayerTriggerType.NO_MORE_ADS_SKIP_TRANSITION));
    }
}