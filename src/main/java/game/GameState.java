package game;

import model.Message;
import model.Tetrimino;
import model.Tile;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import render.LineClearMessage;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static model.Tile.*;

/**
 * Stores all information about the game state used by the game engine and renderer
 */
public class GameState {
    private static final int GAME_WIDTH = 10;
    private static final int GAME_HEIGHT = 40;

    private Mode mode;
    private int bgOption = 1;


    private Tile[][] solidTiles;

    private Tetrimino gamePiece;
    private Tetrimino heldPiece;
    private boolean allowHold;
    private Deque<Tetrimino> nextPieces;
    private RandomPieceGenerator pieceGenerator;

    private double gameSpeed;
    private int gameScore;
    private int gameLevel;
    private int linesCleared;
    private boolean pieceOnGround;
    private LinkedList<Message> lineClearMessages;
    private double lineClearMessagesTimestamp;
    private int comboStreak;
    private boolean lineClear;
    private double lineClearStart;
    private Set<Integer> clearRows;
    private CameraPushDirection cameraPushDirection;
    private CameraPushDirection lastCameraPushDirection;
    private double cameraPushTimestamp;
    private Vector2f cameraControls;

    public Lock lock = new ReentrantLock();

    public GameState() {
        setMode(Mode.GAME);
    }

    public Tile[][] getSolidTiles() {
        return solidTiles;
    }

    public void setSolidTiles(Tile[][] tiles) {
        this.solidTiles = tiles;
    }

    public int getBoardWidth() {
        return (solidTiles.length > 0) ? solidTiles[0].length : 0;
    }

    public int getBoardHeight() {
        return solidTiles.length;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        switch (mode) {
            case GAME:
                gamePiece = new Tetrimino(0);
                gamePiece.setPosition(0, GAME_HEIGHT - 4 - gamePiece.getHeight());

                heldPiece = null;
                allowHold = true;

                nextPieces = new LinkedList<>();
                pieceGenerator = new RandomPieceGenerator();
                for (int i = 0; i < 4; i++) {
                    nextPieces.add(pieceGenerator.nextPiece());
                }

                solidTiles = new Tile[GAME_HEIGHT][GAME_WIDTH];

                gameScore = 0;
                gameLevel = 1;
                linesCleared = 0;
                pieceOnGround = false;
                lineClearMessages = new LinkedList<>();
                lineClearMessagesTimestamp = GLFW.glfwGetTime();
                comboStreak = -1;
                lineClearStart = 0.0;
                lineClear = false;
                clearRows = null;

                cameraPushDirection = CameraPushDirection.NONE;
                lastCameraPushDirection = CameraPushDirection.NONE;
                cameraPushTimestamp = 0.0;
                cameraControls = new Vector2f(0.0f, 0.0f);

                break;
        }
    }

    public int getBgOption() {
        return bgOption;
    }

    public void setBgOption(int bgOption) {
        this.bgOption = bgOption;
    }

    public Tetrimino getGamePiece() {
        return gamePiece;
    }

    public void setGamePiece(Tetrimino gamePiece) {
        this.gamePiece = gamePiece;
    }

    public Tetrimino getHeldPiece() {
        return heldPiece;
    }

    public void setHeldPiece(Tetrimino heldPiece) {
        this.heldPiece = heldPiece;
    }

    public boolean isAllowHold() {
        return allowHold;
    }

    public void setAllowHold(boolean allowHold) {
        this.allowHold = allowHold;
    }

    public Deque<Tetrimino> getNextPieces() {
        return nextPieces;
    }

    public void setNextPieces(Deque<Tetrimino> nextPieces) {
        this.nextPieces = nextPieces;
    }

    public RandomPieceGenerator getPieceGenerator() {
        return pieceGenerator;
    }

    public void setPieceGenerator(RandomPieceGenerator pieceGenerator) {
        this.pieceGenerator = pieceGenerator;
    }

