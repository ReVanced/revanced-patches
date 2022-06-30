package app.revanced.integrations.ryd;

import android.content.Context;


import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.ryd.requests.RYDRequester;

public class Voting {
    private Registration registration;
    private Context context;

    public Voting(Context context, Registration registration) {
        this.context = context;
        this.registration = registration;
    }

    public boolean sendVote(String videoId, int vote) {
        String userId = registration.getUserId();
        LogHelper.debug(Voting.class, "Trying to vote the following video: " + videoId + " with vote " + vote + " and userId: " + userId);
        return RYDRequester.sendVote(videoId, userId, vote);
    }
}
