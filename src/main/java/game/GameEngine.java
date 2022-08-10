package game;

import game.GameState;
import model.Tetrimino;
import model.Tile;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import render.LineClearMessage;
import render.Scene;
import startup.GameWindow;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs independent of a consistent time interval, grabs time from GLFW.glfwGetTime()
 */
public class GameEngine {
    private static final int MOVE_RESET_LIMIT = 15;
    private static final double GAME_SPEED = 0.05;

    private GameState state;
    private Scene scene;
    private GameWindow window;
    private Controls controls;
    private double lastTime;
    private double lastTimeStepFall;
    private double lastTimeStepGame;
    private int fallSteps;
    private int gameSteps;
    private int dirFramesHeld;
    private int turnDirFramesHeld;
    private int dirLast;
    private int turnDirLast;
    private int stepsPieceOnGround;
    private int moveResetCount;
    private boolean lastMoveIsRotate;
    private int lastSrsNum;

    public GameEngine(GameState state, Scene scene, GameWindow window) {
        controls = new Controls();
        lastTime = GLFW.glfwGetTime();
        lastTimeStepFall = lastTime;
        lastTimeStepGame = lastTime;
        dirLast = 0;
        turnDirLast = 0;
        fallSteps = 0;
        gameSteps = 0;
        stepsPieceOnGround = 0;
        moveResetCount = 0;
        lastMoveIsRotate = false;
        lastSrsNum = -1;

        GLFW.glfwSetKeyCallback(window.getWindowId(), new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                controls.getKeyInput(key, action);
            }
        });

        this.state = state;
        this.scene = scene;
        respawnPiece();
        updateGameSpeed();
    }

    public void run() {
        double time = GLFW.glfwGetTime();
        double delta = time - lastTime;
        int stepsFall = (int) ((time - lastTimeStepFall) / state.getGameSpeed());
        lastTimeStepFall = lastTimeStepFall + stepsFall * state.getGameSpeed();
        int stepsGame = (int) ((time - lastTimeStepGame) / GAME_SPEED);
        lastTimeStepGame = lastTimeStepGame + stepsGame * GAME_SPEED;
        lastTime = time;

        Tetrimino gamePiece = state.getGamePiece();

        controls.readGamepadInputs(GLFW.GLFW_JOYSTICK_1);

        if (controls.turnLeft) {
            controls.turnLeft = false;
            gamePiece.setRotation(gamePiece.getRotation() - 1);
            if (tryWallKicks(-1)) {
                if (state.isPieceOnGround() && moveResetCount < MOVE_RESET_LIMIT) {
                    moveResetCount++;
                    stepsPieceOnGround = 0;
                }
                gamePiece.setY(gamePiece.getY() - 1);
                state.setPieceOnGround(!state.isValidTilePos());
                gamePiece.setY(gamePiece.getY() + 1);
                lastMoveIsRotate = true;
            } else {
                gamePiece.setRotation(gamePiece.getRotation() + 1);
            }
        }
        if (controls.turnRight) {
            controls.turnRight = false;
            gamePiece.setRotation(gamePiece.getRotation() + 1);
            if (tryWallKicks(1)) {
                if (state.isPieceOnGround() && moveResetCount < MOVE_RESET_LIMIT) {
                    moveResetCount++;
                    stepsPieceOnGround = 0;
                }
                gamePiece.setY(gamePiece.getY() - 1);
                state.setPieceOnGround(!state.isValidTilePos());
                gamePiece.setY(gamePiece.getY() + 1);
                lastMoveIsRotate = true;
            } else {
                gamePiece.setRotation(gamePiece.getRotation() - 1);
            }
        }
        if (controls.up) {
            lastMoveIsRotate = false;
            controls.up = false;
            state.setGameScore(state.getGameScore() + Math.max(gamePiece.getY() - state.getPieceLowestPos(), 0) * 2);
            gamePiece.setY(state.getPieceLowestPos());
            clearPiece();
        }
        if (controls.holdPiece) {
            controls.holdPiece = false;
            if (state.isAllowHold()) {
                state.setAllowHold(false);
                Tetrimino newPiece;
                if (state.getHeldPiece() == null) {
                    newPiece = getNewPiece();
                } else {
                    newPiece = state.getHeldPiece();
                }
                state.setHeldPiece(state.getGamePiece());
                state.setGamePiece(newPiece);
                resetPiecePosition(newPiece);
            }
        }

        scene.getCamera().setPosition(new Vector3f(0.0f + controls.cameraX * 2.5f, 2.0f + controls.cameraY * 2.5f, 10.0f));

        for (int i = 0; i < stepsGame; i++) {
            runStepGame();
        }
        for (int i = 0; i < stepsFall; i++) {
            runStepFall();
        }
    }

    public void runStepFall() {
        Tetrimino gamePiece = state.getGamePiece();

        gamePiece.setY(gamePiece.getY() - 1);
        if (state.isValidTilePos()) {
            lastMoveIsRotate = false;
            gamePiece.setY(gamePiece.getY() - 1);
            state.setPieceOnGround(!state.isValidTilePos());
            gamePiece.setY(gamePiece.getY() + 1);
        } else {
            gamePiece.setY(gamePiece.getY() + 1);
            state.setPieceOnGround(true);
        }
    }

    public void runStepGame() {
        Tetrimino gamePiece = state.getGamePiece();
        int dir;
        int turnDir;

        boolean left = controls.left || controls.leftTap;
        boolean right = controls.right || controls.rightTap;
        controls.leftTap = false;
        controls.rightTap = false;

        if (!state.isPieceOnGround()) {
            stepsPieceOnGround = 0;
        }
        gamePiece.setY(gamePiece.getY() - 1);
        state.setPieceOnGround(!state.isValidTilePos());
        gamePiece.setY(gamePiece.getY() + 1);

        if ((left && right) || (!left && !right)) {
            dir = 0;
        } else if (left) {
            dir = -1;
        } else {
            dir = 1;
        }
        if (dir == 0 || dir != dirLast) {
            dirLast = dir;
            dirFramesHeld = 0;
        } else {
            dirFramesHeld++;
        }

        if ((controls.turnLeft && controls.turnRight) || (!controls.turnLeft && !controls.turnRight)) {
            turnDir = 0;
        } else if (controls.turnLeft) {
            turnDir = -1;
        } else {
            turnDir = 1;
        }
        if (turnDir == 0 || turnDir != turnDirLast) {
            turnDirLast = turnDir;
            turnDirFramesHeld = 0;
        }

        if (dirFramesHeld > 3 || dirFramesHeld == 0) {
            if (dir == -1) {
                gamePiece.setX(gamePiece.getX() - 1);
                if (state.isValidTilePos()) {
                    lastMoveIsRotate = false;
                    if (state.isPieceOnGround()) {
                        gamePiece.setY(gamePiece.getY() - 1);
                        if (state.isValidTilePos()) {
                            state.setPieceOnGround(false);
                        } else if (moveResetCount < MOVE_RESET_LIMIT) {
                            state.setPieceOnGround(false);
                            moveResetCount++;
                        }
                        gamePiece.setY(gamePiece.getY() + 1);
                    }
                } else {
                    gamePiece.setX(gamePiece.getX() + 1);
                }
            } else if (dir == 1) {
                gamePiece.setX(gamePiece.getX() + 1);
                if (state.isValidTilePos()) {
                    lastMoveIsRotate = false;
                    if (state.isPieceOnGround()) {
                        gamePiece.setY(gamePiece.getY() - 1);
                        if (state.isValidTilePos()) {
                            state.setPieceOnGround(false);
                        } else if (moveResetCount < MOVE_RESET_LIMIT) {
                            state.setPieceOnGround(false);
                            moveResetCount++;
                        }
                        gamePiece.setY(gamePiece.getY() + 1);
                    }
                } else {
                    gamePiece.setX(gamePiece.getX() - 1);
                }
            }
        }

        if (controls.down) {
            gamePiece.setY(gamePiece.getY() - 1);
            if (state.isValidTilePos()) {
                lastMoveIsRotate = false;
                state.setGameScore(state.getGameScore() + 1);
            } else {
                gamePiece.setY(gamePiece.getY() + 1);
                if (state.isPieceOnGround()) {
                    clearPiece();
                } else {
                    state.setPieceOnGround(true);
                    stepsPieceOnGround = 0;
                }
            }
        }

        if (state.isPieceOnGround() && (stepsPieceOnGround > 10 || moveResetCount >= MOVE_RESET_LIMIT)) {
            clearPiece();
        }
        if (state.isPieceOnGround()) {
            stepsPieceOnGround++;
        }
    }

    private void updateGameSpeed() {
        int level = state.getGameLevel();
        state.setGameSpeed(Math.pow(0.8 - (level - 1) * 0.007, level - 1));
    }

    private void clearPiece() {
        state.getLineClearMessages().clear();
        checkTSpins();
        state.setSolidTiles(state.getDrawnTiles(state.getBoardWidth(), state.getBoardHeight(), true, false));
        Set<Integer> rows = findRows();
        switch (rows.size()) {
            case 1:
                state.getLineClearMessages().add(LineClearMessage.Message.SINGLE);
                break;
            case 2:
                state.getLineClearMessages().add(LineClearMessage.Message.DOUBLE);
                break;
            case 3:
                state.getLineClearMessages().add(LineClearMessage.Message.TRIPLE);
                break;
            case 4:
                state.getLineClearMessages().add(LineClearMessage.Message.TETRIS);
                break;
        }
        // TODO: Somehow trigger the LineClearMessage object to change it's message and update the timer
        clearRows(rows);
        respawnPiece();
        state.setAllowHold(true);
    }

    private void respawnPiece() {
        state.setGamePiece(getNewPiece());
        resetPiecePosition(state.getGamePiece());
        state.setPieceOnGround(false);
        moveResetCount = 0;
        lastMoveIsRotate = false;
    }

    private Tetrimino getNewPiece() {
        Tetrimino newPiece = state.getNextPieces().removeFirst();
        state.getNextPieces().addLast(state.getPieceGenerator().nextPiece());
        return newPiece;
    }

    private void resetPiecePosition(Tetrimino piece) {
        piece.setPosition((state.getBoardWidth() - state.getGamePiece().getWidth()) / 2, 20 + 2 - state.getGamePiece().getHeight());
        piece.setRotation(0);
    }

    private Set<Integer> findRows() {
        Set<Integer> rows = new HashSet<>();
        Tile[][] tiles = state.getSolidTiles();
        for (int i = 0; i < state.getBoardHeight(); i++) {
            boolean rowFull = true;
            for (int j = 0; j < state.getBoardWidth(); j++) {
                if (tiles[i][j] == null) {
                    rowFull = false;
                    break;
                }
            }
            if (rowFull) {
                rows.add(i);
            }
        }
        return rows;
    }

    private void clearRows(Set<Integer> rows) {
        Tile[][] tiles = state.getSolidTiles();
        int offset = 0;
        for (int row : rows) {
            for (int i = row + offset; i < state.getBoardHeight() - 1; i++) {
                tiles[i] = tiles[i + 1];
            }
            tiles[state.getBoardHeight() - 1] = new Tile[state.getBoardWidth()];
            offset--;
        }
        state.setLinesCleared(state.getLinesCleared() + rows.size());
        if (state.getLinesCleared() / 10 + 1 > state.getGameLevel()) {
            state.setGameLevel(state.getLinesCleared() / 10 + 1);
            updateGameSpeed();
        }
    }

    private boolean tryWallKicks(int turnedDir) {
        Tetrimino gamePiece = state.getGamePiece();
        if (state.isValidTilePos()) {
            lastSrsNum = -1;
            return true;
        }
        if (gamePiece.getId() == 0) {
            if (turnedDir == 1) {
                switch (gamePiece.getRotation()) {
                    case 1:
                        return tryWallKick(-2, 0, 0)
                                || tryWallKick(1, 0, 1)
                                || tryWallKick(-2, -1, 2)
                                || tryWallKick(1, 2, 3);
                    case 2:
                        return tryWallKick(-1, 0, 0)
                                || tryWallKick(2, 0, 1)
                                || tryWallKick(-1, 2, 2)
                                || tryWallKick(2, -1, 3);
                    case 3:
                        return tryWallKick(2, 0, 0)
                                || tryWallKick(-1, 0, 1)
                                || tryWallKick(2, 1, 2)
                                || tryWallKick(-1, -2, 3);
                    case 0:
                        return tryWallKick(1, 0, 0)
                                || tryWallKick(-2, 0, 1)
                                || tryWallKick(1, -2, 2)
                                || tryWallKick(-2, 1, 3);
                }
            } else if (turnedDir == -1) {
                switch (gamePiece.getRotation()) {
                    case 0:
                        return tryWallKick(2, 0, 0)
                                || tryWallKick(-1, 0, 1)
                                || tryWallKick(2, 1, 2)
                                || tryWallKick(-1, -2, 3);
                    case 1:
                        return tryWallKick(1, 0, 0)
                                || tryWallKick(-2, 0, 1)
                                || tryWallKick(1, -2, 2)
                                || tryWallKick(-2, 1, 3);
                    case 2:
                        return tryWallKick(-2, 0, 0)
                                || tryWallKick(1, 0, 1)
                                || tryWallKick(-2, -1, 2)
                                || tryWallKick(1, 2, 3);
                    case 3:
                        return tryWallKick(-1, 0, 0)
                                || tryWallKick(2, 0, 1)
                                || tryWallKick(-1, 2, 2)
                                || tryWallKick(2, -1, 3);
                }
            }
        } else {
            if (turnedDir == 1) {
                switch (gamePiece.getRotation()) {
                    case 1:
                        return tryWallKick(-1, 0, 0)
                                || tryWallKick(-1, 1, 1)
                                || tryWallKick(0, -2, 2)
                                || tryWallKick(-1, -2, 3);
                    case 2:
                        return tryWallKick(1, 0, 0)
                                || tryWallKick(1, -1, 1)
                                || tryWallKick(0, 2, 2)
                                || tryWallKick(1, 2, 3);
                    case 3:
                        return tryWallKick(1, 0, 0)
                                || tryWallKick(1, 1, 1)
                                || tryWallKick(0, -2, 2)
                                || tryWallKick(1, -2, 3);
                    case 0:
                        return tryWallKick(-1, 0, 0)
                                || tryWallKick(-1, -1, 1)
                                || tryWallKick(0, 2, 2)
                                || tryWallKick(-1, 2, 3);
                }
            } else if (turnedDir == -1) {
                switch (gamePiece.getRotation()) {
                    case 0:
                        return tryWallKick(1, 0, 0)
                                || tryWallKick(1, -1, 1)
                                || tryWallKick(0, 2, 2)
                                || tryWallKick(1, 2, 3);
                    case 1:
                        return tryWallKick(-1, 0, 0)
                                || tryWallKick(-1, 1, 1)
                                || tryWallKick(0, -2, 2)
                                || tryWallKick(-1, -2, 3);
                    case 2:
                        return tryWallKick(-1, 0, 0)
                                || tryWallKick(-1, -1, 1)
                                || tryWallKick(0, 2, 2)
                                || tryWallKick(-1, 2, 3);
                    case 3:
                        return tryWallKick(1, 0, 0)
                                || tryWallKick(1, 1, 1)
                                || tryWallKick(0, -2, 2)
                                || tryWallKick(1, -2, 3);
                }
            }
        }
        return false;
    }

    private boolean tryWallKick(int x, int y, int srsNum) {
        lastSrsNum = srsNum;
        Tetrimino gamePiece = state.getGamePiece();
        gamePiece.setX(gamePiece.getX() + x);
        gamePiece.setY(gamePiece.getY() + y);
        if (state.isValidTilePos()) {
            return true;
        }
        gamePiece.setX(gamePiece.getX() - x);
        gamePiece.setY(gamePiece.getY() - y);
        return false;
    }

    private void checkTSpins() {
        if (!lastMoveIsRotate) {
            return;
        }
        Tetrimino gamePiece = state.getGamePiece();
        if (gamePiece.getId() != 5) {
            return;
        }

        boolean[] corners = new boolean[4];
        Tile[][] tiles = state.getSolidTiles();
        corners[0] = tiles[0][0] != null;
        corners[1] = tiles[0][3] != null;
        corners[2] = tiles[3][0] != null;
        corners[3] = tiles[3][3] != null;
        int cornerCount = 0;
        for (int i = 0; i < corners.length; i++) {
            if (corners[i]) {
                cornerCount++;
            }
        }
        if (cornerCount < 3) {
            return;
        }

        if (cornerCount == 4) {
            onTSpin();
            return;
        }

        if (lastSrsNum == 3) {
            onTSpin();
        }

        if (gamePiece.getRotation() == 0 && corners[2] && corners[3]) {
            onTSpin();
            return;
        }
        if (gamePiece.getRotation() == 1 && corners[1] && corners[3]) {
            onTSpin();
            return;
        }
        if (gamePiece.getRotation() == 2 && corners[0] && corners[1]) {
            onTSpin();
            return;
        }
        if (gamePiece.getRotation() == 3 && corners[0] && corners[2]) {
            onTSpin();
            return;
        }

        onMiniTSpin();
    }

    private void onTSpin() {
        state.getLineClearMessages().add(LineClearMessage.Message.TSPIN);
    }

    private void onMiniTSpin() {
        state.getLineClearMessages().add(LineClearMessage.Message.MINI);
        state.getLineClearMessages().add(LineClearMessage.Message.TSPIN);
    }
}

// TODO: Score stuff:
//  Single/Mini T-Spin		                        100×level
//  Mini T-Spin Single		                        200×level
//  Double		                                    300×level
//  T-Spin/Mini T-Spin Double		                400×level
//  Triple		                                    500×level
//  B2B Mini T-Spin Double		                    600×level
//  Tetris/T-Spin Single		                    800×level
//  B2B T-Spin Single/B2B Tetris/T-Spin Double		1,200×level
//  T-Spin Triple		                            1,600×level
//  B2B T-Spin Double		                        1,800×level
//  B2B T-Spin Triple		                        2,400×level
//  Combo		                                    (move value+50)×level
//  Soft drop		                                1 point per cell        Done
//  Hard drop		                                2 points per cell       Done