    public double getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(double gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public int getGameScore() {
        return gameScore;
    }

    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public void setLinesCleared(int linesCleared) {
        this.linesCleared = linesCleared;
    }

    public boolean isPieceOnGround() {
        return pieceOnGround;
    }

    public void setPieceOnGround(boolean pieceOnGround) {
        this.pieceOnGround = pieceOnGround;
    }

    public LinkedList<Message> getLineClearMessages() {
        return lineClearMessages;
    }

    public void setLineClearMessagesTimestamp(double time) {
        lineClearMessagesTimestamp = time;
    }

    public double getLineClearMessagesTimestamp() {
        return lineClearMessagesTimestamp;
    }

    public int getComboStreak() {
        return comboStreak;
    }

    public void setComboStreak(int comboStreak) {
        this.comboStreak = comboStreak;
    }

    public boolean isLineClear() {
        return lineClear;
    }

    public void setLineClear(boolean lineClear) {
        this.lineClear = lineClear;
    }

    public double getLineClearStart() {
        return lineClearStart;
    }

    public void setLineClearStart(double lineClearStart) {
        this.lineClearStart = lineClearStart;
    }

    public Set<Integer> getClearRows() {
        return clearRows;
    }

    public void setClearRows(Set<Integer> clearRows) {
        this.clearRows = clearRows;
    }

    public CameraPushDirection getCameraPushDirection() {
        return cameraPushDirection;
    }

    public void setCameraPushDirection(CameraPushDirection cameraPushDirection) {
        this.cameraPushDirection = cameraPushDirection;
    }

    public CameraPushDirection getLastCameraPushDirection() {
        return lastCameraPushDirection;
    }

    public void setLastCameraPushDirection(CameraPushDirection lastCameraPushDirection) {
        this.lastCameraPushDirection = lastCameraPushDirection;
    }

    public double getCameraPushTimestamp() {
        return cameraPushTimestamp;
    }

    public void setCameraPushTimestamp(double cameraPushTimestamp) {
        this.cameraPushTimestamp = cameraPushTimestamp;
    }

    public Vector2f getCameraControls() {
        return cameraControls;
    }

    public void setCameraControls(Vector2f cameraControls) {
        this.cameraControls = cameraControls;
    }

    public Tile[][] getDrawnTiles(int width, int height, boolean runSafe, boolean includeExtras) {
        Tile[][] out = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out[y][x] = solidTiles[y][x];
            }
        }
        boolean[][] pieces = gamePiece.getPieces();
        int pieceLowestY = -1;
        if (includeExtras) {
            pieceLowestY = getPieceLowestPos();
        }
        for (int y = 0; y < gamePiece.getHeight(); y++) {
            for (int x = 0; x < gamePiece.getWidth(); x++) {
                int boardX = x + gamePiece.getX();
                int boardY = y + gamePiece.getY();
                int pieceX = -1;
                int pieceY = -1;
                switch (gamePiece.getRotation()) {
                    case 0:
                        pieceX = x;
                        pieceY = y;
                        break;
                    case 1:
                        pieceX = gamePiece.getWidth() - 1 - y;
                        pieceY = x;
                        break;
                    case 2:
                        pieceX = gamePiece.getWidth() - 1 - x;
                        pieceY = gamePiece.getHeight() - 1 - y;
                        break;
                    case 3:
                        pieceX = y;
                        pieceY = gamePiece.getHeight() - 1 - x;
                        break;
                }
                if (!pieces[pieceY][pieceX]) {
                    continue;
                }
                if (includeExtras) {
                    int boardLowestY = pieceLowestY + y;
                    if (!(boardX < 0 || boardLowestY < 0 || boardX >= width || boardLowestY >= height) && out[boardLowestY][boardX] == null) {
                        out[boardLowestY][boardX] = BLOCK_GHOST;
                    }
                }
                if (boardX < 0 || boardY < 0 || boardX >= width || boardY >= height) {
                    if (runSafe) {
                        continue;
                    } else {
                        return null;
                    }
                }
                if (out[boardY][boardX] != null && out[boardY][boardX] != BLOCK_GHOST) {
                    if (runSafe) {
                        continue;
                    } else {
                        return null;
                    }
                }
                out[boardY][boardX] = gamePiece.getTileType();
            }
        }
        return out;
    }

    public int getPieceLowestPos() {
        int yPos = getGamePiece().getY();
        int pos = yPos;
        while (getGamePiece().getY() >= -4) {
            getGamePiece().setY(getGamePiece().getY() - 1);
            if (!isValidTilePos()) {
                getGamePiece().setY(yPos);
                return pos;
            }
            pos--;
        }
        getGamePiece().setY(yPos);
        return yPos;
    }

    public boolean isValidTilePos() {
        return getDrawnTiles(getBoardWidth(), getBoardHeight(), false, false) != null;
    }

    public enum Mode {
        GAME
    }

    public enum CameraPushDirection {
        NONE,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
