package game;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Controls {
    public Controls() {
    }

    public boolean left = false;
    public boolean right = false;
    public boolean leftTap = false;
    public boolean rightTap = false;
    public boolean up = false;
    public boolean down = false;
    public boolean turnRight = false;
    public boolean turnLeft = false;
    public boolean holdPiece = false;
    public float cameraX = 0.0f;
    public float cameraY = 0.0f;

    private GLFWGamepadState lastGamepadState = null;

    public void getKeyInput(int key, int action) {
        boolean value = (action == GLFW_PRESS);
        if (action == GLFW_REPEAT) {
            return;
        }
        switch (key) {
            case GLFW_KEY_A:
            case GLFW_KEY_LEFT:
                left = value;
                leftTap = leftTap || left;
                break;
            case GLFW_KEY_D:
            case GLFW_KEY_RIGHT:
                right = value;
                rightTap = rightTap || right;
                break;
            case GLFW_KEY_W:
            case GLFW_KEY_UP:
                up = value;
                break;
            case GLFW_KEY_S:
            case GLFW_KEY_DOWN:
                down = value;
                break;
            case GLFW_KEY_E:
            case GLFW_KEY_SPACE:
            case GLFW_KEY_X:
                turnRight = value || turnRight;
                break;
            case GLFW_KEY_Q:
            case GLFW_KEY_Z:
                turnLeft = value || turnLeft;
                break;
            case GLFW_KEY_R:
            case GLFW_KEY_C:
                holdPiece = value || holdPiece;
                break;
        }
    }

    public void readGamepadInputs(int jid) {
        if (!glfwJoystickIsGamepad(jid)) {
            return;
        }
        GLFWGamepadState state = GLFWGamepadState.malloc();
        if (!glfwGetGamepadState(jid, state)) {
            return;
        }
        if (lastGamepadState == null) {
            lastGamepadState = state;
            return;
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_A)) {
            turnLeft = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_A);
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_B)) {
            turnRight = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_B);
        }

        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_DPAD_LEFT)) {
            left = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_DPAD_LEFT);
            leftTap = leftTap || left;
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT)) {
            right = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT);
            rightTap = rightTap || right;
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_DPAD_UP)) {
            up = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_DPAD_UP);
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_DPAD_DOWN)) {
            down = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_DPAD_DOWN);
        }

        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_Y)) {
            holdPiece = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_Y);
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER)) {
            holdPiece = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER);
        }
        if (isGamepadButtonUpdated(state, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER)) {
            holdPiece = isGamepadButtonPressed(state, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER);
        }

        if (isGamepadAxisUpdated(state, GLFW_GAMEPAD_AXIS_RIGHT_X) || isGamepadAxisUpdated(state, GLFW_GAMEPAD_AXIS_RIGHT_Y)) {
            cameraX = state.axes(GLFW_GAMEPAD_AXIS_RIGHT_X);
            cameraY = -state.axes(GLFW_GAMEPAD_AXIS_RIGHT_Y);
        }

        lastGamepadState.close();
        lastGamepadState = state;
    }

    private boolean isGamepadButtonUpdated(GLFWGamepadState state, int button) {
        return state.buttons(button) != lastGamepadState.buttons(button);
    }
    private boolean isGamepadButtonPressed(GLFWGamepadState state, int button) {
        return state.buttons(button) == GLFW_PRESS;
    }

    private boolean isGamepadAxisUpdated(GLFWGamepadState state, int axis) {
        return state.axes(axis) != lastGamepadState.axes(axis);
    }
}
