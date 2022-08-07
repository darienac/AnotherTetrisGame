package render;

import org.joml.Vector3f;

public class LightSource {
    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

    public LightSource() {
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }
}
