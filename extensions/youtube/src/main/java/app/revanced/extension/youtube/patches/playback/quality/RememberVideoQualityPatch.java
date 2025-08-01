package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.NetworkType;
import static app.revanced.extension.shared.Utils.dipToPixels;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.IntegerSetting;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

@SuppressWarnings("unused")
public class RememberVideoQualityPatch {

    /**
     * Interface to use obfuscated methods.
     */
    public interface VideoQualityMenuInterface {
        void patch_setMenuIndexFromQuality(VideoQuality quality);
    }

    public static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;
    private static final IntegerSetting videoQualityWifi = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting videoQualityMobile = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;
    private static final IntegerSetting shortsQualityWifi = Settings.SHORTS_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting shortsQualityMobile = Settings.SHORTS_QUALITY_DEFAULT_MOBILE;

    private static boolean qualityNeedsUpdating;

    /**
     * If the user selected a new quality from the flyout menu,
     * and {@link Settings#REMEMBER_VIDEO_QUALITY_LAST_SELECTED}
     * or {@link Settings#REMEMBER_SHORTS_QUALITY_LAST_SELECTED} is enabled.
     */
    private static boolean userChangedDefaultQuality;

    /**
     * Index of the video quality chosen by the user from the flyout menu.
     */
    private static int userSelectedQualityIndex;

    /**
     * The available qualities of the current video.
     */
    @Nullable
    private static List<VideoQuality> videoQualities;

    /**
     * The current VideoQualityMenuInterface, set during setVideoQuality.
     */
    @Nullable
    private static VideoQualityMenuInterface currentMenuInterface;

    private static boolean shouldRememberVideoQuality() {
        BooleanSetting preference = ShortsPlayerState.isOpen() ?
                Settings.REMEMBER_SHORTS_QUALITY_LAST_SELECTED
                : Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED;
        return preference.get();
    }

    public static int getDefaultVideoQuality() {
        final boolean isShorts = ShortsPlayerState.isOpen();
        return Utils.getNetworkType() == NetworkType.MOBILE
                ? (isShorts ? shortsQualityMobile : videoQualityMobile).get()
                : (isShorts ? shortsQualityWifi : videoQualityWifi).get();
    }

    @Nullable
    public static VideoQualityMenuInterface getCurrentMenuInterface() {
        return currentMenuInterface;
    }

    @Nullable
    public static List<VideoQuality> getVideoQualities() {
        return videoQualities;
    }

    private static void changeDefaultQuality(int qualityResolution) {
        final boolean shortPlayerOpen = ShortsPlayerState.isOpen();
        String networkTypeMessage;
        IntegerSetting qualitySetting;
        if (Utils.getNetworkType() == NetworkType.MOBILE) {
            networkTypeMessage = str("revanced_remember_video_quality_mobile");
            qualitySetting = shortPlayerOpen ? shortsQualityMobile : videoQualityMobile;
        } else {
            networkTypeMessage = str("revanced_remember_video_quality_wifi");
            qualitySetting = shortPlayerOpen ? shortsQualityWifi : videoQualityWifi;
        }
        qualitySetting.save(qualityResolution);

        if (Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED_TOAST.get()) {
            String qualityLabel = qualityResolution == AUTOMATIC_VIDEO_QUALITY_VALUE
                    ? str("video_quality_quick_menu_auto_toast")
                    : qualityResolution + "p";
            Utils.showToastShort(str(
                    shortPlayerOpen
                            ? "revanced_remember_video_quality_toast_shorts"
                            : "revanced_remember_video_quality_toast",
                    networkTypeMessage,
                    qualityLabel)
            );
        }
    }

