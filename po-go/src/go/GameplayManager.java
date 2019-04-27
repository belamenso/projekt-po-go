package go;

import util.Pair;

import java.util.ArrayList;
import java.util.Optional;

import static go.GameLogic.gameLogic;

/**
 * Klasa przechowująca stan gry oraz jej pełną historię
 * ruch o indeksie i w moves przechodzi ze stanu boards.get(i) do boards.get(i+1)
 * (ponieważ na początku boards jest pusta plansza, ale nie ma zerowego ruchu)
 */
public class GameplayManager {

    /**
     * Reprezentuje jeden ruch w historii gry
     */
    static abstract class Move {
        Stone player;

        Move(Stone color) {
            player = color;
        }
    }

    /**
     * gracz player przepuścił kolejkę
     */
    static class Pass extends Move {
        Pass(Stone color) { super(color); }
    }

    /**
     * Gracz player umieścił swój kamień na pozycji position
     */
    static class StonePlacement extends Move {
        Pair<Integer, Integer> position;

        StonePlacement(Stone color, int x, int y) {
            super(color);
            position = new Pair<>(x, y);
            // TODO error handling
        }
    }

    /**
     * historia gry
     */
    private ArrayList<Board> boards = new ArrayList<>();
    /**
     * historia ruchów
     */
    private ArrayList<Move> moves = new ArrayList<>();
    /**
     * czy gra jest w trakcie?
     */
    private boolean inProgress;
    /**
     * liczba czarnych kameni przejętych przez białe w ciągu gry
     * TODO zamień to na historię
     */
    private int capturedByWhite = 0;
    /**
     * liczba białych kamieni przejętych przez czarne w ciągu gry
     * TODO zamień to na historię
     */
    private int capturedByBlack = 0;
    /**
     * wartość liczbowa dodawana do punktów białych pod koniec gry, ponieważ one nie zaczynały
     * zawsze zachodzi floor(komi) != komi aby nie dopuścić remisów
     */
    private final double komi;

    private void updateCapturedCount(Stone playerCapturing, int n) {
        if (playerCapturing.equals(Stone.White))
            capturedByWhite += n;
        else
            capturedByBlack += n;
    }

    private Move getLastMove() {
        assert !moves.isEmpty();
        return moves.get(moves.size() - 1);
    }

    //////////////////// public interface

    public GameplayManager(Board.BoardSize size, double komi) {
        assert Math.floor(komi) != komi; // needed to break ties
        this.komi = komi;
        boards.add(new Board(size));
    }

    public GameplayManager() {
        this(Board.BoardSize.Size9, 6.5);
    }

    public double getKomi() { return komi; }

    public int getCapturedBy(Stone color) {
        return color.equals(Stone.White) ? capturedByWhite : capturedByBlack;
    }

    public Board getBoard() {
        return boards.get(boards.size() - 1);
    }

    public boolean inProgress() { return inProgress; }

    public boolean finished() { return !inProgress(); }

    /**
     * <b>może być wywołane tylko jeśli gra jest w trakcie!</b>
     * @return kolor gracza, który teraz ma ruch
     */
    public Stone nextTurn() {
        assert inProgress(); // TODO
        if (moves.isEmpty() || getLastMove().player.equals(Stone.White))
            return Stone.Black;
        else
            return Stone.White;
    }

    /**
     * <b>może być wywołane tylko gry m.player == nextTurn() oraz gdy ruch jest poprawny!</b>
     * TODO better error handling
     * rejestruje ruch gracza m.player i gra toczy się dalej
     * @param m opis ruchu
     */
    public void registerMove(Move m) {
        assert m.player == nextTurn(); // TODO
        if (m instanceof Pass) {
            if (!moves.isEmpty() && getLastMove() instanceof Pass)
                inProgress = false;
            moves.add(m);
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

            moves.add(placement);
            boards.add(nextBoard);
        }
    }

    /**
     * Wynik gry, remisy są niemożliwe
     * TODO czy ta klasa na pewno powinna być tutaj?
     */
    public class Result {
        Stone winner;
        double whitePoints;
        double blackPoints;
    }

    /**
     * <b>wolno wołać tylko gdy finished()!</b>
     * TODO better error handling
     * @return wynik gry
     */
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
        ret.whitePoints += komi;
        assert ret.whitePoints != ret.blackPoints; // ties are not possible
        ret.winner = ret.whitePoints > ret.blackPoints ? Stone.White : Stone.Black;
        return ret;
    }
}
