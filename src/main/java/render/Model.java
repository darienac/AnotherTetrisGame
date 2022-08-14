package render;

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import shaders.ShaderProgram;
import shaders.ShaderProgram3D;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Model {
    private static final String MODEL_PATH = "src/main/resources/models/";
    private static final int LOAD_FLAGS = aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals;

    private Mesh[] meshes;

    public Model() {
        Mesh[] meshes = new Mesh[0];
    }

    public Model(String modelName) throws Exception {
        AIScene aiScene = aiImportFile(MODEL_PATH + modelName, LOAD_FLAGS);
        if (aiScene == null) {
            throw new IOException("Error loading model");
        }

        int materialCount = aiScene.mNumMaterials();
        PointerBuffer aiMaterials = aiScene.mMaterials();
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < materialCount; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            materials.add(new Material(aiMaterial));
        }

        int meshCount = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        Mesh[] meshes = new Mesh[meshCount];
        for (int i = 0; i < meshCount; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh, materials);
            meshes[i] = mesh;
        }

        this.meshes = meshes;
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void render(Scene gameScene, ModelTransform modelTransform) {
        for (Mesh mesh : meshes) {
            mesh.render(gameScene, modelTransform);
        }
    }

    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) {
        Mesh mesh = new Mesh(aiMesh);

        Material material;
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new Material();
        }
        mesh.setMaterial(material);

        return mesh;
    }

    private static void processVertices(AIMesh aiMesh, List<Float> vertices) {
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();
            vertices.add(aiVertex.x());
            vertices.add(aiVertex.y());
            vertices.add(aiVertex.z());
        }
    }

    private static void processNormals(AIMesh aiMesh, List<Float> normals) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        while (aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.y());
            normals.add(aiNormal.z());
        }
    }
}