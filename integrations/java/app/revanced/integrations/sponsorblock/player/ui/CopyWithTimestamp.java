package app.revanced.integrations.sponsorblock.player.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import app.revanced.integrations.sponsorblock.player.VideoHelpers;
import app.revanced.integrations.utils.ReVancedUtils;

import static app.revanced.integrations.sponsorblock.StringRef.str;

public class CopyWithTimestamp extends SlimButton {
    public CopyWithTimestamp(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID, ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_timestamp_button_list"));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(ReVancedUtils.getIdentifier("vanced_yt_copy_icon_with_time", "drawable"));
        this.button_text.setText(str("action_tcopy"));
    }

    @Override
    public void onClick(View view) {
        VideoHelpers.copyVideoUrlWithTimeStampToClipboard();
    }
}
