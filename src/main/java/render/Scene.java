package render;

import org.joml.Vector3f;
import shaders.ShaderProgram3D;
import startup.GameWindow;

public class Scene {
    private Camera camera;
    private ShaderProgram3D shaderProgram3D;
    private LightSource lightSource;
    private Vector3f ambientLightColor;

    public Scene(GameWindow window, ShaderProgram3D shaderProgram3D) {
        this.shaderProgram3D = shaderProgram3D;

        this.camera = new Camera(window);
        this.lightSource = new LightSource();
        ambientLightColor = new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public ShaderProgram3D getShaderProgram3D() {
        return shaderProgram3D;
    }

    public void setShaderProgram(ShaderProgram3D shaderProgram3D) {
        this.shaderProgram3D = shaderProgram3D;
    }

    public LightSource getLightSource() {
        return lightSource;
    }

    public void setLightSource(LightSource lightSource) {
        this.lightSource = lightSource;
    }

    public Vector3f getAmbientLightColor() {
        return ambientLightColor;
    }
}
