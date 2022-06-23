package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import app.revanced.integrations.sponsorblock.player.VideoHelpers;
import app.revanced.integrations.utils.ReVancedUtils;

public class CopyButton extends SlimButton {
    public CopyButton(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID, ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_button_list"));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(ReVancedUtils.getIdentifier("vanced_yt_copy_icon", "drawable"));
        this.button_text.setText(str("action_copy"));
    }

    @Override
    public void onClick(View view) {
        VideoHelpers.copyVideoUrlToClipboard();
    }
}
