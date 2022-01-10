package fi.vanced.libraries.youtube.ui;

import static pl.jakubweg.StringRef.str;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import fi.vanced.libraries.youtube.player.VideoHelpers;
import fi.vanced.utils.VancedUtils;

public class SponsorBlock extends SlimButton {
    public SponsorBlock(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID, false);

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(VancedUtils.getIdentifier("vanced_sb_logo", "drawable"));
        this.button_text.setText("SB");
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(YouTubeTikTokRoot_Application.getAppContext(), "Nothing atm", Toast.LENGTH_SHORT).show();
    }
}
