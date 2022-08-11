package render;

import model.Message;
import model.Tile;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;

public class LineClearMessage {
    private LinkedList<Message> messages;
    Scene gameScene;
    private double timeStart;
    private float x;
    private float y;
    private ModelTransform transform;
    private int comboAmount;
    private ScoreDisplay countDisplay;

    public LineClearMessage(Scene gameScene, float x, float y) throws Exception {
        this.gameScene = gameScene;
        this.x = x;
        this.y = y;
        this.comboAmount = -1;
        transform = new ModelTransform();
        transform.setScale(0.3f);
        countDisplay = new ScoreDisplay(gameScene, new Vector3f(0.0f, 0.0f, 0.0f), 0.15f, Tile.LABEL_X, 1, 2);
        timeStart = GLFW.glfwGetTime();
    }

    public void setMessages(LinkedList<Message> messages) {
        this.messages = new LinkedList<>(messages);
    }

    public void setTimeStart(double timeStart) {
        this.timeStart = timeStart;
    }

    public void setComboAmount(int comboAmount) {
        this.comboAmount = comboAmount;
    }

    public void render() throws Exception {
        double delta = GLFW.glfwGetTime() - timeStart;
        if (messages == null || messages.size() == 0 || (delta > 3)) {
            return;
        }

        float fade;
        if (delta < 0.5f) {
            fade = (float) delta * 2;
        } else if (delta > 2.5f) {
            fade = 1.0f - 2 * ((float) delta - 2.5f);
        } else {
            fade = 1.0f;
        }
        gameScene.getShaderProgram3D().setUniformFloat("uMessageFade", fade);

        float scale = 0.4f;
        transform.setScale(scale);
        countDisplay.setTileSize(scale / 2);

        Model[] modelMessages = ResourcesCache.getInstance().getMessageModels();
        float yPos = y;
        for (Message message : messages) {
            Model model = null;
            switch (message) {
                case TSPIN:
                    model = modelMessages[7];
                    break;
                case SINGLE:
                    model = modelMessages[6];
                    break;
                case DOUBLE:
                    model = modelMessages[5];
                    break;
                case TRIPLE:
                    model = modelMessages[4];
                    break;
                case TETRIS:
                    model = modelMessages[3];
                    break;
                case B2B:
                    model = modelMessages[2];
                    break;
                case COMBO:
                    model = modelMessages[1];
                    break;
                case MINI:
                    model = modelMessages[0];
                    break;
            }
            transform.setPosition(new Vector3f(x, yPos, 0.0f));
            yPos -= scale;
            model.render(gameScene, transform);
        }

        if (comboAmount > 0) {
            countDisplay.getOrigin().set(new Vector3f(x, yPos, 0.0f));
            countDisplay.renderCount(comboAmount);
        }
    }
}
