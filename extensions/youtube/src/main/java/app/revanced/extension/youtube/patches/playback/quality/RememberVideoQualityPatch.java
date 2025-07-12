package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.shared.Utils.showToastShort;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.IntegerSetting;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;
import app.revanced.extension.youtube.videoplayer.VideoQualityDialogButton;

@SuppressWarnings("unused")
public class RememberVideoQualityPatch {
    private static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;
    private static final IntegerSetting videoQualityWifi = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting videoQualityMobile = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;
    private static final IntegerSetting shortsQualityWifi = Settings.SHORTS_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting shortsQualityMobile = Settings.SHORTS_QUALITY_DEFAULT_MOBILE;

    private static boolean qualityNeedsUpdating;

    /**
     * If the user selected a new quality from the flyout menu,
     * and {@link Settings#REMEMBER_VIDEO_QUALITY_LAST_SELECTED} is enabled.
     */
    private static boolean userChangedDefaultQuality;

    /**
     * Index of the video quality chosen by the user from the flyout menu.
     */
    private static int userSelectedQualityIndex;

    /**
     * The available qualities of the current video in human readable form: [1080, 720, 480]
     */
    @Nullable
    private static List<Integer> videoQualities;

    /**
     * Mapping of filtered quality indices (used in dialog) to original quality indices.
     */
    @Nullable
    private static Map<Integer, Integer> filteredToOriginalIndexMap;

    /**
     * Quality interface and method for setting quality.
     */
    private static Object qInterface;
    private static String qIndexMethod;

    /**
     * Tracks the last applied quality index.
     */
    private static int lastAppliedQualityIndex = -1; // Initialize to -1 (invalid index)

    /**
     * Getter for lastAppliedQualityIndex.
     */
    public static int getLastAppliedQualityIndex() {
        return lastAppliedQualityIndex;
    }

    /**
     * Getter for videoQualities.
     */
    @Nullable
    public static List<Integer> getVideoQualities() {
        return videoQualities;
    }

    private static boolean shouldRememberVideoQuality() {
        BooleanSetting preference = ShortsPlayerState.isOpen() ?
                Settings.REMEMBER_SHORTS_QUALITY_LAST_SELECTED
                : Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED;
        return preference.get();
    }

    private static void changeDefaultQuality(int defaultQuality) {
        String networkTypeMessage;
        boolean useShortsPreference = ShortsPlayerState.isOpen();
        if (Utils.getNetworkType() == Utils.NetworkType.MOBILE) {
            if (useShortsPreference) shortsQualityMobile.save(defaultQuality);
            else videoQualityMobile.save(defaultQuality);
            networkTypeMessage = str("revanced_remember_video_quality_mobile");
        } else {
            if (useShortsPreference) shortsQualityWifi.save(defaultQuality);
            else videoQualityWifi.save(defaultQuality);
            networkTypeMessage = str("revanced_remember_video_quality_wifi");
        }
        if (Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED_TOAST.get())
            showToastShort(str(
                    useShortsPreference ? "revanced_remember_video_quality_toast_shorts" : "revanced_remember_video_quality_toast",
                    networkTypeMessage, (defaultQuality + "p")
            ));
    }

