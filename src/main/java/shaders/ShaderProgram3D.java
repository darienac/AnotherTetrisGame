package shaders;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20C.*;

public class ShaderProgram3D extends ShaderProgram {
    private static final String VERTEX_RESOURCE = "vertex3d.glsl";
    private static final String FRAGMENT_RESOURCE = "fragment3d.glsl";

    public final int aVertex;
    public final int aNormal;
    public final int aTexCoord;

    public final int textureDiffuse;
    public final int textureReflect;

    public final int uViewProjectionMatrix;
    public final FloatBuffer viewProjectionMatrixBuffer = BufferUtils.createFloatBuffer(4 * 4);
    public final int uModelMatrix;
    public final FloatBuffer modelMatrixBuffer = BufferUtils.createFloatBuffer(4 * 4);
    public final int uNormalMatrix;
    public final FloatBuffer normalMatrixBuffer = BufferUtils.createFloatBuffer(4 * 4);

    public final int uLightPosition;
    public final FloatBuffer lightPositionBuffer = BufferUtils.createFloatBuffer(3);
    public final int uLightColor;
    public final FloatBuffer lightColorBuffer = BufferUtils.createFloatBuffer(3);
    public final int uAmbientLightColor;
    public final FloatBuffer ambientLightColorBuffer = BufferUtils.createFloatBuffer(3);
    public final int uViewPosition;
    public final FloatBuffer viewPositionBuffer = BufferUtils.createFloatBuffer(3);

    public final int uAmbientColor;
    public final FloatBuffer ambientColorBuffer = BufferUtils.createFloatBuffer(3);
    public final int uDiffuseColor;
    public final FloatBuffer diffuseColorBuffer = BufferUtils.createFloatBuffer(3);
    public final int uSpecularColor;
    public final FloatBuffer specularColorBuffer = BufferUtils.createFloatBuffer(3);
    public final int uEmissiveColor;
    public final FloatBuffer emissiveColorBuffer = BufferUtils.createFloatBuffer(3);

    public final int uOpacity;


    public ShaderProgram3D() throws Exception {
        super();
        createVertexShader(VERTEX_RESOURCE);
        createFragmentShader(FRAGMENT_RESOURCE);
        link();

        aVertex = glGetAttribLocation(getProgramId(), "aVertex");
        glEnableVertexAttribArray(aVertex);
        aTexCoord = glGetAttribLocation(getProgramId(), "aTexCoord");
        glEnableVertexAttribArray(aTexCoord);
        aNormal = glGetAttribLocation(getProgramId(), "aNormal");
        glEnableVertexAttribArray(aNormal);

        textureDiffuse = glGetUniformLocation(getProgramId(), "textureDiffuse");
        textureReflect = glGetUniformLocation(getProgramId(), "textureReflect");

        uViewProjectionMatrix = glGetUniformLocation(getProgramId(), "uViewProjectionMatrix");
        uModelMatrix = glGetUniformLocation(getProgramId(), "uModelMatrix");
        uNormalMatrix = glGetUniformLocation(getProgramId(), "uNormalMatrix");

        uLightPosition = glGetUniformLocation(getProgramId(), "uLightPosition");
        uLightColor = glGetUniformLocation(getProgramId(), "uLightColor");
        uAmbientLightColor = glGetUniformLocation(getProgramId(), "uAmbientLightColor");
        uViewPosition = glGetUniformLocation(getProgramId(), "uViewPosition");

        uAmbientColor = glGetUniformLocation(getProgramId(), "uAmbientColor");
        uDiffuseColor = glGetUniformLocation(getProgramId(), "uDiffuseColor");
        uSpecularColor = glGetUniformLocation(getProgramId(), "uSpecularColor");
        uEmissiveColor = glGetUniformLocation(getProgramId(), "uEmissiveColor");

        uOpacity = glGetUniformLocation(getProgramId(), "uOpacity");
    }
}
