package app.revanced.extension.instagram.misc.followbackindicator;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.instagram.misc.followbackindicator.Helper;
import com.instagram.common.session.UserSession;

@SuppressWarnings("unused")
public class FollowBackIndicatorPatch {

    public static void indicator(UserSession userSession, Object profileInfoObject, Object badgeObject){
        try {
            String loggedInUserId = userSession.getUserId();
            Object viewingProfileUserObject = Helper.getViewingProfileUserObject(profileInfoObject);
            String viewingProfileUserId = Helper.getViewingProfileUserId(viewingProfileUserObject);

            // If the logged in user id is same as viewing profile, then no need to display the badge.
            if(loggedInUserId.equals(viewingProfileUserId)) return;

            Boolean followed_by = Helper.getFollowbackInfo(viewingProfileUserObject);
            String indicatorText = followed_by ? "Follows you" : "Does not follow you";
            Helper.setInternalBadgeText(badgeObject,indicatorText);

        } catch (Exception ex){
            Logger.printException(() -> "Failed follow back indicator", ex);
        }
    }
}
