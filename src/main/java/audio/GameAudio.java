package audio;

import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC11.*;

public class GameAudio {
    public GameAudio() {
        long device = alcOpenDevice((ByteBuffer) null);
        if (device != 0) {
            long context = alcCreateContext(device, (int[]) null);
        }
        alGetError();

        alGenBuffers( /* put something here */);

        // Load file
    }
}
