package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.utils.StringRef.sf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.utils.StringRef;

public class SponsorSegment implements Comparable<SponsorSegment> {
    public enum SegmentVote {
        UPVOTE(sf("sb_vote_upvote"), 1,false),
        DOWNVOTE(sf("sb_vote_downvote"), 0, true),
        CATEGORY_CHANGE(sf("sb_vote_category"), -1, true); // apiVoteType is not used for category change

        public static final SegmentVote[] voteTypesWithoutCategoryChange = {
                UPVOTE,
                DOWNVOTE,
        };

        @NonNull
        public final StringRef title;
        public final int apiVoteType;
        public final boolean shouldHighlight;

        SegmentVote(@NonNull StringRef title, int apiVoteType, boolean shouldHighlight) {
            this.title = title;
            this.apiVoteType = apiVoteType;
            this.shouldHighlight = shouldHighlight;
        }
    }

    @NonNull
    public final SegmentCategory category;
    /**
     * NULL if segment is unsubmitted
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
     * @param nearThreshold threshold to declare the time parameter is near this segment. Must be a positive number
     */
    public boolean startIsNear(long videoTime, long nearThreshold) {
        return Math.abs(start - videoTime) <= nearThreshold;
    }

    /**
     * @param nearThreshold threshold to declare the time parameter is near this segment. Must be a positive number
     */
    public boolean endIsNear(long videoTime, long nearThreshold) {
        return Math.abs(end - videoTime) <= nearThreshold;
    }

    /**
     * @return if the time parameter is within this segment
     */
    public boolean containsTime(long videoTime) {
        return start <= videoTime && videoTime < end;
    }

    /**
     * @return if the segment is completely contained inside this segment
     */
    public boolean containsSegment(SponsorSegment other) {
        return start <= other.start && other.end <= end;
    }

    /**
     * @return the length of this segment, in milliseconds.  Always a positive number.
     */
    public long length() {
        return end - start;
    }

    /**
     * @return 'skip segment' UI overlay button text
     */
    @NonNull
    public String getSkipButtonText() {
        return category.getSkipButtonText(start, VideoInformation.getCurrentVideoLength()).toString();
    }

    /**
     * @return 'skipped segment' toast message
     */
    @NonNull
    public String getSkippedToastText() {
        return category.getSkippedToastText(start, VideoInformation.getCurrentVideoLength()).toString();
    }

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
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
