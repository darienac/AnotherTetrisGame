package shaders;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20C.*;

public class ShaderProgram3D extends ShaderProgram {
    public int aVertex;
    public int aNormal;
    public int aTexCoord;

    public int textureDiffuse;
    public int textureReflect;

    public int uViewProjectionMatrix;
    public FloatBuffer viewProjectionMatrixBuffer = BufferUtils.createFloatBuffer(4 * 4);
    public int uModelMatrix;
    public FloatBuffer modelMatrixBuffer = BufferUtils.createFloatBuffer(4 * 4);
    public int uNormalMatrix;
    public FloatBuffer normalMatrixBuffer = BufferUtils.createFloatBuffer(4 * 4);

    public int uLightPosition;
    public FloatBuffer lightPositionBuffer = BufferUtils.createFloatBuffer(3);
    public int uLightColor;
    public FloatBuffer lightColorBuffer = BufferUtils.createFloatBuffer(3);
    public int uAmbientLightColor;
    public FloatBuffer ambientLightColorBuffer = BufferUtils.createFloatBuffer(3);
    public int uViewPosition;
    public FloatBuffer viewPositionBuffer = BufferUtils.createFloatBuffer(3);

    public int uAmbientColor;
    public FloatBuffer ambientColorBuffer = BufferUtils.createFloatBuffer(3);
    public int uDiffuseColor;
    public FloatBuffer diffuseColorBuffer = BufferUtils.createFloatBuffer(3);
    public int uSpecularColor;
    public FloatBuffer specularColorBuffer = BufferUtils.createFloatBuffer(3);
    public int uEmissiveColor;
    public FloatBuffer emissiveColorBuffer = BufferUtils.createFloatBuffer(3);

    public int uOpacity;
    public int uTime;
    public int uAspect;


    public ShaderProgram3D() throws Exception {
        super();
    }

    @Override
    public void link() throws Exception {
        super.link();

        aVertex = glGetAttribLocation(getProgramId(), "aVertex");
        glEnableVertexAttribArray(aVertex);
        if (aVertex != -1) {
            aTexCoord = glGetAttribLocation(getProgramId(), "aTexCoord");
        }
        if (aTexCoord != -1) {
            glEnableVertexAttribArray(aTexCoord);
        }
        aNormal = glGetAttribLocation(getProgramId(), "aNormal");
        if (aNormal != -1) {
            glEnableVertexAttribArray(aNormal);
        }

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
        uTime = glGetUniformLocation(getProgramId(), "uTime");
        uAspect = glGetUniformLocation(getProgramId(), "uAspect");
    }
}
