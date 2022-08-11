package game;

import game.GameState;
import model.Message;
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

import java.util.*;

/**
 * Runs independent of a consistent time interval, grabs time from GLFW.glfwGetTime()
 */
public class GameEngine {
    private static final int MOVE_RESET_LIMIT = 15;
    private static final double GAME_SPEED = 0.05;
    private static final Set<LinkedList<Message>> difficultClears = new HashSet<>();
    private static final Map<LinkedList<Message>, Integer> clearPoints = new HashMap<>();

    static {
        difficultClears.add(new LinkedList<>(Arrays.asList(Message.TETRIS)));
        difficultClears.add(new LinkedList<>(Arrays.asList(Message.MINI, Message.TSPIN, Message.SINGLE)));
        difficultClears.add(new LinkedList<>(Arrays.asList(Message.TSPIN, Message.SINGLE)));
        difficultClears.add(new LinkedList<>(Arrays.asList(Message.MINI, Message.TSPIN, Message.DOUBLE)));
        difficultClears.add(new LinkedList<>(Arrays.asList(Message.TSPIN, Message.DOUBLE)));
        difficultClears.add(new LinkedList<>(Arrays.asList(Message.TSPIN, Message.TRIPLE)));

        clearPoints.put(new LinkedList<>(Arrays.asList(Message.SINGLE)), 100);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.DOUBLE)), 300);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.TRIPLE)), 500);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.TETRIS)), 800);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.MINI, Message.TSPIN)), 100);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.TSPIN)), 400);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.MINI, Message.TSPIN, Message.SINGLE)), 200);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.TSPIN, Message.SINGLE)), 800);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.MINI, Message.TSPIN, Message.DOUBLE)), 400);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.TSPIN, Message.DOUBLE)), 1200);
        clearPoints.put(new LinkedList<>(Arrays.asList(Message.TSPIN, Message.TRIPLE)), 1600);
    }

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
    private boolean b2bViable;

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
        b2bViable = false;

        state.setGameLevel(16);

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
            if (state.getPieceLowestPos() != state.getGamePiece().getY()) {
                lastMoveIsRotate = false;
            }
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
                if (!state.isPieceOnGround()) {
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
        state.setComboStreak(state.getComboStreak() + 1);
        switch (rows.size()) {
            case 0:
                state.setComboStreak(-1);
                break;
            case 1:
                state.getLineClearMessages().add(Message.SINGLE);
                break;
            case 2:
                state.getLineClearMessages().add(Message.DOUBLE);
                break;
            case 3:
                state.getLineClearMessages().add(Message.TRIPLE);
                break;
            case 4:
                state.getLineClearMessages().add(Message.TETRIS);
                break;
        }
        int points = 0;
        if (clearPoints.containsKey(state.getLineClearMessages())) {
            points = clearPoints.get(state.getLineClearMessages()) * state.getGameLevel();
        }
        if (difficultClears.contains(state.getLineClearMessages())) {
            if (b2bViable) {
                points *= 1.5;
                state.getLineClearMessages().addFirst(Message.B2B);
            }
            b2bViable = true;
        } else if (rows.size() > 0) {
            b2bViable = false;
        }
        if (state.getComboStreak() > 0) {
            state.getLineClearMessages().add(Message.COMBO);
            points += state.getComboStreak() * 50 * state.getGameLevel();
        }
        state.setGameScore(state.getGameScore() + points);
        state.setLineClearMessagesTimestamp(GLFW.glfwGetTime());
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
        stepsPieceOnGround = 0;
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
        System.out.println("checking for tspins");

        boolean[] corners = new boolean[4];
        Tile[][] tiles = state.getSolidTiles();
        int x = gamePiece.getX();
        int y = gamePiece.getY();
        corners[0] = checkTileSolidSafe(tiles, x, y);
        corners[1] = checkTileSolidSafe(tiles, x, y + 2);
        corners[2] = checkTileSolidSafe(tiles, x + 2, y);
        corners[3] = checkTileSolidSafe(tiles, x + 2, y + 2);
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
            return;
        }

        // TODO: fix these
        if (gamePiece.getRotation() == 0 && corners[1] && corners[3]) {
            onTSpin();
            return;
        }
        if (gamePiece.getRotation() == 1 && corners[2] && corners[3]) {
            onTSpin();
            return;
        }
        if (gamePiece.getRotation() == 2 && corners[0] && corners[2]) {
            onTSpin();
            return;
        }
        if (gamePiece.getRotation() == 3 && corners[0] && corners[1]) {
            onTSpin();
            return;
        }

        onMiniTSpin();
    }

    private boolean checkTileSolidSafe(Tile[][] tiles, int x, int y) {
        if (y < 0 || y >= tiles.length) {
            return true;
        }
        Tile[] row = tiles[y];
        if (x < 0 || x >= row.length) {
            return true;
        }
        return row[x] != null;
    }

    private void onTSpin() {
        state.getLineClearMessages().add(Message.TSPIN);
    }

    private void onMiniTSpin() {
        state.getLineClearMessages().add(Message.MINI);
        state.getLineClearMessages().add(Message.TSPIN);
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