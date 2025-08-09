package app.revanced.extension.spotify.misc.fix;

import android.media.*;
import android.os.Build;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import xyz.gianlu.librespot.player.decoders.Decoder;
import xyz.gianlu.librespot.player.decoders.SeekableInputStream;
import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public final class AndroidNativeDecoder extends Decoder {
    private static final String TAG = AndroidNativeDecoder.class.getSimpleName();
    private final byte[] buffer = new byte[2 * BUFFER_SIZE];
    private final MediaCodec codec;
    private final MediaExtractor extractor;
    private final Object closeLock = new Object();
    private long presentationTime = 0;

    public AndroidNativeDecoder(@NotNull SeekableInputStream audioIn, float normalizationFactor, int duration) throws IOException, DecoderException {
        super(audioIn, normalizationFactor, duration);

        extractor = new MediaExtractor();
        extractor.setDataSource(new MediaDataSource() {
            @Override
            public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                audioIn.seek((int) position);
                return audioIn.read(buffer, offset, size);
            }

            @Override
            public long getSize() {
                return audioIn.size();
            }

            @Override
            public void close() {
                audioIn.close();
            }
        });

        if (extractor.getTrackCount() == 0)
            throw new DecoderException("No tracks found.");

        extractor.selectTrack(0);

        MediaFormat format = extractor.getTrackFormat(0);

        codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
        codec.configure(format, null, null, 0);
        codec.start();

        int sampleSize = 16;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (format.getInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT)) {
                case AudioFormat.ENCODING_PCM_8BIT:
                    sampleSize = 8;
                    break;
                case AudioFormat.ENCODING_PCM_16BIT:
                    sampleSize = 16;
                    break;
                default:
                    throw new DecoderException("Unsupported PCM encoding.");
            }
        }

        setAudioFormat(new OutputAudioFormat(format.getInteger(MediaFormat.KEY_SAMPLE_RATE), sampleSize,
                format.getInteger(MediaFormat.KEY_CHANNEL_COUNT), true, false));
    }

    @Override
    protected int readInternal(@NotNull OutputStream out) throws IOException {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        while (true) {
            if (closed) return -1;

            synchronized (closeLock) {
                int inputBufferId = codec.dequeueInputBuffer(-1);
                if (inputBufferId >= 0) {
                    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
                    int count = extractor.readSampleData(inputBuffer, 0);
                    if (count == -1) {
                        codec.signalEndOfInputStream();
                        return -1;
                    }

                    codec.queueInputBuffer(inputBufferId, inputBuffer.position(), inputBuffer.limit(), extractor.getSampleTime(), 0);
                    extractor.advance();
                }

                int outputBufferId = codec.dequeueOutputBuffer(info, -1);
                if (outputBufferId >= 0) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);

                    while (outputBuffer.remaining() > 0) {
                        int read = Math.min(outputBuffer.remaining(), buffer.length);
                        outputBuffer.get(buffer, 0, read);
                        out.write(buffer, 0, read);
                    }

                    codec.releaseOutputBuffer(outputBufferId, false);
                    presentationTime = TimeUnit.MICROSECONDS.toMillis(info.presentationTimeUs);
                    return info.size;
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    Log.d(TAG, "Output buffers changed");
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    Log.d(TAG, "Output format changed: " + codec.getOutputFormat());
                } else {
                    Log.e(TAG, "Failed decoding: " + outputBufferId);
                    return -1;
                }
            }
        }
    }

    @Override
    public void seek(int positionMs) {
        extractor.seekTo(TimeUnit.MILLISECONDS.toMicros(positionMs), MediaExtractor.SEEK_TO_CLOSEST_SYNC);
    }

    @Override
    public void close() throws IOException {
        synchronized (closeLock) {
            codec.release();
            extractor.release();
            super.close();
        }
    }

    @Override
    public int time() {
        return (int) presentationTime;
    }
}