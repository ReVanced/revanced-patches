package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.shared.Utils.getResourceColor;
import static app.revanced.extension.shared.Utils.getResourceDimension;
import static app.revanced.extension.shared.Utils.getResourceDimensionPixelSize;
import static app.revanced.extension.shared.Utils.getResourceIdentifier;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Objects;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.objects.SponsorSegment;

public class SkipSponsorButton extends FrameLayout {
    /**
     * Adds a high contrast border around the skip button.
     *
     * This feature is not currently used.
     * If this is added, it needs an additional button width change because
     * as-is the skip button text is clipped when this is on.
     */
    private static final boolean highContrast = false;
    private final LinearLayout skipSponsorBtnContainer;
    private final TextView skipSponsorTextView;
    private final Paint background;
    private final Paint border;
    private SponsorSegment segment;
    final int defaultBottomMargin;
    final int ctaBottomMargin;

    public SkipSponsorButton(Context context) {
        this(context, null);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(getResourceIdentifierOrThrow(context,
                "revanced_sb_skip_sponsor_button", "layout"), this, true);  // layout:skip_ad_button
        setMinimumHeight(getResourceDimensionPixelSize("ad_skip_ad_button_min_height"));  // dimen:ad_skip_ad_button_min_height
        skipSponsorBtnContainer = Objects.requireNonNull(findViewById(getResourceIdentifierOrThrow(
                context, "revanced_sb_skip_sponsor_button_container", "id")));  // id:skip_ad_button_container

        background = new Paint();
        background.setColor(getResourceColor("skip_ad_button_background_color"));  // color:skip_ad_button_background_color);
        background.setStyle(Paint.Style.FILL);

        border = new Paint();
        border.setColor(getResourceColor("skip_ad_button_border_color"));  // color:skip_ad_button_border_color);
        border.setStrokeWidth(getResourceDimension("ad_skip_ad_button_border_width"));  // dimen:ad_skip_ad_button_border_width);
        border.setStyle(Paint.Style.STROKE);

        skipSponsorTextView = Objects.requireNonNull(findViewById(getResourceIdentifier(context, "revanced_sb_skip_sponsor_button_text", "id")));  // id:skip_ad_button_text;
        defaultBottomMargin = getResourceDimensionPixelSize("skip_button_default_bottom_margin");  // dimen:skip_button_default_bottom_margin
        ctaBottomMargin = getResourceDimensionPixelSize("skip_button_cta_bottom_margin");  // dimen:skip_button_cta_bottom_margin

        updateLayout();

        skipSponsorBtnContainer.setOnClickListener(v -> {
            // The view controller handles hiding this button, but hide it here as well just in case something goofs.
            setVisibility(View.GONE);
            SegmentPlaybackController.onSkipSegmentClicked(segment);
        });
    }

    @Override  // android.view.ViewGroup
    protected final void dispatchDraw(Canvas canvas) {
        final int left = skipSponsorBtnContainer.getLeft();
        final int top = skipSponsorBtnContainer.getTop();
        final int right = left + skipSponsorBtnContainer.getWidth();
        final int bottom = top + skipSponsorBtnContainer.getHeight();

        // Determine corner radius for rounded button
        float cornerRadius = skipSponsorBtnContainer.getHeight() / 2f;

        if (Settings.SB_SQUARE_LAYOUT.get()) {
            // Square button.
            canvas.drawRect(left, top, right, bottom, background);
            if (highContrast) {
                canvas.drawLines(new float[]{
                                right, top, left, top,
                                left, top, left, bottom,
                                left, bottom, right, bottom},
                        border); // Draw square border.
            }
        } else {
            // Rounded button.
            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, background); // Draw rounded background.
            if (highContrast) {
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, border); // Draw rounded border.
            }
        }

        super.dispatchDraw(canvas);
    }

    /**
     * Update the layout of this button.
     */
    public void updateLayout() {
        if (Settings.SB_SQUARE_LAYOUT.get()) {
            // No padding for square corners.
            setPadding(0, 0, 0, 0);
        } else {
            // Apply padding for rounded corners.
            final int padding = SponsorBlockViewController.ROUNDED_LAYOUT_MARGIN;
            setPadding(padding, 0, padding, 0);
        }
    }

    public void updateSkipButtonText(@NonNull SponsorSegment segment) {
        this.segment = segment;
        CharSequence newText = segment.getSkipButtonText();

        //noinspection StringEqualsCharSequence
        if (newText.equals(skipSponsorTextView.getText())) {
            return;
        }
        skipSponsorTextView.setText(newText);
    }
}
