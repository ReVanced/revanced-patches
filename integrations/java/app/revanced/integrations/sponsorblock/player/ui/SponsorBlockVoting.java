package app.revanced.integrations.sponsorblock.player.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.utils.ReVancedUtils;

public class SponsorBlockVoting extends SlimButton {
    public SponsorBlockVoting(Context context, ViewGroup container) {
        super(context, container, SlimButton.SLIM_METADATA_BUTTON_ID, false);

        initialize();
    }

    private void initialize() {
        this.button_icon.setImageResource(ReVancedUtils.getIdentifier("vanced_sb_voting", "drawable"));
        this.button_text.setText("SB Voting");
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(YouTubeTikTokRoot_Application.getAppContext(), "Nothing atm", Toast.LENGTH_SHORT).show();
    }
}
