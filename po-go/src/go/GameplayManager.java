package go;

import util.Pair;

import java.util.ArrayList;
import java.util.Optional;

public class GameplayManager {

    static abstract class Move {
        Stone player;

        Move(Stone color) {
            player = color;
        }
    }

    static class Pass extends Move {
        Pass(Stone color) { super(color); }
    }

    static class StonePlacement extends Move {
        Pair<Integer, Integer> position;

        StonePlacement(Stone color, int x, int y) {
            super(color);
            position = new Pair<>(x, y);
            // TODO error handling
        }
    }

    private ArrayList<Board> boards = new ArrayList<>();
    private ArrayList<Move> move = new ArrayList<>();

    public GameplayManager(Board.BoardSize size) {
        boards.add(new Board(size));
    }
}
