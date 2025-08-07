package app.revanced.extension.youtube.videoplayer;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.youtube.patches.VideoInformation.AUTOMATIC_VIDEO_QUALITY_VALUE;
import static app.revanced.extension.youtube.patches.VideoInformation.VIDEO_QUALITY_1080P_PREMIUM_NAME;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.quality.RememberVideoQualityPatch;
import app.revanced.extension.youtube.settings.Settings;
import kotlin.Unit;

@SuppressWarnings("unused")
public class VideoQualityDialogButton {

    @Nullable
    private static PlayerControlButton instance;

    @Nullable
    private static CharSequence currentOverlayText;

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_video_quality_dialog_button",
                    "revanced_video_quality_dialog_button_placeholder",
                    "revanced_video_quality_dialog_button_text",
                    Settings.VIDEO_QUALITY_DIALOG_BUTTON::get,
                    view -> {
                        try {
                            showVideoQualityDialog(view.getContext());
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button onClick failure", ex);
                        }
                    },
                    view -> {
                        try {
                            VideoQuality[] qualities = VideoInformation.getCurrentQualities();
                            if (qualities == null) {
                                Logger.printDebug(() -> "Cannot reset quality, videoQualities is null");
                                return true;
                            }

                            // Reset to default quality.
                            final int defaultResolution = RememberVideoQualityPatch.getDefaultQualityResolution();
                            for (VideoQuality quality : qualities) {
                                final int resolution = quality.patch_getResolution();
                                if (resolution != AUTOMATIC_VIDEO_QUALITY_VALUE && resolution <= defaultResolution) {
                                    Logger.printDebug(() -> "Resetting quality to: " + quality);
                                    VideoInformation.changeQuality(quality);
                                    return true;
                                }
                            }

                            // Existing hook cannot set default quality to auto.
                            // Instead show the quality dialog.
                            showVideoQualityDialog(view.getContext());
                            return true;
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button reset failure", ex);
                        }
                        return false;
                    }
            );

            // Set initial text.
            updateButtonText(VideoInformation.getCurrentQuality());

            VideoInformation.onQualityChange.addObserver((@Nullable VideoQuality quality) -> {
                updateButtonText(quality);
                return Unit.INSTANCE;
            });
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) {
            instance.setVisibilityImmediate(visible);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) {
            instance.setVisibility(visible, animated);
        }
    }

    /**
     * Updates the button text based on the current video quality.
     */
    public static void updateButtonText(@Nullable VideoQuality quality) {
        try {
            Utils.verifyOnMainThread();
            if (instance == null) return;

            final int resolution = quality == null
                    ? AUTOMATIC_VIDEO_QUALITY_VALUE // Video is still loading.
                    : quality.patch_getResolution();

            SpannableStringBuilder text = new SpannableStringBuilder();
            String qualityText = switch (resolution) {
                case AUTOMATIC_VIDEO_QUALITY_VALUE -> "";
                case 144, 240, 360 -> "LD";
                case 480  -> "SD";
                case 720  -> "HD";
                case 1080 -> "FHD";
                case 1440 -> "QHD";
                case 2160 -> "4K";
                default   -> "?"; // Should never happen.
            };

            text.append(qualityText);
            if (resolution == 1080 && VIDEO_QUALITY_1080P_PREMIUM_NAME.equals(quality.patch_getQualityName())) {
                // Underline the entire "FHD" text for 1080p Premium.
                text.setSpan(new UnderlineSpan(), 0, qualityText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            currentOverlayText = text;
            Utils.runOnMainThreadDelayed(() -> {
                if (currentOverlayText != text) {
                    Logger.printDebug(() -> "Ignoring stale button text update of: " + text);
                    return;
                }
                instance.setTextOverlay(text);
            }, 100);
        } catch (Exception ex) {
            Logger.printException(() -> "updateButtonText failure", ex);
        }
    }

    /**
     * Shows a dialog with available video qualities, excluding Auto, with a title showing the current quality.
     */
    private static void showVideoQualityDialog(Context context) {
        try {
            VideoQuality[] currentQualities = VideoInformation.getCurrentQualities();
            VideoQuality currentQuality = VideoInformation.getCurrentQuality();
            if (currentQualities == null || currentQuality == null) {
                Logger.printDebug(() -> "Cannot show qualities dialog, videoQualities is null");
                return;
            }
            if (currentQualities.length < 2) {
                // Should never happen.
                Logger.printException(() -> "Cannot show qualities dialog, no qualities available");
                return;
            }

            // -1 adjustment for automatic quality at first index.
            int listViewSelectedIndex = -1;
            for (VideoQuality quality : currentQualities) {
                if (quality.patch_getQualityName().equals(currentQuality.patch_getQualityName())) {
                    break;
                }
                listViewSelectedIndex++;
            }

            List<String> qualityLabels = new ArrayList<>(currentQualities.length - 1);
            for (VideoQuality availableQuality : currentQualities) {
                if (availableQuality.patch_getResolution() != AUTOMATIC_VIDEO_QUALITY_VALUE) {
                    qualityLabels.add(availableQuality.patch_getQualityName());
                }
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
            final int baseColor = Utils.getDialogBackgroundColor();
            final int adjustedHandleBarBackgroundColor = Utils.adjustColorBrightness(
                    baseColor, 0.9f, 1.25f);
            handleBackground.getPaint().setColor(adjustedHandleBarBackgroundColor);
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
            final int adjustedTitleForegroundColor = Utils.adjustColorBrightness(
                    Utils.getAppForegroundColor(), 1.6f, 0.6f);
            spannableTitle.setSpan(
                    new ForegroundColorSpan(adjustedTitleForegroundColor),
                    separatorStart,
                    separatorStart + separatorPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableTitle.append("   "); // Space after separator.

            // Append quality label with adjusted title color.
            final int qualityStart = spannableTitle.length();
            spannableTitle.append(currentQuality.patch_getQualityName());
            spannableTitle.setSpan(
                    new ForegroundColorSpan(adjustedTitleForegroundColor),
                    qualityStart,
                    qualityStart + currentQuality.patch_getQualityName().length(),
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
                    final int originalIndex = which + 1; // Adjust for automatic.
                    VideoQuality selectedQuality = currentQualities[originalIndex];
                    RememberVideoQualityPatch.userChangedQuality(selectedQuality.patch_getResolution());
                    VideoInformation.changeQuality(selectedQuality);

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
                    // Limit height in landscape mode.
                    params.height = Utils.percentageHeightToPixels(80);
                } else {
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                }
                params.width = portraitWidth;
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

    private static class CustomQualityAdapter extends ArrayAdapter<String> {
        private static final int CUSTOM_LIST_ITEM_CHECKED_ID = Utils.getResourceIdentifier(
                "revanced_custom_list_item_checked", "layout");
        private static final int CHECK_ICON_ID = Utils.getResourceIdentifier(
                "revanced_check_icon", "id");
        private static final int CHECK_ICON_PLACEHOLDER_ID = Utils.getResourceIdentifier(
                "revanced_check_icon_placeholder", "id");
        private static final int ITEM_TEXT_ID = Utils.getResourceIdentifier(
                "revanced_item_text", "id");

        private int selectedPosition = -1;

        public CustomQualityAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, 0, objects);
        }

        private void setSelectedPosition(int position) {
            this.selectedPosition = position;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        CUSTOM_LIST_ITEM_CHECKED_ID,
                        parent,
                        false
                );
                viewHolder = new ViewHolder();
                viewHolder.checkIcon = convertView.findViewById(CHECK_ICON_ID);
                viewHolder.placeholder = convertView.findViewById(CHECK_ICON_PLACEHOLDER_ID);
                viewHolder.textView = convertView.findViewById(ITEM_TEXT_ID);
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
}
