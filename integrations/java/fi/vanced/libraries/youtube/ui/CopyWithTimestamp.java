package fi.vanced.libraries.youtube.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import fi.vanced.libraries.youtube.player.VideoHelpers;
import fi.vanced.utils.VancedUtils;

import static pl.jakubweg.StringRef.str;

public class CopyWithTimestamp extends SlimButton {
    public CopyWithTimestamp(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID, ButtonVisibility.isVisibleInContainer(context, "pref_copy_video_url_timestamp_button_list"));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(VancedUtils.getIdentifier("vanced_yt_copy_icon_with_time", "drawable"));
        this.button_text.setText(str("action_tcopy"));
    }

    @Override
    public void onClick(View view) {
        VideoHelpers.copyVideoUrlWithTimeStampToClipboard();
    }
}
