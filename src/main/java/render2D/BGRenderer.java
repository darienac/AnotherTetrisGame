package render2D;

import org.lwjgl.glfw.GLFW;
import render.Texture;
import shaders.ShaderProgramBG;
import startup.GameWindow;

import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20C.*;

public class BGRenderer {
    private static final int vertexArrayBuffer;
    private static final int texCoordArrayBuffer;
    private static final int elementArrayBuffer;

    static {
        float[] vertices = new float[] {
                -1.0f, -1.0f,
                1.0f, 1.0f,
                -1.0f, 1.0f,
                1.0f, -1.0f,
        };
        float[] texCoords = new float[] {
                0.0f, 0.0f,
                1.0f, -1.0f,
                0.0f, -1.0f,
                1.0f, 0.0f,
        };
        int[] faces = new int[] {
            0, 1, 2,
            0, 3, 1,
        };

        vertexArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        texCoordArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texCoordArrayBuffer);
        glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW);
        elementArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, elementArrayBuffer);
        glBufferData(GL_ARRAY_BUFFER, faces, GL_STATIC_DRAW);
    }

    private final ShaderProgramBG shaderProgram;
    private final Texture texture0;

    public BGRenderer(ShaderProgramBG shaderProgram, Texture texture0) {
        this.shaderProgram = shaderProgram;
        this.texture0 = texture0;
    }

    public void render(GameWindow window) {
        shaderProgram.bind();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);

        glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffer);
        glVertexAttribPointer(shaderProgram.aVertex(), 2, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, texCoordArrayBuffer);
        glVertexAttribPointer(shaderProgram.aTexCoord(), 2, GL_FLOAT, false, 0, 0);

        glUniform1i(shaderProgram.texture0(), 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture0.getTextureId());

        glUniform1f(shaderProgram.uTime(), (float) GLFW.glfwGetTime());
        glUniform1f(shaderProgram.uAspect(), ((float) window.getWidth()) / window.getHeight());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementArrayBuffer);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }
}
