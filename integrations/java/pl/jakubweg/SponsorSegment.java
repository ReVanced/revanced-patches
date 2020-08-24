package pl.jakubweg;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SponsorSegment implements Comparable<SponsorSegment> {
    public final long start;
    public final long end;
    public final SponsorBlockSettings.SegmentInfo category;
    public final String UUID;

    public SponsorSegment(long start, long end, SponsorBlockSettings.SegmentInfo category, String UUID) {
        this.start = start;
        this.end = end;
        this.category = category;
        this.UUID = UUID;
    }

    public static SponsorSegment readFrom(RandomAccessFile stream) throws IOException {
        long start = stream.readLong();
        long end = stream.readLong();
        String categoryName = stream.readUTF();
        String UUID = stream.readUTF();
        SponsorBlockSettings.SegmentInfo category = SponsorBlockSettings.SegmentInfo.valueOf(categoryName);
        return new SponsorSegment(start, end, category, UUID);
    }

    @Override
    public String toString() {
        return "SegmentInfo{" +
                "start=" + start +
                ", end=" + end +
                ", category='" + category + '\'' +
                '}';
    }

    @Override
    public int compareTo(SponsorSegment o) {
        return (int) (this.start - o.start);
    }

    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeLong(start);
        stream.writeLong(end);
        stream.writeUTF(category.name());
        stream.writeUTF(UUID);
    }
}
