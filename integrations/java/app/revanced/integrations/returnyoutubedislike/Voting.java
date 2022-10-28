package app.revanced.integrations.returnyoutubedislike;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;

public class Voting {
    private Registration registration;

    public Voting(Registration registration) {
        this.registration = registration;
    }

    public boolean sendVote(String videoId, ReturnYouTubeDislike.Vote vote) {
        String userId = registration.getUserId();
        LogHelper.debug(Voting.class, "Trying to vote the following video: " + videoId + " with vote " + vote + " and userId: " + userId);
        return ReturnYouTubeDislikeApi.sendVote(videoId, userId, vote.value);
    }
}
