package render;

import org.joml.Vector4f;

public class TexturePlaneModel extends Model {
    public TexturePlaneModel(Texture texture, float width, float height, float tx0, float ty0, float tx1, float ty1) throws Exception {
        super();
        Mesh mesh = new PlaneMesh(width, height, tx0, ty0, tx1, ty1);

        Vector4f ambient = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f diffuse = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f specular = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f emissive = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        Material material = new Material(ambient, diffuse, specular, emissive, 1.0f);

        material.setTexture(texture);
        material.setReflectMap(TextureCache.getInstance().getTexture(TextureCache.DEFAULT_BLACK));

        mesh.setMaterial(material);

        setMeshes(new Mesh[] {mesh});
    }

    public TexturePlaneModel(Texture texture, float width, float height) throws Exception {
        this(texture, width, height, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public TexturePlaneModel(Texture texture) throws Exception {
        this(texture, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f);
    }
}