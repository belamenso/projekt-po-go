package go;

import util.Pair;

import java.util.Queue;
import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedList;

public enum GameLogic {
    GameLogic;

    private final ArrayList<Pair<Integer, Integer>> offsets;
    {
        offsets = new ArrayList<>(4);
        offsets.add(new Pair<>(0, -1));
        offsets.add(new Pair<>(0, 1));
        offsets.add(new Pair<>(-1, 0));
        offsets.add(new Pair<>(1,0));
    }

    private boolean indicesOk(Board board, int i, int j) {
        return i >= 0 && i < board.getSize() && j >= 0 && j < board.getSize();
    }

    private boolean[][] boolMatrix(Board board) {
        return new boolean[board.getSize()][board.getSize()];
    }

    private class CollectRet {
        ArrayList<Pair<Integer, Integer>> group = new ArrayList<>();
        int liberties = 0;
    }

    private CollectRet collect(Board board, int i, int j, boolean[][] seen) {
        CollectRet ret = new CollectRet();
        boolean[][] libertiesSeen = boolMatrix(board);
        assert board.get(i, j).isPresent(); // TODO
        Stone color = board.get(i, j).get();
        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(i, j));
        ret.group.add(new Pair<>(i, j));
        outer: while (!queue.isEmpty()) {
            Pair<Integer, Integer> curr = queue.remove();
            seen[curr.x][curr.y] = true;
            for (Pair<Integer, Integer> d: offsets) {
                int x = curr.x + d.x, y = curr.y + d.y;

                if (!indicesOk(board, x, y)) continue outer;

                if (board.get(x, y).isEmpty()) {
                    if (!libertiesSeen[x][y]) {
                        ret.liberties++;
                        libertiesSeen[x][y] = true;
                    }
                } else if (board.get(x, y).get() == color) {
                    if (!seen[x][y]) {
                        queue.add(new Pair<>(x, y));
                        ret.group.add(new Pair<>(x, y));
                        seen[x][y] = true;
                    }
                }
            }
        }
        return ret;
    }

    public boolean movePossible(Board board, int i, int j, Stone color) {
        if (!indicesOk(board, i, j)) return false;
        if (board.get(i, j).isPresent()) return false;

        board.getBoard()[i][j] = new Optional<>(color);
        CollectRet group = collect(board, i, j, boolMatrix(board));
        board.getBoard()[i][j] = Optional.empty();

        return group.liberties != 0;
    }

    public ArrayList<Pair<Integer, Integer>> captured(Board board) {
        boolean[][] seen = boolMatrix(board);
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.get(i, j).isPresent() && !seen[i][j]) {
                    CollectRet group = collect(board, i, j, seen);
                    // TODO czy zawsze tylko jedna grupa na raz jest złapana?
                    if (group.liberties == 0)
                        return group.group;
                }
            }
        }
        return new ArrayList<>();
    }
}
