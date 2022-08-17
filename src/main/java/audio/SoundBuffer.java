package audio;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class SoundBuffer implements AutoCloseable {
    private static final String AUDIO_PATH = "src/main/resources/audio/";

    private final int bufferId;
    private ShortBuffer pcm = null;

    private int channels;
    private int sampleRate;
    private int format;

    public SoundBuffer(String file) throws Exception {
        bufferId = alGenBuffers();
        ShortBuffer pcm = readVorbis(file);
        if (channels == 1) {
            format = AL_FORMAT_MONO16;
        } else {
            format = AL_FORMAT_STEREO16;
        }
        alBufferData(bufferId, format, pcm, sampleRate);
        free(pcm);
    }

    public int getBufferId() {
        return bufferId;
    }

    private ShortBuffer readVorbis(String resource) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            ShortBuffer out = stb_vorbis_decode_filename(AUDIO_PATH + resource, channelsBuffer, sampleRateBuffer);

            channels = channelsBuffer.get(0);
            sampleRate = sampleRateBuffer.get(0);

            return out;
        }
    }

    @Override
    public void close() throws Exception {
        alDeleteBuffers(this.bufferId);
        if (pcm != null) {
            MemoryUtil.memFree(pcm);
        }
    }
}