    /**
     * Injection point.
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(VideoQuality[] qualities, VideoQualityMenuInterface menu, int originalQualityIndex) {
        try {
            Utils.verifyOnMainThread();

            currentMenuInterface = menu; // Later used by player quality button.

            final boolean useShortsPreference = ShortsPlayerState.isOpen();
            final int preferredQuality = Utils.getNetworkType() == NetworkType.MOBILE
                    ? (useShortsPreference ? shortsQualityMobile : videoQualityMobile).get()
                    : (useShortsPreference ? shortsQualityWifi : videoQualityWifi).get();

            final boolean qualitiesChanged = videoQualities == null || videoQualities.size() != qualities.length;
            if (qualitiesChanged) {
                videoQualities = Arrays.asList(qualities);
                Logger.printDebug(() -> "VideoQualities: " + videoQualities);
            }

            if (!userChangedDefaultQuality && preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                return originalQualityIndex; // Nothing to do.
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                VideoQuality quality = videoQualities.get(userSelectedQualityIndex);
                Logger.printDebug(() -> "User changed default quality to: " + quality);
                changeDefaultQuality(quality.patch_getResolution());
                return userSelectedQualityIndex;
            }

            // After changing videos the qualities can initially be for the prior video.
            // If the qualities have changed and the default is not auto then an update is needed.
            if (!qualityNeedsUpdating && !qualitiesChanged) {
                return originalQualityIndex;
            }
            qualityNeedsUpdating = false;

            // Find the highest quality that is equal to or less than the preferred.
            VideoQuality qualityToUse = videoQualities.get(0); // First element is automatic mode.
            int qualityIndexToUse = 0;
            int i = 0;
            for (VideoQuality quality : videoQualities) {
                final int qualityResolution = quality.patch_getResolution();
                if (qualityResolution > qualityToUse.patch_getResolution() && qualityResolution <= preferredQuality) {
                    qualityToUse = quality;
                    qualityIndexToUse = i;
                    break;
                }
                i++;
            }

            // If the desired quality index is equal to the original index,
            // then the video is already set to the desired default quality.
            VideoQuality qualityToUseFinal = qualityToUse;
            if (qualityIndexToUse == originalQualityIndex) {
                Logger.printDebug(() -> "Video is already preferred quality: " + qualityToUseFinal);
            } else {
                Logger.printDebug(() -> "Changing video quality from: "
                        + videoQualities.get(originalQualityIndex) + " to: " + qualityToUseFinal);
            }

            // On first load of a new video, if the video is already the desired quality
            // then the quality flyout will show 'Auto' (ie: Auto (720p)).
            //
            // To prevent user confusion, set the video index even if the
            // quality is already correct so the UI picker will not display "Auto".
            menu.patch_setMenuIndexFromQuality(qualities[qualityIndexToUse]);

            return qualityIndexToUse;
        } catch (Exception ex) {
            Logger.printException(() -> "setVideoQuality failure", ex);
            return originalQualityIndex;
        }
    }

    /**
     * Injection point. Fixes bad data used by YouTube.
     */
    public static int fixVideoQualityResolution(String name, int quality) {
        final int correctQuality = 480;
        if (name.equals("480p") && quality != correctQuality) {
            Logger.printDebug(() -> "Fixing bad data of " + name + " from: " + quality
                    + " to: " + correctQuality);
            return correctQuality;
        }

        return quality;
    }

    /**
     * Injection point.
     * @param qualityIndex Element index of {@link #videoQualities}.
     */
    public static void userChangedQuality(int qualityIndex) {
        if (shouldRememberVideoQuality()) {
            userSelectedQualityIndex = qualityIndex;
            userChangedDefaultQuality = true;
        }
    }

    /**
     * Injection point.
     * @param videoResolution Human readable resolution: 480, 720, 1080.
     */
    public static void userChangedQualityInFlyout(int videoResolution) {
        Utils.verifyOnMainThread();
        if (!shouldRememberVideoQuality()) return;

        changeDefaultQuality(videoResolution);
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
        Utils.verifyOnMainThread();

        Logger.printDebug(() -> "newVideoStarted");
        qualityNeedsUpdating = true;
        videoQualities = null;
        currentMenuInterface = null;
    }