    /**
     * Injection point.
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(Object[] qualities, final int originalQualityIndex,
                                      Object qInterfaceArg, String qIndexMethodArg) {
        try {
            // Store qInterface and qIndexMethod for use in dialog.
            qInterface = qInterfaceArg;
            qIndexMethod = qIndexMethodArg;

            boolean useShortsPreference = ShortsPlayerState.isOpen();
            final int preferredQuality = Utils.getNetworkType() == Utils.NetworkType.MOBILE
                    ? (useShortsPreference ? shortsQualityMobile : videoQualityMobile).get()
                    : (useShortsPreference ? shortsQualityWifi : videoQualityWifi).get();

            if (!userChangedDefaultQuality && preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                lastAppliedQualityIndex = originalQualityIndex;
                return originalQualityIndex; // Nothing to do.
            }

            if (videoQualities == null || videoQualities.size() != qualities.length) {
                videoQualities = new ArrayList<>(qualities.length);
                filteredToOriginalIndexMap = new HashMap<>();
                Set<Integer> seenQualities = new LinkedHashSet<>(); // Maintains insertion order.
                int filteredIndex = 0;

                for (int i = 0; i < qualities.length; i++) {
                    Object streamQuality = qualities[i];
                    for (Field field : streamQuality.getClass().getFields()) {
                        if (field.getType().isAssignableFrom(Integer.TYPE)
                                && field.getName().length() <= 2) {
                            int quality = field.getInt(streamQuality);
                            if (quality == AUTOMATIC_VIDEO_QUALITY_VALUE || quality > 0) {
                                videoQualities.add(quality);
                                if (seenQualities.add(quality)) {
                                    filteredToOriginalIndexMap.put(filteredIndex++, i);
                                }
                            }
                        }
                    }
                }

                // After changing videos the qualities can initially be for the prior video.
                // So if the qualities have changed an update is needed.
                qualityNeedsUpdating = true;
                Logger.printDebug(() -> "VideoQualities: " + videoQualities + ", IndexMap: " + filteredToOriginalIndexMap);
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                final int quality = videoQualities.get(userSelectedQualityIndex);
                Logger.printDebug(() -> "User changed default quality to: " + quality);
                changeDefaultQuality(quality);
                lastAppliedQualityIndex = userSelectedQualityIndex;
                return userSelectedQualityIndex;
            }

            if (!qualityNeedsUpdating) {
                lastAppliedQualityIndex = originalQualityIndex;
                return originalQualityIndex;
            }
            qualityNeedsUpdating = false;

            // Find the highest quality that is equal to or less than the preferred.
            int qualityToUse = videoQualities.get(0); // first element is automatic mode
            int qualityIndexToUse = 0;
            int i = 0;
            for (Integer quality : videoQualities) {
                if (quality <= preferredQuality && qualityToUse < quality) {
                    qualityToUse = quality;
                    qualityIndexToUse = i;
                }
                i++;
            }

            // If the desired quality index is equal to the original index,
            // then the video is already set to the desired default quality.
            final int qualityToUseFinal = qualityToUse;
            if (qualityIndexToUse == originalQualityIndex) {
                // On first load of a new video, if the UI video quality flyout menu
                // is not updated then it will still show 'Auto' (ie: Auto (480p)),
                // even though it's already set to the desired resolution.
                //
                // To prevent confusion, set the video index anyways (even if it matches the existing index)
                // as that will force the UI picker to not display "Auto".
                Logger.printDebug(() -> "Video is already preferred quality: " + qualityToUseFinal);
            } else {
                Logger.printDebug(() -> "Changing video quality from: "
                        + videoQualities.get(originalQualityIndex) + " to: " + qualityToUseFinal);
            }

            Method m = qInterface.getClass().getMethod(qIndexMethod, Integer.TYPE);
            m.invoke(qInterface, qualityToUse);
            lastAppliedQualityIndex = qualityIndexToUse;
            VideoQualityDialogButton.updateButtonIcon();
            return qualityIndexToUse;
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to set quality", ex);
            lastAppliedQualityIndex = originalQualityIndex; // Fallback to original index
            return originalQualityIndex;
        }
    }

    /**
     * Injection point.  Old quality menu.
     */
    public static void userChangedQuality(int selectedQualityIndex) {
        if (shouldRememberVideoQuality()) {
            userSelectedQualityIndex = selectedQualityIndex;
            userChangedDefaultQuality = true;
            VideoQualityDialogButton.updateButtonIcon();
        }
    }

    /**
     * Injection point.  New quality menu.
     */
    public static void userChangedQualityInNewFlyout(int selectedQuality) {
        if (!shouldRememberVideoQuality()) return;

        changeDefaultQuality(selectedQuality); // Quality is human readable resolution (ie: 1080).
        VideoQualityDialogButton.updateButtonIcon();
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
        Logger.printDebug(() -> "newVideoStarted");
        qualityNeedsUpdating = true;
        videoQualities = null;
        filteredToOriginalIndexMap = null;
        lastAppliedQualityIndex = -1; // Reset on new video.
    }

    /**
     * Shows a dialog with available video qualities, excluding duplicates.
     */
    public static void showVideoQualityDialog(@NonNull Context context) {
        try {
            if (videoQualities == null || videoQualities.isEmpty()) {
                showToastShort(str("revanced_video_quality_no_qualities_available"));
                return;
            }

            // Create dialog without a theme for custom appearance.
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            // Preset size constants.
            final int dip4 = dipToPixels(4);   // Height for handle bar.
            final int dip5 = dipToPixels(5);   // Padding for mainLayout.
            final int dip6 = dipToPixels(6);   // Bottom margin.
            final int dip8 = dipToPixels(8);   // Side padding.
            final int dip20 = dipToPixels(20); // Margin below handle.
            final int dip40 = dipToPixels(40); // Width for handle bar.

            // Create main vertical LinearLayout.
            LinearLayout mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(dip5, dip8, dip5, dip8);

            // Set rounded rectangle background.
            ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                    Utils.createCornerRadii(12), null, null));
            background.getPaint().setColor(Utils.getDialogBackgroundColor());
            mainLayout.setBackground(background);

            // Add handle bar at the top.
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

