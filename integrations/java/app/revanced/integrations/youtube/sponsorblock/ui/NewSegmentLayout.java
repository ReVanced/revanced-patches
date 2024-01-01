package app.revanced.integrations.youtube.sponsorblock.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.shared.Logger;

import static app.revanced.integrations.shared.Utils.getResourceDimensionPixelSize;
import static app.revanced.integrations.shared.Utils.getResourceIdentifier;

public final class NewSegmentLayout extends FrameLayout {
    private static final ColorStateList rippleColorStateList = new ColorStateList(
            new int[][]{new int[]{android.R.attr.state_enabled}},
            new int[]{0x33ffffff} // sets the ripple color to white
    );
    private final int rippleEffectId;

    final int defaultBottomMargin;
    final int ctaBottomMargin;

    public NewSegmentLayout(final Context context) {
        this(context, null);
    }

    public NewSegmentLayout(final Context context, final AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NewSegmentLayout(final Context context, final AttributeSet attributeSet, final int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public NewSegmentLayout(final Context context, final AttributeSet attributeSet,
                            final int defStyleAttr, final int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(
                getResourceIdentifier(context, "new_segment", "layout"), this, true
        );

        TypedValue rippleEffect = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleEffect, true);
        rippleEffectId = rippleEffect.resourceId;

        initializeButton(
                context,
                "sb_new_segment_rewind",
                () -> VideoInformation.seekToRelative(-Settings.SB_CREATE_NEW_SEGMENT_STEP.get()),
                "Rewind button clicked"
        );

        initializeButton(
                context,
                "sb_new_segment_forward",
                () -> VideoInformation.seekToRelative(Settings.SB_CREATE_NEW_SEGMENT_STEP.get()),
                "Forward button clicked"
        );

        initializeButton(
                context,
                "sb_new_segment_adjust",
                SponsorBlockUtils::onMarkLocationClicked,
                "Adjust button clicked"
        );

        initializeButton(
                context,
                "sb_new_segment_compare",
                SponsorBlockUtils::onPreviewClicked,
                "Compare button clicked"
        );

        initializeButton(
                context,
                "sb_new_segment_edit",
                SponsorBlockUtils::onEditByHandClicked,
                "Edit button clicked"
        );

        initializeButton(
                context,
                "sb_new_segment_publish",
                SponsorBlockUtils::onPublishClicked,
                "Publish button clicked"
        );

        defaultBottomMargin = getResourceDimensionPixelSize("brand_interaction_default_bottom_margin");
        ctaBottomMargin = getResourceDimensionPixelSize("brand_interaction_cta_bottom_margin");
    }

    /**
     * Initializes a segment button with the given resource identifier name with the given handler and a ripple effect.
     *
     * @param context                The context.
     * @param resourceIdentifierName The resource identifier name for the button.
     * @param handler                The handler for the button's click event.
     * @param debugMessage           The debug message to print when the button is clicked.
     */
    private void initializeButton(final Context context, final String resourceIdentifierName,
                                  final ButtonOnClickHandlerFunction handler, final String debugMessage) {
        final ImageButton button = findViewById(getResourceIdentifier(context, resourceIdentifierName, "id"));

        // Add ripple effect
        button.setBackgroundResource(rippleEffectId);
        RippleDrawable rippleDrawable = (RippleDrawable) button.getBackground();
        rippleDrawable.setColor(rippleColorStateList);

        button.setOnClickListener((v) -> {
            handler.apply();
            Logger.printDebug(() -> debugMessage);
        });
    }

    @FunctionalInterface
    public interface ButtonOnClickHandlerFunction {
        void apply();
    }
}
