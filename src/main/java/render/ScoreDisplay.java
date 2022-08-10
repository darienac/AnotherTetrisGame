package render;

import model.Tile;
import org.joml.Vector3f;

import static model.Tile.*;

public class ScoreDisplay {
    private static final Tile[] digits = {
            DIGIT_0,
            DIGIT_1,
            DIGIT_2,
            DIGIT_3,
            DIGIT_4,
            DIGIT_5,
            DIGIT_6,
            DIGIT_7,
            DIGIT_8,
            DIGIT_9,
    };

    private final TileDrawer tileDrawer;
    private final int numDigits;
    private final Tile labelTile;

    public ScoreDisplay(Scene scene, Vector3f origin, float tileSize, Tile labelTile, int labelWidth, int numDigits) throws Exception {
        tileDrawer = new TileDrawer(scene, origin, tileSize, labelWidth + numDigits, 1);
        this.numDigits = numDigits;
        this.labelTile = labelTile;
    }

    public void renderCount(int amount) throws Exception {
        if (labelTile != null) {
            tileDrawer.render(labelTile, 0, 0);
        }
        for (int i = 0; i < numDigits; i++) {
            int digit = amount % 10;
            tileDrawer.render(digits[digit], tileDrawer.getColumns() - 1 - i, 0);
            amount /= 10;
        }
    }

    public void setTileSize(float tileSize) {
        tileDrawer.setTileSize(tileSize);
    }

    public Vector3f getOrigin() {
        return tileDrawer.getOrigin();
    }
}
