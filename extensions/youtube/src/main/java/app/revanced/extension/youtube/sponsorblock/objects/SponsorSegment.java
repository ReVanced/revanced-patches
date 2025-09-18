package app.revanced.extension.youtube.sponsorblock.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.shared.StringRef;

import java.util.Objects;

import static app.revanced.extension.shared.StringRef.sf;

import android.util.Range;

public class SponsorSegment implements Comparable<SponsorSegment> {

    public enum SegmentVote {
        UPVOTE(sf("revanced_sb_vote_upvote"), 1,false),
        DOWNVOTE(sf("revanced_sb_vote_downvote"), 0, true),
        CATEGORY_CHANGE(sf("revanced_sb_vote_category"), -1, true); // ApiVoteType is not used for category change.

        public static final SegmentVote[] voteTypesWithoutCategoryChange = {
                UPVOTE,
                DOWNVOTE,
        };

        @NonNull
        public final StringRef title;
        public final int apiVoteType;
        /**
         * If the option should be highlighted for VIP users.
         */
        public final boolean highlightIfVipAndVideoIsLocked;

        SegmentVote(@NonNull StringRef title, int apiVoteType, boolean highlightIfVipAndVideoIsLocked) {
            this.title = title;
            this.apiVoteType = apiVoteType;
            this.highlightIfVipAndVideoIsLocked = highlightIfVipAndVideoIsLocked;
        }
    }

    @NonNull
    public final SegmentCategory category;
    /**
     * NULL if segment is unsubmitted.
     */
    @Nullable
    public final String UUID;
    public final long start;
    public final long end;
    public final boolean isLocked;
    public boolean didAutoSkipped = false;
    /**
     * If this segment has been counted as 'skipped'
     */
    public boolean recordedAsSkipped = false;

    public SponsorSegment(@NonNull SegmentCategory category, @Nullable String UUID, long start, long end, boolean isLocked) {
        this.category = category;
        this.UUID = UUID;
        this.start = start;
        this.end = end;
        this.isLocked = isLocked;
    }

    public boolean shouldAutoSkip() {
        return category.behaviour.skipAutomatically && !(didAutoSkipped && category.behaviour == CategoryBehaviour.SKIP_AUTOMATICALLY_ONCE);
    }

    /**
     * @param nearThreshold threshold to declare the time parameter is near this segment. Must be a positive number.
     */
    public boolean startIsNear(long videoTime, long nearThreshold) {
        return Math.abs(start - videoTime) <= nearThreshold;
    }

    /**
     * @param nearThreshold threshold to declare the time parameter is near this segment. Must be a positive number.
     */
    public boolean endIsNear(long videoTime, long nearThreshold) {
        return Math.abs(end - videoTime) <= nearThreshold;
    }

    /**
     * @return if the time parameter is within this segment.
     */
    public boolean containsTime(long videoTime) {
        return start <= videoTime && videoTime < end;
    }

    /**
     * @return if the segment is completely contained inside this segment.
     */
    public boolean containsSegment(SponsorSegment other) {
        return start <= other.start && other.end <= end;
    }

    /**
     * @return If the range has any overlap with this segment.
     */
    public boolean intersectsRange(Range<Long> range) {
        return range.getLower() < end && range.getUpper() >= start;
    }

    /**
     * @return The start/end time in range form.
     * Range times are adjusted since it uses inclusive and Segments use exclusive.
     * <p>
     * {@link SegmentCategory#HIGHLIGHT} is unique and
     * returns a range from the start of the video until the highlight.
     */
    public Range<Long> getUndoRange() {
        final long undoStart = category == SegmentCategory.HIGHLIGHT
                ? 0
                : start;
        return Range.create(undoStart,  end - 1);
    }

    /**
     * @return the length of this segment, in milliseconds. Always a positive number.
     */
    public long length() {
        return end - start;
    }

    /**
     * @return 'skip segment' UI overlay button text.
     */
    @NonNull
    public String getSkipButtonText() {
        return category.getSkipButtonText(start, VideoInformation.getVideoLength()).toString();
    }

    /**
     * @return 'skipped segment' toast message.
     */
    @NonNull
    public String getSkippedToastText() {
        return category.getSkippedToastText(start, VideoInformation.getVideoLength()).toString();
    }

    @Override
    public int compareTo(SponsorSegment o) {
        // If both segments start at the same time, then sort with the longer segment first.
        // This keeps the seekbar drawing correct since it draws the segments using the sorted order.
        return start == o.start ? Long.compare(o.length(), length()) : Long.compare(start, o.start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SponsorSegment other)) return false;
        return Objects.equals(UUID, other.UUID)
                && category == other.category
                && start == other.start
                && end == other.end;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(UUID);
    }

    @NonNull
    @Override
    public String toString() {
        return "SponsorSegment{"
                + "category=" + category
                + ", start=" + start
                + ", end=" + end
                + '}';
    }
}
