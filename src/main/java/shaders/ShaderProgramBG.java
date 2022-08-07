package shaders;

import static org.lwjgl.opengl.GL20C.*;

public class ShaderProgramBG extends ShaderProgram {
    private int aVertex;
    private int aTexCoord;

    private int texture0;

    private int uTime;
    private int uAspect;

    public ShaderProgramBG() throws Exception {
        super();
    }

    @Override
    public void link() throws Exception {
        super.link();

        aVertex = glGetAttribLocation(getProgramId(), "aVertex");
        glEnableVertexAttribArray(aVertex);
        aTexCoord = glGetAttribLocation(getProgramId(), "aTexCoord");
        glEnableVertexAttribArray(aTexCoord);

        texture0 = glGetUniformLocation(getProgramId(), "texture0");

        uTime = glGetUniformLocation(getProgramId(), "uTime");
        uAspect = glGetUniformLocation(getProgramId(), "uAspect");
    }

    public int aVertex() {
        return aVertex;
    }

    public int aTexCoord() {
        return aTexCoord;
    }

    public int uTime() {
        return uTime;
    }

    public int uAspect() {
        return uAspect;
    }

    public int texture0() {
        return texture0;
    }
}
