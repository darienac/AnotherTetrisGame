package model;

import static model.Tile.*;

public class Tetrimino {
    private static final boolean F = false;
    private static final boolean T = true;
    private static final boolean[][][] pieces = {
        {
            {F, F, F, F},
            {F, F, F, F},
            {T, T, T, T},
            {F, F, F, F},
        },
        {
            {F, F, F},
            {T, T, T},
            {T, F, F},
        },
        {
            {F, F, F},
            {T, T, T},
            {F, F, T}
        },
        {
            {T, T},
            {T, T},
        },
        {
            {F, F, F},
            {T, T, F},
            {F, T, T},
        },
        {
            {F, F, F},
            {T, T, T},
            {F, T, F},
        },
        {
            {F, F, F},
            {F, T, T},
            {T, T, F},
        },
    };
    private static final Tile[] tColors = {BLOCK_CYAN, BLOCK_BLUE, BLOCK_ORANGE, BLOCK_YELLOW, BLOCK_GREEN, BLOCK_PURPLE, BLOCK_RED};

    private int id;
    private int x;
    private int y;
    private int rotation = 0;

    public Tetrimino(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation % 4;
        if (this.rotation < 0) {
            this.rotation += 4;
        }
    }

    public Tile getTileType() {
        return tColors[id];
    }

    public int getWidth() {
        return pieces[id][0].length;
    }

    public int getHeight() {
        return pieces[id].length;
    }

    public boolean[][] getPieces() {
        return pieces[id];
    }
}