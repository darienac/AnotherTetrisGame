package game;

import game.GameState;
import model.Tetrimino;
import model.Tile;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import render.Scene;
import startup.GameWindow;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs independent of a consistent time interval, grabs time from GLFW.glfwGetTime()
 */
public class GameEngine {
    private GameState state;
    private Scene scene;
    private GameWindow window;
    private Controls controls;
    private double lastTime;
    private double lastTimeStep;
    private int dirFramesHeld;
    private int turnDirFramesHeld;
    int dirLast;
    int turnDirLast;

    public GameEngine(GameState state, Scene scene, GameWindow window) {
        controls = new Controls();
        lastTime = GLFW.glfwGetTime();
        lastTimeStep = lastTime;
        dirLast = 0;
        turnDirLast = 0;

        GLFW.glfwSetKeyCallback(window.getWindowId(), new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                controls.getKeyInput(key, action);
            }
        });

        this.state = state;
        this.scene = scene;
        respawnPiece();
    }

    public void run() {
        double time = GLFW.glfwGetTime();
        double delta = time - lastTime;
        int steps = (int) ((time - lastTimeStep) / state.getGameSpeed());
        lastTimeStep = lastTimeStep + steps * state.getGameSpeed();
        lastTime = time;

        Tetrimino gamePiece = state.getGamePiece();

        controls.readGamepadInputs(GLFW.GLFW_JOYSTICK_1);

        if (controls.turnLeft) {
            controls.turnLeft = false;
            gamePiece.setRotation(gamePiece.getRotation() - 1);
            if (!tryWallKicks(-1)) {
                gamePiece.setRotation(gamePiece.getRotation() + 1);
            }
        }
        if (controls.turnRight) {
            controls.turnRight = false;
            gamePiece.setRotation(gamePiece.getRotation() + 1);
            if (!tryWallKicks(1)) {
                gamePiece.setRotation(gamePiece.getRotation() - 1);
            }
        }
        if (controls.up) {
            controls.up = false;
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

        for (int i = 0; i < steps; i++) {
            runStep();
        }
    }

    public void runStep() {
        Tetrimino gamePiece = state.getGamePiece();
        int gameTicks = state.getGameTicks();
        int dir;
        int turnDir;

        if ((controls.left && controls.right) || (!controls.left && !controls.right)) {
            dir = 0;
        } else if (controls.left) {
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

        if (dirFramesHeld > 7 || dirFramesHeld % 3 == 0) {
            if (dir == -1) {
                gamePiece.setX(gamePiece.getX() - 1);
                if (!state.isValidTilePos()) {
                    gamePiece.setX(gamePiece.getX() + 1);
                }
            } else if (dir == 1) {
                gamePiece.setX(gamePiece.getX() + 1);
                if (!state.isValidTilePos()) {
                    gamePiece.setX(gamePiece.getX() - 1);
                }
            }
        }
        if (state.getGameTicks() % 20 == 0 || controls.down) {
            gamePiece.setY(gamePiece.getY() - 1);
            if (!state.isValidTilePos()) {
                gamePiece.setY(gamePiece.getY() + 1);
                if (state.getGameTicks() % 20 == 0) {
                    clearPiece();
                }
            }
        }

        state.setGameTicks(gameTicks + 1);
    }

    private void clearPiece() {
        state.setSolidTiles(state.getDrawnTiles(state.getBoardWidth(), state.getBoardHeight(), true, false));
        Set<Integer> rows = findRows();
        clearRows(rows);
        respawnPiece();
        state.setAllowHold(true);
    }

    private void respawnPiece() {
        state.setGamePiece(getNewPiece());
        resetPiecePosition(state.getGamePiece());
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
    }

    private boolean tryWallKicks(int turnedDir) {
        Tetrimino gamePiece = state.getGamePiece();
        if (state.isValidTilePos()) {
            return true;
        }
        if (gamePiece.getId() == 0) {
            if (turnedDir == 1) {
                switch (gamePiece.getRotation()) {
                    case 1:
                        return tryWallKick(-2, 0)
                                || tryWallKick(1, 0)
                                || tryWallKick(-2, -1)
                                || tryWallKick(1, 2);
                    case 2:
                        return tryWallKick(-1, 0)
                                || tryWallKick(2, 0)
                                || tryWallKick(-1, 2)
                                || tryWallKick(2, -1);
                    case 3:
                        return tryWallKick(2, 0)
                                || tryWallKick(-1, 0)
                                || tryWallKick(2, 1)
                                || tryWallKick(-1, -2);
                    case 0:
                        return tryWallKick(1, 0)
                                || tryWallKick(-2, 0)
                                || tryWallKick(1, -2)
                                || tryWallKick(-2, 1);
                }
            } else if (turnedDir == -1) {
                switch (gamePiece.getRotation()) {
                    case 0:
                        return tryWallKick(2, 0)
                                || tryWallKick(-1, 0)
                                || tryWallKick(2, 1)
                                || tryWallKick(-1, -2);
                    case 1:
                        return tryWallKick(1, 0)
                                || tryWallKick(-2, 0)
                                || tryWallKick(1, -2)
                                || tryWallKick(-2, 1);
                    case 2:
                        return tryWallKick(-2, 0)
                                || tryWallKick(1, 0)
                                || tryWallKick(-2, -1)
                                || tryWallKick(1, 2);
                    case 3:
                        return tryWallKick(-1, 0)
                                || tryWallKick(2, 0)
                                || tryWallKick(-1, 2)
                                || tryWallKick(2, -1);
                }
            }
        } else {
            if (turnedDir == 1) {
                switch (gamePiece.getRotation()) {
                    case 1:
                        return tryWallKick(-1, 0)
                                || tryWallKick(-1, 1)
                                || tryWallKick(0, -2)
                                || tryWallKick(-1, -2);
                    case 2:
                        return tryWallKick(1, 0)
                                || tryWallKick(1, -1)
                                || tryWallKick(0, 2)
                                || tryWallKick(1, 2);
                    case 3:
                        return tryWallKick(1, 0)
                                || tryWallKick(1, 1)
                                || tryWallKick(0, -2)
                                || tryWallKick(1, -2);
                    case 0:
                        return tryWallKick(-1, 0)
                                || tryWallKick(-1, -1)
                                || tryWallKick(0, 2)
                                || tryWallKick(-1, 2);
                }
            } else if (turnedDir == -1) {
                switch (gamePiece.getRotation()) {
                    case 0:
                        return tryWallKick(1, 0)
                                || tryWallKick(1, -1)
                                || tryWallKick(0, 2)
                                || tryWallKick(1, 2);
                    case 1:
                        return tryWallKick(-1, 0)
                                || tryWallKick(-1, 1)
                                || tryWallKick(0, -2)
                                || tryWallKick(-1, -2);
                    case 2:
                        return tryWallKick(-1, 0)
                                || tryWallKick(-1, -1)
                                || tryWallKick(0, 2)
                                || tryWallKick(-1, 2);
                    case 3:
                        return tryWallKick(1, 0)
                                || tryWallKick(1, 1)
                                || tryWallKick(0, -2)
                                || tryWallKick(1, -2);
                }
            }
        }
        return false;
    }

    private boolean tryWallKick(int x, int y) {
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
}
