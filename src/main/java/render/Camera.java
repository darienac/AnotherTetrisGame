package render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import startup.GameWindow;

public class Camera {
    private GameWindow window;

    // Directly modified by getters and setters
    float fovy = (float) Math.toRadians(45.0f);
    float zNear = 0.01f;
    float zFar = 100.0f;

    private Vector3f position = new Vector3f(0.0f, 0.0f, 10.0f);
    private Vector3f target = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

    // Indirect, used for rendering when requested

    public Camera(GameWindow window) {
        this.window = window;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f getViewProjectionMatrix() {
        float aspect = (float) window.getWidth() / window.getHeight();
        return (new Matrix4f()).perspective(fovy, aspect, zNear, zFar).lookAt(position, target, up);
    }
}