    /**
     * Shows a dialog with available video qualities, excluding Auto, with a title showing the current quality.
     */
    public static void showVideoQualityDialog(Context context) {
        try {
            List<VideoQuality> qualities = videoQualities;
            if (qualities == null) {
                Logger.printDebug(() -> "Cannot show qualities dialog, videoQualities is null");
                return;
            }
            VideoQualityMenuInterface menu = currentMenuInterface;
            if (menu == null) {
                Logger.printDebug(() -> "Cannot show qualities dialog, menu is null");
                return;
            }

            String currentQualityLabel = str("video_quality_quick_menu_auto_toast");
            final int currentQuality = getDefaultVideoQuality();
            int listViewSelectedIndex = -1;
            if (currentQuality != AUTOMATIC_VIDEO_QUALITY_VALUE) {
                int i = 0;
                for (VideoQuality quality : qualities) {
                    if (quality.patch_getResolution() == currentQuality) {
                        currentQualityLabel = quality.patch_getQualityName();
                        listViewSelectedIndex = i - 1; // Adjust for automatic quality at first index.
                        break;
                    }
                    i++;
                }
            }

            final int qualitySize = qualities.size();
            List<String> qualityLabels = new ArrayList<>(qualitySize);
            for (int i = 0; i < qualitySize; i++) {
                VideoQuality quality = qualities.get(i);
                if (quality.patch_getResolution() != AUTOMATIC_VIDEO_QUALITY_VALUE) {
                    qualityLabels.add(quality.patch_getQualityName());
                }
            }

            if (qualityLabels.isEmpty()) {
                Logger.printException(() -> "Video has no available qualities");
                return;
            }

            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            final int dip4 = dipToPixels(4);   // Height for handle bar.
            final int dip5 = dipToPixels(5);   // Padding for mainLayout.
            final int dip6 = dipToPixels(6);   // Bottom margin.
            final int dip8 = dipToPixels(8);   // Side padding.
            final int dip16 = dipToPixels(16); // Left padding for ListView.
            final int dip20 = dipToPixels(20); // Margin below handle.
            final int dip40 = dipToPixels(40); // Width for handle bar.

            LinearLayout mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(dip5, dip8, dip5, dip8);

            ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                    Utils.createCornerRadii(12), null, null));
            background.getPaint().setColor(Utils.getDialogBackgroundColor());
            mainLayout.setBackground(background);

