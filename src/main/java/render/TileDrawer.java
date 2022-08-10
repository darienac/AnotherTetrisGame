package render;

import model.Tile;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Doesn't draw from an array, but instead is manually told the tile x and y coords to draw at after a grid is set up
 */
public class TileDrawer {
    private static final float MODEL_SIZE = 4.0f;

    private static final Vector4f COLOR_CYAN = new Vector4f(0.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector4f COLOR_YELLOW = new Vector4f(1.0f, 0.9f, 0.2f, 1.0f);
    private static final Vector4f COLOR_PURPLE = new Vector4f(0.4f, 0.1f, 0.8f, 1.0f);
    private static final Vector4f COLOR_GREEN = new Vector4f(0.0f, 0.8f, 0.0f, 1.0f);
    private static final Vector4f COLOR_RED = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
    private static final Vector4f COLOR_BLUE = new Vector4f(0.0f, 0.3f, 1.0f, 1.0f);
    private static final Vector4f COLOR_ORANGE = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);

    private final Scene gameScene;
    private final Vector3f origin;
    private float tileSize;
    private final int columns;
    private final int rows;

    private ModelTransform modelTransform;

    public TileDrawer(Scene gameScene, Vector3f origin, float tileSize, int columns, int rows) {
        this.gameScene = gameScene;
        this.origin = origin;
        this.tileSize = tileSize;
        this.columns = columns;
        this.rows = rows;

        modelTransform = new ModelTransform();
        modelTransform.setScale(tileSize / MODEL_SIZE);
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public void render(Tile tile, int x, int y) throws Exception {
        if (tile == null) {
            return;
        }
        if (x < 0 || y < 0 || x >= columns || y >= rows) {
            throw new IllegalArgumentException("Invalid tile coordinate: (" + x + ", " + y + ")");
        }

        ResourcesCache rs = ResourcesCache.getInstance();

        float offsetX = 0.0f;
        float offsetY = 0.0f;
        Model model = rs.getTetrisBlock();
        switch (tile) {
            case BLOCK_CYAN:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_CYAN);
                break;
            case BLOCK_YELLOW:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_YELLOW);
                break;
            case BLOCK_PURPLE:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_PURPLE);
                break;
            case BLOCK_GREEN:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_GREEN);
                break;
            case BLOCK_RED:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_RED);
                break;
            case BLOCK_BLUE:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_BLUE);
                break;
            case BLOCK_ORANGE:
                model = rs.getTetrisBlock();
                rs.getTetrisBlockMaterial().setDiffuseColor(COLOR_ORANGE);
                break;
            case BLOCK_GHOST:
                model = rs.getGhostBlock();
                break;
            case WALL_V:
                model = rs.getWallV();
                break;
            case WALL_H:
                model = rs.getWallH();
                break;
            case WALL_J:
                model = rs.getWallJ();
                break;
            case LABEL_SCORE:
                model = rs.getLabelScore();
                offsetX = 2.5f;
                break;
            case LABEL_LEVEL:
                model = rs.getLabelLevel();
                offsetX = 2.5f;
                break;
            case LABEL_X:
                model = rs.getLabelX();
                break;
            case DIGIT_0:
                model = rs.getDigits()[0];
                break;
            case DIGIT_1:
                model = rs.getDigits()[1];
                break;
            case DIGIT_2:
                model = rs.getDigits()[2];
                break;
            case DIGIT_3:
                model = rs.getDigits()[3];
                break;
            case DIGIT_4:
                model = rs.getDigits()[4];
                break;
            case DIGIT_5:
                model = rs.getDigits()[5];
                break;
            case DIGIT_6:
                model = rs.getDigits()[6];
                break;
            case DIGIT_7:
                model = rs.getDigits()[7];
                break;
            case DIGIT_8:
                model = rs.getDigits()[8];
                break;
            case DIGIT_9:
                model = rs.getDigits()[9];
                break;
        }
        Vector3f position = modelTransform.getPosition();
        position.set(origin);
        position.x += (x - (columns - 1) * 0.5f + offsetX) * tileSize;
        position.y += (y - (rows - 1) * 0.5f + offsetY) * tileSize;
        model.render(gameScene, modelTransform);
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public void setTileSize(float tileSize) {
        this.tileSize = tileSize;
        modelTransform.setScale(tileSize / MODEL_SIZE);
    }
}
