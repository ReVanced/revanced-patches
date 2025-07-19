package app.revanced.extension.spotify.misc.fix;

import android.media.AudioFormat;
import android.media.AudioTrack;
import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;
import xyz.gianlu.librespot.player.mixing.output.SinkException;
import xyz.gianlu.librespot.player.mixing.output.SinkOutput;

import java.io.IOException;

import static android.media.AudioFormat.*;

/**
 * @author devgianlu
 */
public final class AndroidSinkOutput implements SinkOutput {
    private AudioTrack track;
    private float lastVolume = -1;

    @Override
    public boolean start(OutputAudioFormat format) throws SinkException {
        if (format.getSampleSizeInBits() != 16) {
            throw new SinkException("Unsupported SampleSize", null);
        }
        if (format.getChannels() < 1 || format.getChannels() > 2) {
            throw new SinkException("Unsupported Number of Channels", null);
        }
        int pcmEncoding = ENCODING_PCM_16BIT;
        int channelConfig = format.getChannels() == 1 ? CHANNEL_OUT_MONO : CHANNEL_OUT_STEREO;
        int sampleRate = (int) format.getSampleRate();
        int minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                channelConfig,
                pcmEncoding
        );

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(pcmEncoding)
                .setSampleRate(sampleRate)
                .build();

        try {
            track = new AudioTrack.Builder()
                    .setBufferSizeInBytes(minBufferSize)
                    .setAudioFormat(audioFormat)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        } catch (UnsupportedOperationException e) {
            throw new SinkException("AudioTrack creation failed in Sink: ", e.getCause());
        }

        if (lastVolume != -1) track.setVolume(lastVolume);

        track.play();
        return true;
    }

    @Override
    public void write(byte[] buffer, int offset, int len) throws IOException {
        int outcome = track.write(buffer, offset, len, AudioTrack.WRITE_BLOCKING);
        switch (outcome) {
            case AudioTrack.ERROR:
                throw new IOException("Generic Operation Failure while writing Track");
            case AudioTrack.ERROR_BAD_VALUE:
                throw new IOException("Invalid value used while writing Track");
            case AudioTrack.ERROR_DEAD_OBJECT:
                throw new IOException("Track Object has died in the meantime");
            case AudioTrack.ERROR_INVALID_OPERATION:
                throw new IOException("Failure due to improper use of Track Object methods");
        }
    }

    @Override
    public void flush() {
        if (track != null) track.flush();
    }

    @Override
    public boolean setVolume(float volume) {
        lastVolume = volume;
        if (track != null) track.setVolume(volume);
        return true;
    }

    @Override
    public void release() {
        if (track != null) track.release();
    }

    @Override
    public void stop() {
        if (track != null && track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) track.stop();
    }

    @Override
    public void close() {
        track = null;
    }

    int getPlayState() {
        return track.getPlayState();
    }
}
