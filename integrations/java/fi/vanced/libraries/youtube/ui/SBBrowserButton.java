package fi.vanced.libraries.youtube.ui;

import static pl.jakubweg.StringRef.str;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import fi.vanced.libraries.youtube.player.VideoInformation;
import fi.vanced.utils.VancedUtils;
import pl.jakubweg.SponsorBlockSettings;
import pl.jakubweg.SponsorBlockUtils;

public class SBBrowserButton extends SlimButton {
    private static final String BROWSER_URL = "https://sb.ltn.fi/video/";

    public SBBrowserButton(Context context, ViewGroup container) {
        super(context, container, SLIM_METADATA_BUTTON_ID,
                SponsorBlockUtils.isSBButtonEnabled(context, SponsorBlockSettings.PREFERENCES_KEY_BROWSER_BUTTON));

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(VancedUtils.getIdentifier("vanced_sb_browser", "drawable"));
        this.button_text.setText(str("action_browser"));
    }

    @Override
    public void onClick(View v) {
        Uri uri = Uri.parse(BROWSER_URL + VideoInformation.currentVideoId);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}