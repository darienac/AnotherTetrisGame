package game;

import model.Tetrimino;

import java.util.LinkedList;
import java.util.Queue;

public class RandomPieceGenerator {
    private Queue<Tetrimino> pieces;

    public RandomPieceGenerator() {
        pieces = new LinkedList<>();
    }

    private void addBag() {
        LinkedList<Integer> bag = new LinkedList<>();
        for (int i = 0; i < 7; i++) {
            bag.add(i);
        }
        for (int i = 0; i < 7; i++) {
            int choiceId = (int) Math.floor(Math.random() * bag.size());
            pieces.add(new Tetrimino(bag.get(choiceId)));
            bag.remove(choiceId);
        }
    }

    public Tetrimino nextPiece() {
        if (pieces.size() == 0) {
            addBag();
        }
        return pieces.remove();
    }
}
