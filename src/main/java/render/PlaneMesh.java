package render;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

public class PlaneMesh extends Mesh {
    public PlaneMesh(float width, float height, float tx0, float ty0, float tx1, float ty1) {
        super();

        float[] vertices = new float[] {
                -width / 2, height / 2, 0.0f,
                -width / 2, -height / 2, 0.0f,
                width / 2, height / 2, 0.0f,
                width / 2, -height / 2, 0.0f,
        };
        float[] texCoords = new float[] {
                tx0, ty1, 0.0f,
                tx0, ty0, 0.0f,
                tx1, ty1, 0.0f,
                tx1, ty0, 0.0f,
        };
        float[] normals = new float[] {
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
        };
        int[] faces = new int[] {
                0, 1, 2,
                2, 1, 3,
        };

        glBindBuffer(GL_ARRAY_BUFFER, getVertexArrayBuffer());
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, getTexCoordArrayBuffer());
        glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, getNormalArrayBuffer());
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, getElementArrayBuffer());
        glBufferData(GL_ARRAY_BUFFER, faces, GL_STATIC_DRAW);

        setElementCount(faces.length);
    }

    public PlaneMesh(float width, float height) {
        this(width, height, 0.0f, 0.0f, 1.0f, 1.0f);
    }
}