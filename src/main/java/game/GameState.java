package game;

import model.Tetrimino;
import model.Tile;

import java.util.Deque;
import java.util.LinkedList;

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
    private int gameTicks;
    private int gameScore;
    private int gameLevel;

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

                nextPieces = new LinkedList<Tetrimino>();
                pieceGenerator = new RandomPieceGenerator();
                for (int i = 0; i < 4; i++) {
                    nextPieces.add(pieceGenerator.nextPiece());
                }

                solidTiles = new Tile[GAME_HEIGHT][GAME_WIDTH];

                gameSpeed = 0.05;
                gameTicks = 0;
                gameScore = 0;
                gameLevel = 1;
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

    public int getGameTicks() {
        return gameTicks;
    }

    public void setGameTicks(int gameTicks) {
        this.gameTicks = gameTicks;
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
}
