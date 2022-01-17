package pl.jakubweg.objects;

import pl.jakubweg.SponsorBlockSettings;

public class SponsorSegment implements Comparable<SponsorSegment> {
    public final long start;
    public final long end;
    public final SponsorBlockSettings.SegmentInfo category;
    public final String UUID;
    public final boolean isLocked;

    public SponsorSegment(long start, long end, SponsorBlockSettings.SegmentInfo category, String UUID, boolean isLocked) {
        this.start = start;
        this.end = end;
        this.category = category;
        this.UUID = UUID;
        this.isLocked = isLocked;
    }

    @Override
    public String toString() {
        return "SegmentInfo{" +
                "start=" + start +
                ", end=" + end +
                ", category='" + category + '\'' +
                ", locked=" + isLocked +
                '}';
    }

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
    }
}
