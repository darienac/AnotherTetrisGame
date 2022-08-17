package render;

import game.GameState;

import model.Message;
import model.Tetrimino;
import model.Tile;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import render2D.BGRenderer;
import shaders.ShaderProgram;
import shaders.ShaderProgram3D;
import shaders.ShaderProgramBG;
import startup.GameWindow;

import java.util.LinkedList;

import static org.lwjgl.opengl.GL30.*;

/**
 * Renders the game using OpenGL, renders whenever the render function is called
 */
public class GameRenderer {
    private final GameState state;
    private final GameWindow window;
    private final Scene gameScene;
    private final Scene rainbowScene;

    private final ShaderProgram3D shaderProgram3D;
    private final ShaderProgram3D rainbowShader;
    private final ShaderProgram3D pulseShader;
    private final ShaderProgram3D dissolveShader;

    private final TileDrawer tetrisBoard;

    private final Model testImg;
    private final ModelTransform testImgT;
    private final Model holdText;
    private final ModelTransform holdTextT;
    private final Model nextText;
    private final ModelTransform nextTextT;
    private final ModelTransform tileViewTransform;

    private final TileDrawer pieceDrawer3x3;
    private final TileDrawer pieceDrawer4x4;

    private final BGRenderer bgRenderer;
    private final ScoreDisplay gameScoreDisplay;
    private final ScoreDisplay gameLevelDisplay;
    private final LineClearMessage lineClearMessage;

    private double lastFramerateCheck;
    private int framesPerSecond;

    public GameRenderer(GameState state, GameWindow window) throws Exception {
        this.state = state;
        this.window = window;

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram3D = new ShaderProgram3D();
        shaderProgram3D.createVertexShader("vertex3d.glsl");
        shaderProgram3D.createFragmentShader("fragment3d.glsl");
        shaderProgram3D.link();
        gameScene = new Scene(window, shaderProgram3D);
        gameScene.getCamera().setPosition(new Vector3f(0.0f, 2.0f, 10.0f));
        gameScene.getLightSource().setPosition(new Vector3f(0.0f, 0.0f, 5.0f));

        rainbowShader = new ShaderProgram3D();
        rainbowShader.createVertexShader("vertex3d.glsl");
        rainbowShader.createFragmentShader("fragment3dRainbow.glsl");
        rainbowShader.link();
        rainbowScene = new Scene(window, rainbowShader);
        rainbowScene.setCamera(gameScene.getCamera());
        rainbowScene.setLightSource(gameScene.getLightSource());

        pulseShader = new ShaderProgram3D();
        pulseShader.createVertexShader("vertex3d.glsl");
        pulseShader.createFragmentShader("fragment3dPulse.glsl");
        pulseShader.link();

        dissolveShader = new ShaderProgram3D();
        dissolveShader.createVertexShader("vertex3d.glsl");
        dissolveShader.createFragmentShader("fragment3dDissolve.glsl");
        dissolveShader.link();

        tetrisBoard = new TileDrawer(gameScene, new Vector3f(0.0f, 0.0f, 0.0f), 0.25f, 12, 22);

        testImg = new TexturePlaneModel(new Texture("tetrisLogo.png"), 6.56f, 1.92f);
        testImgT = new ModelTransform();
        testImgT.setPosition(new Vector3f(0.0f, 5.0f, -14.0f));

        holdText = new TexturePlaneModel(new Texture("holdText.png"), 0.75f, 0.25f);
        holdTextT = new ModelTransform();
        holdTextT.setPosition(new Vector3f(-2.25f, 2.5f, 0.0f));

        nextText = new TexturePlaneModel(new Texture("nextText.png"), 0.75f, 0.25f);
        nextTextT = new ModelTransform();
        nextTextT.setPosition(new Vector3f(2.25f, 2.5f, 0.0f));

        tileViewTransform = new ModelTransform();

        pieceDrawer3x3 = new TileDrawer(gameScene, new Vector3f(0.0f, 0.0f, 0.0f), 0.2f, 3, 3);
        pieceDrawer4x4 = new TileDrawer(gameScene, new Vector3f(0.0f, 0.0f, 0.0f), 0.2f, 4, 4);

        gameScoreDisplay = new ScoreDisplay(gameScene, new Vector3f(0.0f, 3.0f, 0.0f), 0.15f, Tile.LABEL_SCORE, 6, 8);
        gameLevelDisplay = new ScoreDisplay(gameScene, new Vector3f(-2.25f, 1.2f, 0.0f), 0.15f, Tile.LABEL_LEVEL, 6, 2);

        lineClearMessage = new LineClearMessage(rainbowScene, -2.25f, 0.0f);

        ShaderProgramBG bgShader = new ShaderProgramBG();
        bgShader.createFragmentShader("fragmentBG1.glsl");
        bgShader.createVertexShader("vertexBG.glsl");
        bgShader.link();
        bgRenderer = new BGRenderer(bgShader, new Texture("clouds.png"));

        lastFramerateCheck = GLFW.glfwGetTime();
        framesPerSecond = 0;
    }