            // Prepare dialog items, removing duplicates.
            List<String> qualityLabels = new ArrayList<>();
            List<Integer> filteredQualities = new ArrayList<>();
            Set<Integer> seenQualities = new LinkedHashSet<>();
            int filteredIndex = 0;
            for (Integer quality : videoQualities) {
                if (quality != AUTOMATIC_VIDEO_QUALITY_VALUE && seenQualities.add(quality)) {
                    String label = quality + "p";
                    qualityLabels.add(label);
                    filteredQualities.add(quality);
                    filteredToOriginalIndexMap.put(filteredIndex++, videoQualities.indexOf(quality));
                }
            }

            // Determine pre-selected quality index.
            int selectedIndex;
            if (lastAppliedQualityIndex >= 0 && lastAppliedQualityIndex < videoQualities.size()) {
                int originalQuality = videoQualities.get(lastAppliedQualityIndex);
                selectedIndex = filteredQualities.indexOf(originalQuality);
            } else {
                int preferredQuality = Utils.getNetworkType() == Utils.NetworkType.MOBILE
                        ? videoQualityMobile.get()
                        : videoQualityWifi.get();
                selectedIndex = filteredQualities.indexOf(preferredQuality);
                if (selectedIndex < 0) selectedIndex = 0;
            }

            // Create ListView for quality options.
            ListView listView = new ListView(context);
            CustomQualityAdapter adapter = new CustomQualityAdapter(context, qualityLabels);
            adapter.setSelectedPosition(selectedIndex);
            listView.setAdapter(adapter);
            listView.setDivider(null);
            listView.setPadding(0, 0, 0, 0);

            // Handle item click.
            listView.setOnItemClickListener((parent, view, which, id) -> {
                try {
                    int selectedQuality = filteredQualities.get(which);
                    int originalIndex = filteredToOriginalIndexMap.get(filteredQualities.indexOf(selectedQuality));
                    if (qInterface != null && qIndexMethod != null) {
                        Method m = qInterface.getClass().getMethod(qIndexMethod, Integer.TYPE);
                        m.invoke(qInterface, selectedQuality);
                        lastAppliedQualityIndex = originalIndex;
                        VideoQualityDialogButton.updateButtonIcon();
                        Logger.printDebug(() -> "Applied dialog quality: " + selectedQuality + " (original index: " + originalIndex + ")");
                    } else {
                        Logger.printDebug(() -> "Cannot apply quality: qInterface or qIndexMethod is null");
                        showToastShort(str("revanced_video_quality_apply_failed"));
                    }

                    // Update saved setting if remembrance is enabled.
                    if (shouldRememberVideoQuality()) {
                        changeDefaultQuality(selectedQuality);
                    }

                    showToastShort(str("revanced_video_quality_selected_toast", qualityLabels.get(which)));
                    dialog.dismiss();
                } catch (Exception ex) {
                    Logger.printException(() -> "Video quality selection failure", ex);
                    showToastShort(str("revanced_video_quality_apply_failed"));
                }
            });

            // Add ListView to main layout.
            LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            listViewParams.setMargins(0, 0, 0, dip5);
            listView.setLayoutParams(listViewParams);
            mainLayout.addView(listView);

            // Wrap mainLayout in another LinearLayout for side margins.
            LinearLayout wrapperLayout = new LinearLayout(context);
            wrapperLayout.setOrientation(LinearLayout.VERTICAL);
            wrapperLayout.setPadding(dip8, 0, dip8, 0);
            wrapperLayout.addView(mainLayout);
            dialog.setContentView(wrapperLayout);

            // Configure dialog window.
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

            // Apply slide-in animation.
            final int fadeDurationFast = Utils.getResourceInteger("fade_duration_fast");
            Animation slideInABottomAnimation = Utils.getResourceAnimation("slide_in_bottom");
            slideInABottomAnimation.setDuration(fadeDurationFast);
            mainLayout.startAnimation(slideInABottomAnimation);

            // Set touch listener for drag-to-dismiss.
            mainLayout.setOnTouchListener(new View.OnTouchListener() {
                final float dismissThreshold = Utils.dipToPixels(100);
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

            // Set text.
            viewHolder.textView.setText(getItem(position));

            // Show check icon for selected item.
            boolean isSelected = position == selectedPosition;
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

    /**
     * Adjusts the HandleBar background color based on the current theme.
     */
    public static int getAdjustedHandleBarBackgroundColor() {
        final int baseColor = Utils.getDialogBackgroundColor();
        return Utils.isDarkModeEnabled()
                ? Utils.adjustColorBrightness(baseColor, 1.25f)  // Lighten for dark theme.
                : Utils.adjustColorBrightness(baseColor, 0.9f); // Darken for light theme.
    }
}
