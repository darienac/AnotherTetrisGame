package render;

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMaterialProperty;
import org.lwjgl.assimp.AIString;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Material {
    public static Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f ambientColor;
    private Vector4f diffuseColor;
    private Vector4f specularColor;
    private Vector4f emissiveColor;
    private float reflectance;
    private float opacity;
    private Texture texture = null;
    private Texture reflectMap = null;

    public Material() {
        this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, 1.0f);
    }

    public Material(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, Vector4f emissiveColor, float reflectance) {
        setAmbientColor(ambientColor);
        setDiffuseColor(diffuseColor);
        setSpecularColor(specularColor);
        setEmissiveColor(emissiveColor);
        setReflectance(reflectance);

        setOpacity(1.0f);
    }

    public Material(AIMaterial aiMaterial) throws Exception {
        AIColor4D color = AIColor4D.create();

        AIString path = AIString.calloc();
        aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture texture = null;
        TextureCache textCache = TextureCache.getInstance();

        if (textPath.length() > 0) {
            texture = textCache.getTexture(textPath);
        } else {
            texture = textCache.getTexture(null);
        }

        Vector4f ambient = Material.DEFAULT_COLOR;
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);
        if (result == 0) {
            ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f diffuse = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
        if (result == 0) {
            diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f specular = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);
        if (result == 0) {
            specular = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        Vector4f emissive = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_EMISSIVE, aiTextureType_NONE, 0, color);
        if (result == 0) {
            emissive = new Vector4f(color.r(), color.g(), color.b(), color.a());
        }

        setAmbientColor(ambient);
        setDiffuseColor(diffuse);
        setSpecularColor(specular);
        setEmissiveColor(emissive);
        setReflectance(reflectance);
        setTexture(texture);
        setReflectMap(textCache.getTexture(TextureCache.DEFAULT_BLACK));

        try (MemoryStack stack = stackPush()) {
            PointerBuffer materialPointer = stack.mallocPointer(1);
            aiGetMaterialProperty(aiMaterial, AI_MATKEY_OPACITY, materialPointer);
            float opacity = AIMaterialProperty.create(materialPointer.get()).mData().getFloat();
            setOpacity(opacity);
        }
    }

    public void setAmbientColor(Vector4f ambientColor) {
        this.ambientColor = ambientColor;
    }

    public void setDiffuseColor(Vector4f diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public void setSpecularColor(Vector4f specularColor) {
        this.specularColor = specularColor;
    }

    public void setEmissiveColor(Vector4f emissiveColor) {
        this.emissiveColor = emissiveColor;
    }

    public void setReflectance(float reflectance) {
        this.reflectance = reflectance;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getReflectMap() {
        return reflectMap;
    }

    public void setReflectMap(Texture reflectMap) {
        this.reflectMap = reflectMap;
    }

    public Vector4f getAmbientColor() {
        return ambientColor;
    }

    public Vector4f getDiffuseColor() {
        return diffuseColor;
    }

    public Vector4f getSpecularColor() {
        return specularColor;
    }

    public Vector4f getEmissiveColor() {
        return emissiveColor;
    }

    public float getReflectance() {
        return reflectance;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    @Override
    public String toString() {
        return "Material{" +
                "ambientColor=" + ambientColor +
                ", diffuseColor=" + diffuseColor +
                ", specularColor=" + specularColor +
                ", emissiveColor=" + emissiveColor +
                ", reflectance=" + reflectance +
                ", texture=" + texture +
                ", reflectMap=" + reflectMap +
                ", opacity=" + opacity +
                '}';
    }
}