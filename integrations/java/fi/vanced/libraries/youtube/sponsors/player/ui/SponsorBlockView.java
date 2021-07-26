package fi.vanced.libraries.youtube.sponsors.player.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.lang.ref.WeakReference;

import fi.razerman.youtube.Helpers.XSwipeHelper;

import static fi.razerman.youtube.XGlobals.debug;

public class SponsorBlockView {
    static String TAG = "SponsorBlockView";
    static RelativeLayout inlineSponsorOverlay;
    static ViewGroup _youtubeOverlaysLayout;
    static WeakReference<SkipSponsorButton> _skipSponsorButton = new WeakReference<>(null);
    static WeakReference<NewSegmentLayout> _newSegmentLayout = new WeakReference<>(null);
    static boolean shouldShowOnPlayerType = true;

    public static void initialize(Object viewGroup) {
        try {
            if(debug){
                Log.d(TAG, "initializing");
            }

            _youtubeOverlaysLayout = (ViewGroup) viewGroup;

            addView();
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to set ViewGroup", ex);
        }
    }

    public static void showSkipButton() {
        skipSponsorButtonVisibility(true);
    }
    public static void hideSkipButton() {
        skipSponsorButtonVisibility(false);
    }

    public static void showNewSegmentLayout() {
        newSegmentLayoutVisibility(true);
    }
    public static void hideNewSegmentLayout() {
        newSegmentLayoutVisibility(false);
    }

    public static void playerTypeChanged(String playerType) {
        try {
            shouldShowOnPlayerType = playerType.equalsIgnoreCase("WATCH_WHILE_FULLSCREEN") || playerType.equalsIgnoreCase("WATCH_WHILE_MAXIMIZED");

            if (playerType.equalsIgnoreCase("WATCH_WHILE_FULLSCREEN")) {
                setSkipBtnMargins(true);
                setNewSegmentLayoutMargins(true);
                return;
            }

            setSkipBtnMargins(false);
            setNewSegmentLayoutMargins(false);
        }
        catch (Exception ex) {
            Log.e(TAG, "Player type changed caused a crash.", ex);
        }
    }

    private static void addView() {
        inlineSponsorOverlay = new RelativeLayout(YouTubeTikTokRoot_Application.getAppContext());
        setLayoutParams(inlineSponsorOverlay);
        LayoutInflater.from(YouTubeTikTokRoot_Application.getAppContext()).inflate(getIdentifier("inline_sponsor_overlay", "layout"), inlineSponsorOverlay);

        _youtubeOverlaysLayout.addView(inlineSponsorOverlay, _youtubeOverlaysLayout.getChildCount() - 2);

        SkipSponsorButton skipSponsorButton = (SkipSponsorButton) inlineSponsorOverlay.findViewById(getIdentifier("skip_sponsor_button", "id"));
        _skipSponsorButton = new WeakReference<>(skipSponsorButton);

        NewSegmentLayout newSegmentView = (NewSegmentLayout) inlineSponsorOverlay.findViewById(getIdentifier("new_segment_view", "id"));
        _newSegmentLayout = new WeakReference<>(newSegmentView);
    }

    private static void setLayoutParams(RelativeLayout relativeLayout) {
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private static void setSkipBtnMargins(boolean fullScreen) {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) {
            Log.e(TAG, "Unable to setSkipBtnMargins");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) skipSponsorButton.getLayoutParams();
        if (params == null) {
            Log.e(TAG, "Unable to setSkipBtnMargins");
            return;
        }
        params.bottomMargin = fullScreen ? skipSponsorButton.ctaBottomMargin : skipSponsorButton.defaultBottomMargin;
        skipSponsorButton.setLayoutParams(params);
    }

    private static void skipSponsorButtonVisibility(boolean visible) {
        SkipSponsorButton skipSponsorButton = _skipSponsorButton.get();
        if (skipSponsorButton == null) {
            Log.e(TAG, "Unable to skipSponsorButtonVisibility");
            return;
        }

        visible &= shouldShowOnPlayerType;

        skipSponsorButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        bringLayoutToFront();
    }

    private static void setNewSegmentLayoutMargins(boolean fullScreen) {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) {
            Log.e(TAG, "Unable to setNewSegmentLayoutMargins");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newSegmentLayout.getLayoutParams();
        if (params == null) {
            Log.e(TAG, "Unable to setNewSegmentLayoutMargins");
            return;
        }
        params.bottomMargin = fullScreen ? newSegmentLayout.ctaBottomMargin : newSegmentLayout.defaultBottomMargin;
        newSegmentLayout.setLayoutParams(params);
    }

    private static void newSegmentLayoutVisibility(boolean visible) {
        NewSegmentLayout newSegmentLayout = _newSegmentLayout.get();
        if (newSegmentLayout == null) {
            Log.e(TAG, "Unable to newSegmentLayoutVisibility");
            return;
        }

        visible &= shouldShowOnPlayerType;

        newSegmentLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        bringLayoutToFront();
    }

    private static void bringLayoutToFront() {
        checkLayout();
        inlineSponsorOverlay.bringToFront();
        inlineSponsorOverlay.requestLayout();
        inlineSponsorOverlay.invalidate();
    }

    private static void checkLayout() {
        if (inlineSponsorOverlay.getHeight() == 0) {
            View layout = XSwipeHelper.nextGenWatchLayout.findViewById(getIdentifier("player_overlays", "id"));
            if (layout != null) {

                initialize(layout);

                if (debug){
                    Log.d("XGlobals", "player_overlays refreshed for SB");
                }
            }
            else if (debug){
                Log.d("XGlobals", "player_overlays was not found for SB");
            }
        }
    }

    private static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }
}
