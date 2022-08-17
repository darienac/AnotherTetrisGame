package render;

import java.util.HashMap;
import java.util.Map;

public class ResourcesCache {
    private static ResourcesCache resourcesCache = null;

    private final Texture metalReflect;
    private final Texture dissolve;

    private final Model tetrisBlock;
    private final Model ghostBlock;
    private final Material tetrisBlockMaterial;
    private final Model wallV;
    private final Model wallH;
    private final Model wallJ;
    private final Model labelScore;
    private final Model labelLevel;
    private final Model labelX;
    private final Model[] digits;
    private final Model[] messageModels;

    private ResourcesCache() throws Exception {
        metalReflect = new Texture("metalReflection.png");
        dissolve = new Texture("pieceDecay.png");

        tetrisBlock = new Model("tetrisBlock.obj");
        tetrisBlockMaterial = tetrisBlock.getMeshes()[0].getMaterial();
        ghostBlock = new Model("ghostBlock.obj");
        wallV = new Model("pipeEdge.obj");
        wallV.getMeshes()[0].getMaterial().setReflectMap(metalReflect);
        wallH = new Model("pipeEdgeH.obj");
        wallH.getMeshes()[0].getMaterial().setReflectMap(metalReflect);
        wallJ = new Model("pipeJoin.obj");
        wallJ.getMeshes()[0].getMaterial().setReflectMap(metalReflect);

        labelScore = new TexturePlaneModel(new Texture("score.png"), 24.0f, 8.0f);
        labelLevel = new TexturePlaneModel(new Texture("level.png"), 24.0f, 8.0f);
        labelX = new TexturePlaneModel(new Texture("smallX.png"), 4.0f, 8.0f);
        Texture digitsTexture = new Texture("digits.png");
        digits = new Model[10];
        for (int i = 0; i < 10; i++) {
            digits[i] = new TexturePlaneModel(digitsTexture, 4.0f, 8.0f, i * 0.1f, 0.0f, (i + 1) * 0.1f, 1.0f);
        }

        Texture messages = new Texture("lineClearMessages.png");
        messageModels = new Model[8];
        float messageWidth = 3.0f;
        float messageHeight = 1.0f;
        for (int i = 0; i < 8; i++) {
            messageModels[i] = new TexturePlaneModel(messages, messageWidth, messageHeight, 0.0f, i * 0.125f, 1.0f, (i + 1) * 0.125f);
        }
    }

    public static ResourcesCache getInstance() throws Exception {
        if (resourcesCache == null) {
            resourcesCache = new ResourcesCache();
        }
        return resourcesCache;
    }

    public Texture getMetalReflect() {
        return metalReflect;
    }

    public Texture getDissolve() {
        return dissolve;
    }

    public Model getTetrisBlock() {
        return tetrisBlock;
    }

    public Model getGhostBlock() {
        return ghostBlock;
    }

    public Material getTetrisBlockMaterial() {
        return tetrisBlockMaterial;
    }

    public Model getWallV() {
        return wallV;
    }

    public Model getWallH() {
        return wallH;
    }

    public Model getWallJ() {
        return wallJ;
    }

    public Model getLabelScore() {
        return labelScore;
    }

    public Model getLabelLevel() {
        return labelLevel;
    }

    public Model getLabelX() {
        return labelX;
    }

    public Model[] getDigits() {
        return digits;
    }

    public Model[] getMessageModels() {
        return messageModels;
    }
}
