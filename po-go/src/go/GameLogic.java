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

    private class Group {
        ArrayList<Pair<Integer, Integer>> group = new ArrayList<>();
        int liberties = 0;
    }

    private Group collectGroup(Board board, int i, int j, boolean[][] seen) {
        Group ret = new Group();
        boolean[][] libertiesSeen = boolMatrix(board);
        assert indicesOk(board, i, j) && board.get(i, j).isPresent(); // TODO
        assert !seen[i][j];
        Stone color = board.get(i, j).get();
        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(i, j));
        ret.group.add(new Pair<>(i, j));
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> curr = queue.remove();
            seen[curr.x][curr.y] = true;
            for (Pair<Integer, Integer> d: offsets) {
                int x = curr.x + d.x, y = curr.y + d.y;

                if (!indicesOk(board, x, y)) continue;

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

    private class Territory {
        ArrayList<Pair<Integer, Integer>> territory = new ArrayList<>();
        Optional<Stone> captor = Optional.empty();
    }

    private Territory collectTerritory(Board board, int i, int j, boolean[][] seen) {
        assert indicesOk(board, i, j) && board.get(i, j).isEmpty(); // TODO
        assert !seen[i][j];

        Territory ret = new Territory();

        // seenAnyColor && captor.isEmpty() => nie ma jednoznacznego koloru, który przejął to terytorium
        boolean seenAnyColor = false;

        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(i, j));
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> curr = queue.remove();
            for (Pair<Integer, Integer> d : offsets) {
                int x = curr.x + d.x, y = curr.y + d.y;
                if (!indicesOk(board, x, y)) continue;
                if (board.get(x, y).isEmpty()) {
                    if (!seen[x][y]) {
                        queue.add(new Pair<>(x, y));
                        seen[x][y] = true;
                    }
                } else {
                    if (seenAnyColor) {
                        if (ret.captor.isPresent()) {
                            if (!ret.captor.get().equals(board.get(x, y).get())) // else the current captor is ok
                                ret.captor = Optional.empty();
                        } // else there is no proper captor
                    } else {
                        seenAnyColor = true;
                        ret.captor = Optional.of(board.get(x, y).get());
                    }
                }
            }
        }
        return ret;
    }

    public boolean movePossible(Board board, int i, int j, Stone color) {
        if (!indicesOk(board, i, j)) return false;
        if (board.get(i, j).isPresent()) return false;

        board.getBoard()[i][j] = Optional.of(color);
        Group group = collectGroup(board, i, j, boolMatrix(board));
        board.getBoard()[i][j] = Optional.empty();

        return group.liberties != 0;
    }

    public ArrayList<Pair<Integer, Integer>> captured(Board board) {
        ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
        boolean[][] seen = boolMatrix(board);
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.get(i, j).isPresent() && !seen[i][j]) {
                    Group group = collectGroup(board, i, j, seen);
                    if (group.liberties == 0)
                        ret.addAll(group.group);
                }
            }
        }
        return ret;
    }

    public ArrayList<Territory> capturedTerritories(Board board) {
        ArrayList<Territory> ret = new ArrayList<>();
        boolean[][] seen = boolMatrix(board);
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.get(i, j).isEmpty() && !seen[i][j]) {
                    Territory territory = collectTerritory(board, i, j, seen);
                    if (territory.captor.isPresent())
                        ret.add(territory);
                }
            }
        }
        return ret;
    }
}
