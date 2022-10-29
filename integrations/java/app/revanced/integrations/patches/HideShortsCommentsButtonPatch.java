package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideShortsCommentsButtonPatch {
    //Used by app.revanced.patches.youtube.layout.comments.patch.CommentsPatch
    public static void hideShortsCommentsButton(View view) {
        if (!SettingsEnum.HIDE_SHORTS_COMMENTS_BUTTON.getBoolean()) return;
        view.setVisibility(View.GONE);
    }
}