    public Scene getGameScene() {
        return gameScene;
    }

    public void render() throws Exception {
        framesPerSecond++;
        if (GLFW.glfwGetTime() - lastFramerateCheck > 1.0) {
            lastFramerateCheck = Math.floor(GLFW.glfwGetTime() - lastFramerateCheck) + lastFramerateCheck;
            if (framesPerSecond < 60) {
                System.out.println("FPS: " + framesPerSecond);
            }
            framesPerSecond = 0;
        }

        state.lock.lock();
        try {
            double time = GLFW.glfwGetTime();

            glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (window.isResized()) {
                glViewport(0, 0, window.getWidth(), window.getHeight());
                window.setResized(false);
            }

            if (state.getMode() == GameState.Mode.GAME) {
                setCameraPos();

                shaderProgram3D.bind();

                if (state.getBgOption() == 1) {
                    bgRenderer.render(window);
                    gameScene.getAmbientLightColor().set(new Vector3f(0.3f, 0.15f, 1.0f));
                    gameScene.getLightSource().setColor(new Vector3f(1.4f, 1.2f, 1.6f));
                }

                gameScoreDisplay.renderCount(state.getGameScore());
                gameLevelDisplay.renderCount(state.getGameLevel());

                holdText.render(gameScene, holdTextT);
                nextText.render(gameScene, nextTextT);
                drawTetrimino(state.getHeldPiece(), new Vector3f(-2.25f, 2.0f, 0.0f));
                Vector3f nextPiecesPos = new Vector3f(2.25f, 2.0f, 0.0f);
                for (Tetrimino piece : state.getNextPieces()) {
                    drawTetrimino(piece, nextPiecesPos);
                    nextPiecesPos.y -= 0.8f;
                }

                drawTileBox(tetrisBoard, 0, 0, 11, 21);
                Tile[][] tiles = state.getDrawnTiles(10, 20, true, true);
                Tile[][] tilesSolid = state.getSolidTiles();
                for (int y = 0; y < 20; y++) {
                    if (state.isLineClear() && state.getClearRows().contains(y)) {
                        gameScene.setShaderProgram(dissolveShader);
                        float deltaTime = (float) (GLFW.glfwGetTime() - state.getLineClearStart());
                        dissolveShader.setUniformFloat("uDeltaTime", (float) (Math.sqrt(3) * Math.pow(deltaTime, 0.5)));
                        dissolveShader.bindTexture("textureDissolve", 2, ResourcesCache.getInstance().getDissolve());
                    } else {
                        gameScene.setShaderProgram(shaderProgram3D);
                    }
                    for (int x = 0; x < 10; x++) {
                        if (state.isPieceOnGround() && tilesSolid[y][x] == null && tiles[y][x] != null && tiles[y][x] != Tile.BLOCK_GHOST) {
                            gameScene.setShaderProgram(pulseShader);
                            tetrisBoard.render(tiles[y][x], x + 1, y + 1);
                            gameScene.setShaderProgram(shaderProgram3D);
                        } else {
                            tetrisBoard.render(tiles[y][x], x + 1, y + 1);
                        }
                    }
                }
                gameScene.setShaderProgram(shaderProgram3D);

                if (state.getLineClearMessages().size() > 0) {
                    lineClearMessage.setMessages(state.getLineClearMessages());
                    lineClearMessage.setTimeStart(state.getLineClearMessagesTimestamp());
                    lineClearMessage.setComboAmount(state.getComboStreak());
                }
                lineClearMessage.render();
            }
        } finally {
            state.lock.unlock();
        }
    }

