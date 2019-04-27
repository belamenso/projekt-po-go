package go;

import util.Pair;

import java.util.ArrayList;
import java.util.Optional;

import static go.GameLogic.gameLogic;

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
    private boolean inProgress;
    private int capturedByWhite = 0;
    private int capturedByBlack = 0;
    private double komi;

    private void updateCapturedCount(Stone playerCapturing, int n) {
        if (playerCapturing.equals(Stone.White))
            capturedByWhite += n;
        else
            capturedByBlack += n;
    }

    private Move getLastMove() {
        assert !move.isEmpty();
        return move.get(move.size() - 1);
    }

    //////////////////// public interface

    GameplayManager(Board.BoardSize size, double komi) {
        assert Math.floor(komi) != komi; // needed to break ties
        this.komi = komi;
        boards.add(new Board(size));
    }

    GameplayManager() {
        this(Board.BoardSize.Size9, 6.5);
    }

    public double getKomi() { return komi; }

    public int getCapturedBy(Stone color) {
        return color.equals(Stone.White) ? capturedByWhite : capturedByBlack;
    }

    public Board getBoard() {
        return boards.get(boards.size() - 1);
    }

    boolean inProgress() { return inProgress; }

    boolean finished() { return !inProgress(); }

    Stone nextTurn() {
        assert inProgress(); // TODO
        if (move.isEmpty() || getLastMove().player.equals(Stone.White))
            return Stone.Black;
        else
            return Stone.White;
    }

    public void registerMove(Move m) {
        assert m.player == nextTurn(); // TODO
        if (m instanceof Pass) {
            if (!move.isEmpty() && getLastMove() instanceof Pass)
                inProgress = false;
            move.add(m);
        } else {
            StonePlacement placement = (StonePlacement) m;
            assert gameLogic.movePossible(getBoard(), placement.position.x, placement.position.y, placement.player); // TODO

            // compute the next board
            Board nextBoard = getBoard().cloneBoard();
            nextBoard.getBoard()[placement.position.x][placement.position.y] = Optional.of(m.player);
            ArrayList<Pair<Integer, Integer>> captured = gameLogic.captured(nextBoard, m.player);
            updateCapturedCount(m.player, captured.size());
            for (Pair<Integer, Integer> pos : captured)
                nextBoard.getBoard()[pos.x][pos.y] = Optional.empty();

            move.add(placement);
            boards.add(nextBoard);
        }
    }

    class Result {
        Stone winner;
        double whitePoints;
        double blackPoints;
    }

    public Result result() {
        assert finished(); // TODO
        Result ret = new Result();
        ArrayList<GameLogic.Territory> territories = gameLogic.capturedTerritories(getBoard());
        for (GameLogic.Territory t : territories) {
            if (t.captor.isPresent()) {
                if (t.captor.get().equals(Stone.White))
                    ret.whitePoints += t.territory.size();
                else
                    ret.blackPoints += t.territory.size();
            }
        }
        ret.whitePoints -= capturedByBlack;
        ret.blackPoints -= capturedByWhite;
        assert ret.whitePoints != ret.blackPoints; // ties are not possible
        ret.winner = ret.whitePoints > ret.blackPoints ? Stone.White : Stone.Black;
        return ret;
    }
}
