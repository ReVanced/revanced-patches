package app.revanced.integrations.sponsorblock.objects;

import androidx.annotation.NonNull;

import java.text.MessageFormat;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;

public class SponsorSegment implements Comparable<SponsorSegment> {
    public final long start;
    public final long end;
    public final SponsorBlockSettings.SegmentInfo category;
    public final String UUID;
    public final boolean isLocked;
    public boolean didAutoSkipped = false;

    public SponsorSegment(long start, long end, SponsorBlockSettings.SegmentInfo category, String UUID, boolean isLocked) {
        this.start = start;
        this.end = end;
        this.category = category;
        this.UUID = UUID;
        this.isLocked = isLocked;
    }

    @NonNull
    @Override
    public String toString() {
        return MessageFormat.format("SegmentInfo'{'start={0}, end={1}, category=''{2}'', locked={3}'}'", start, end, category, isLocked);
    }

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
    }
}
