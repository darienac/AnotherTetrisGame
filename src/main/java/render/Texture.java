package render;

import org.lwjgl.system.MemoryStack;

import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture implements AutoCloseable {
    private static final String TEXTURE_PATH = "src/main/resources/textures/";

    private final int textureId;
    private final int width;
    private final int height;

    public Texture(String fileName) throws Exception {
        try (MemoryStack stack = stackPush()) {
            System.out.println(TEXTURE_PATH + fileName);

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer avChannels = stack.mallocInt(1);
            ByteBuffer decodedImage = stbi_load(TEXTURE_PATH + fileName, w, h, avChannels, 4);

            width = w.get(0);
            height = h.get(0);

            textureId = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, textureId);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, decodedImage);
            glGenerateMipmap(GL_TEXTURE_2D);

            stbi_image_free(decodedImage);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public int getTextureId() {
        return textureId;
    }

    @Override
    public void close() throws Exception {
        glDeleteTextures(textureId);
    }
}