            View handleBar = new View(context);
            ShapeDrawable handleBackground = new ShapeDrawable(new RoundRectShape(
                    Utils.createCornerRadii(4), null, null));
            handleBackground.getPaint().setColor(getAdjustedHandleBarBackgroundColor());
            handleBar.setBackground(handleBackground);
            LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dip40, dip4);
            handleParams.gravity = Gravity.CENTER_HORIZONTAL;
            handleParams.setMargins(0, 0, 0, dip20);
            handleBar.setLayoutParams(handleParams);
            mainLayout.addView(handleBar);

            // Create SpannableStringBuilder for formatted text.
            SpannableStringBuilder spannableTitle = new SpannableStringBuilder();
            String titlePart = str("video_quality_quick_menu_title");
            String separatorPart = str("video_quality_title_seperator");

            // Append title part with default foreground color.
            spannableTitle.append(titlePart);
            spannableTitle.setSpan(
                    new ForegroundColorSpan(Utils.getAppForegroundColor()),
                    0,
                    titlePart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableTitle.append("   "); // Space after title.

            // Append separator part with adjusted title color.
            int separatorStart = spannableTitle.length();
            spannableTitle.append(separatorPart);
            spannableTitle.setSpan(
                    new ForegroundColorSpan(getAdjustedTitleForegroundColor()),
                    separatorStart,
                    separatorStart + separatorPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableTitle.append("   "); // Space after separator.

            // Append quality label with adjusted title color.
            final int qualityStart = spannableTitle.length();
            spannableTitle.append(currentQualityLabel);
            spannableTitle.setSpan(
                    new ForegroundColorSpan(getAdjustedTitleForegroundColor()),
                    qualityStart,
                    qualityStart + currentQualityLabel.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Add title with current quality.
            TextView titleView = new TextView(context);
            titleView.setText(spannableTitle);
            titleView.setTextSize(16);
            // Remove setTextColor since color is handled by SpannableStringBuilder.
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(dip8, 0, 0, dip20);
            titleView.setLayoutParams(titleParams);
            mainLayout.addView(titleView);

            ListView listView = new ListView(context);
            CustomQualityAdapter adapter = new CustomQualityAdapter(context, qualityLabels);
            adapter.setSelectedPosition(listViewSelectedIndex);
            listView.setAdapter(adapter);
            listView.setDivider(null);
            listView.setPadding(dip16, 0, 0, 0);

            listView.setOnItemClickListener((parent, view, which, id) -> {
                try {
                    final int originalIndex = which + 1;
                    VideoQuality selectedQuality = qualities.get(originalIndex);
                    menu.patch_setMenuIndexFromQuality(selectedQuality);
                    Logger.printDebug(() -> "Applied dialog quality: " + selectedQuality + " index: " + originalIndex);

                    if (shouldRememberVideoQuality()) {
                        changeDefaultQuality(selectedQuality.patch_getResolution());
                    }

                    dialog.dismiss();
                } catch (Exception ex) {
                    Logger.printException(() -> "Video quality selection failure", ex);
                }
            });

            LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            listViewParams.setMargins(0, 0, 0, dip5);
            listView.setLayoutParams(listViewParams);
            mainLayout.addView(listView);

            LinearLayout wrapperLayout = new LinearLayout(context);
            wrapperLayout.setOrientation(LinearLayout.VERTICAL);
            wrapperLayout.setPadding(dip8, 0, dip8, 0);
            wrapperLayout.addView(mainLayout);
            dialog.setContentView(wrapperLayout);

            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.BOTTOM;
                params.y = dip6;
                int portraitWidth = context.getResources().getDisplayMetrics().widthPixels;
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    portraitWidth = Math.min(
                            portraitWidth,
                            context.getResources().getDisplayMetrics().heightPixels);
                }
                params.width = portraitWidth;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(params);
                window.setBackgroundDrawable(null);
            }

            final int fadeDurationFast = Utils.getResourceInteger("fade_duration_fast");
            Animation slideInABottomAnimation = Utils.getResourceAnimation("slide_in_bottom");
            slideInABottomAnimation.setDuration(fadeDurationFast);
            mainLayout.startAnimation(slideInABottomAnimation);

            // noinspection ClickableViewAccessibility
            mainLayout.setOnTouchListener(new View.OnTouchListener() {
                final float dismissThreshold = dipToPixels(100);
                float touchY;
                float translationY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchY = event.getRawY();
                            translationY = mainLayout.getTranslationY();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            final float deltaY = event.getRawY() - touchY;
                            if (deltaY >= 0) {
                                mainLayout.setTranslationY(translationY + deltaY);
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (mainLayout.getTranslationY() > dismissThreshold) {
                                //noinspection ExtractMethodRecommender
                                final float remainingDistance = context.getResources().getDisplayMetrics().heightPixels
                                        - mainLayout.getTop();
                                TranslateAnimation slideOut = new TranslateAnimation(
                                        0, 0, mainLayout.getTranslationY(), remainingDistance);
                                slideOut.setDuration(fadeDurationFast);
                                slideOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {}
                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        dialog.dismiss();
                                    }
                                    @Override
                                    public void onAnimationRepeat(Animation animation) {}
                                });
                                mainLayout.startAnimation(slideOut);
                            } else {
                                TranslateAnimation slideBack = new TranslateAnimation(
                                        0, 0, mainLayout.getTranslationY(), 0);
                                slideBack.setDuration(fadeDurationFast);
                                mainLayout.startAnimation(slideBack);
                                mainLayout.setTranslationY(0);
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });

            dialog.show();
        } catch (Exception ex) {
            Logger.printException(() -> "showVideoQualityDialog failure", ex);
        }
    }

    public static class CustomQualityAdapter extends ArrayAdapter<String> {
        private int selectedPosition = -1;

        public CustomQualityAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, 0, objects);
        }

        public void setSelectedPosition(int position) {
            this.selectedPosition = position;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        Utils.getResourceIdentifier("revanced_custom_list_item_checked", "layout"),
                        parent,
                        false
                );
                viewHolder = new ViewHolder();
                viewHolder.checkIcon = convertView.findViewById(
                        Utils.getResourceIdentifier("revanced_check_icon", "id")
                );
                viewHolder.placeholder = convertView.findViewById(
                        Utils.getResourceIdentifier("revanced_check_icon_placeholder", "id")
                );
                viewHolder.textView = convertView.findViewById(
                        Utils.getResourceIdentifier("revanced_item_text", "id")
                );
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textView.setText(getItem(position));
            final boolean isSelected = position == selectedPosition;
            viewHolder.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            viewHolder.placeholder.setVisibility(isSelected ? View.GONE : View.INVISIBLE);

            return convertView;
        }

        private static class ViewHolder {
            ImageView checkIcon;
            View placeholder;
            TextView textView;
        }
    }

    public static int getAdjustedHandleBarBackgroundColor() {
        final int baseColor = Utils.getDialogBackgroundColor();
        return Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(baseColor, 1.25f)
                : Utils.adjustColorBrightness(baseColor, 0.9f);
    }

    public static int getAdjustedTitleForegroundColor() {
        final int baseColor = Utils.getAppForegroundColor();
        return Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(baseColor, 0.6f)
                : Utils.adjustColorBrightness(baseColor, 1.6f);
    }
}
