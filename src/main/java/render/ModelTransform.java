package render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ModelTransform {
    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f direction = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

    private float scale = 1.0f;

    public ModelTransform() {
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public Vector3f getUp() {
        return up;
    }

    public void setUp(Vector3f up) {
        this.up = up;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Matrix4f getModelMatrix() {
        return (new Matrix4f()).translate(position).rotateTowards(direction, up).scale(scale);
    }

    public Matrix4f getNormalMatrix() {
        return getModelMatrix().invert().transpose();
    }
}
