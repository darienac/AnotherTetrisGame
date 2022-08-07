package render;

import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIVector3D;
import shaders.ShaderProgram3D;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

public class Mesh {
    private final int vertexArrayBuffer;
    private final int texCoordArrayBuffer;
    private final int normalArrayBuffer;
    private final int elementArrayBuffer;
    private int elementCount;

    private Material material;

    public Mesh() {
        vertexArrayBuffer = glGenBuffers();
        texCoordArrayBuffer = glGenBuffers();
        normalArrayBuffer = glGenBuffers();
        elementArrayBuffer = glGenBuffers();
        elementCount = 0;
    }

    public Mesh(AIMesh aiMesh) {
        AIVector3D.Buffer vertices = aiMesh.mVertices();
        AIVector3D.Buffer texCoords = aiMesh.mTextureCoords(0);
        AIVector3D.Buffer normals = aiMesh.mNormals();
        AIFace.Buffer faces = aiMesh.mFaces();

        vertexArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffer);
        nglBufferData(GL_ARRAY_BUFFER, AIVector3D.SIZEOF * vertices.remaining(), vertices.address(), GL_STATIC_DRAW);

        normalArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normalArrayBuffer);
        nglBufferData(GL_ARRAY_BUFFER, AIVector3D.SIZEOF * normals.remaining(), normals.address(), GL_STATIC_DRAW);

        texCoordArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texCoordArrayBuffer);
        if (texCoords != null) {
            nglBufferData(GL_ARRAY_BUFFER, AIVector3D.SIZEOF * texCoords.remaining(), texCoords.address(), GL_STATIC_DRAW);
        }

        int faceCount = aiMesh.mNumFaces();
        elementCount = faceCount * 3;
        IntBuffer elementArrayBufferData = BufferUtils.createIntBuffer(elementCount);
        for (int i = 0; i < faceCount; ++i) {
            AIFace face = faces.get(i);
            if (face.mNumIndices() != 3) {
                throw new IllegalStateException("AIFace.mNumIndices() != 3");
            }
            elementArrayBufferData.put(face.mIndices());
        }
        elementArrayBufferData.flip();
        elementArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementArrayBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArrayBufferData, GL_STATIC_DRAW);
    }

    public void render(Scene gameScene, ModelTransform modelTransform) {
        ShaderProgram3D shaderProgram3D = gameScene.getShaderProgram3D();
        shaderProgram3D.bind();
        Camera camera = gameScene.getCamera();
        LightSource lightSource = gameScene.getLightSource();

        if (material.getOpacity() < 1.0f) {
            glDisable(GL_CULL_FACE);
        } else {
            glEnable(GL_CULL_FACE);

        }

        // Setup attribute values here
        glBindBuffer(GL_ARRAY_BUFFER, vertexArrayBuffer);
        glVertexAttribPointer(shaderProgram3D.aVertex, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, normalArrayBuffer);
        glVertexAttribPointer(shaderProgram3D.aNormal, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, texCoordArrayBuffer);
        glVertexAttribPointer(shaderProgram3D.aTexCoord, 3, GL_FLOAT, false, 0, 0);

        // Setup uniform values here
        glUniform1i(shaderProgram3D.textureDiffuse, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, material.getTexture().getTextureId());

        glUniform1i(shaderProgram3D.textureReflect, 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, material.getReflectMap().getTextureId());

        glUniformMatrix4fv(shaderProgram3D.uViewProjectionMatrix, false, camera.getViewProjectionMatrix().get(shaderProgram3D.viewProjectionMatrixBuffer));
        glUniformMatrix4fv(shaderProgram3D.uModelMatrix, false, modelTransform.getModelMatrix().get(shaderProgram3D.modelMatrixBuffer));
        glUniformMatrix4fv(shaderProgram3D.uNormalMatrix, false, modelTransform.getNormalMatrix().get(shaderProgram3D.normalMatrixBuffer));

        glUniform3fv(shaderProgram3D.uLightPosition, lightSource.getPosition().get(shaderProgram3D.lightPositionBuffer));
        glUniform3fv(shaderProgram3D.uLightColor, lightSource.getColor().get(shaderProgram3D.lightColorBuffer));
        glUniform3fv(shaderProgram3D.uAmbientLightColor, gameScene.getAmbientLightColor().get(shaderProgram3D.ambientLightColorBuffer));
        glUniform3fv(shaderProgram3D.uViewPosition, camera.getPosition().get(shaderProgram3D.viewPositionBuffer));

        glUniform3fv(shaderProgram3D.uAmbientColor, material.getAmbientColor().get(shaderProgram3D.ambientColorBuffer));
        glUniform3fv(shaderProgram3D.uDiffuseColor, material.getDiffuseColor().get(shaderProgram3D.diffuseColorBuffer));
        glUniform3fv(shaderProgram3D.uSpecularColor, material.getSpecularColor().get(shaderProgram3D.specularColorBuffer));
        glUniform3fv(shaderProgram3D.uEmissiveColor, material.getEmissiveColor().get(shaderProgram3D.emissiveColorBuffer));

        glUniform1f(shaderProgram3D.uOpacity, material.getOpacity());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementArrayBuffer);
        glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0);
    }

    public int getVertexArrayBuffer() {
        return vertexArrayBuffer;
    }

    public int getTexCoordArrayBuffer() {
        return texCoordArrayBuffer;
    }

    public int getNormalArrayBuffer() {
        return normalArrayBuffer;
    }

    public int getElementArrayBuffer() {
        return elementArrayBuffer;
    }

    public int getElementCount() {
        return elementCount;
    }

    public void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}