    private void setCameraPos() {
        Vector3f pos1 = getCameraPushPos(state.getLastCameraPushDirection());
        Vector3f pos2 = getCameraPushPos(state.getCameraPushDirection());
        double delta = (GLFW.glfwGetTime() - state.getCameraPushTimestamp());
        double biasPos = Math.max(0, Math.min(1, delta / 0.2));

        double bias = biasPos < 0.5 ? 2 * biasPos * biasPos : 1 - Math.pow(-2 * biasPos + 2, 2) / 2;
        Vector3f pos3 = pos1.mul((float) (1 - biasPos)).add(pos2.mul((float) biasPos));
        Vector2f cameraControls = state.getCameraControls();
        pos3.x += cameraControls.x * 2.5f;
        pos3.y += cameraControls.y * 2.5f;
        gameScene.getCamera().setPosition(pos3);
    }

    private Vector3f getCameraPushPos(GameState.CameraPushDirection pushDir) {
        Vector3f pos = new Vector3f(0.0f, 2.0f, 10.0f);
        switch (pushDir) {
            case LEFT:
                pos.x -= 0.25;
                break;
            case RIGHT:
                pos.x += 0.25;
                break;
            case UP:
                pos.y += 0.25;
                break;
            case DOWN:
                pos.y -= 0.25;
                break;
        }
        return pos;
    }

    private void drawTileBox(TileDrawer tileDrawer, int x0, int y0, int x1, int y1) throws Exception {
        for (int i = y0 + 1; i < y1; i++) {
            tileDrawer.render(Tile.WALL_V, x0, i);
            tileDrawer.render(Tile.WALL_V, x1, i);
        }
        for (int i = x0 + 1; i < x1; i++) {
            tileDrawer.render(Tile.WALL_H, i, y0);
            tileDrawer.render(Tile.WALL_H, i, y1);
        }
        tileDrawer.render(Tile.WALL_J, x0, y0);
        tileDrawer.render(Tile.WALL_J, x1, y0);
        tileDrawer.render(Tile.WALL_J, x0, y1);
        tileDrawer.render(Tile.WALL_J, x1, y1);
    }

    private void drawTetrimino(Tetrimino tetrimino, Vector3f position) throws Exception {
        if (tetrimino == null) {
            return;
        }

        TileDrawer drawer;
        int offsetX = 0;
        int offsetY = 0;
        if (tetrimino.getWidth() == 4) {
            drawer = pieceDrawer4x4;
        } else if (tetrimino.getWidth() == 3) {
            drawer = pieceDrawer3x3;
        } else if (tetrimino.getWidth() == 2) {
            offsetX = 1;
            offsetY = 1;
            drawer = pieceDrawer4x4;
        } else {
            throw new Exception("Incompatible Tetrimino Size");
        }

        drawer.getOrigin().set(position);

        for (int iy = 0; iy < tetrimino.getHeight(); iy++) {
            for (int ix = 0; ix < tetrimino.getWidth(); ix++) {
                if (tetrimino.getPieces()[iy][ix]) {
                    drawer.render(tetrimino.getTileType(), ix + offsetX, iy + offsetY);
                }
            }
        }
    }
}
