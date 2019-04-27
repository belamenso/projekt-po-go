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
    public static abstract class Move {
        Stone player;

        Move(Stone color) {
            player = color;
        }
    }

    /**
     * gracz player przepuścił kolejkę
     */
    public static class Pass extends Move {
        public Pass(Stone color) { super(color); }
    }

    /**
     * Gracz player umieścił swój kamień na pozycji position
     */
    public static class StonePlacement extends Move {
        Pair<Integer, Integer> position;

        public StonePlacement(Stone color, int x, int y) {
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
    private boolean inProgress = true;
    /**
     * Czy któryś z zawodników odłączył się od serwera zanim gra się zakończyła?
     */
    private boolean gameInterrupted = false; // TODO better handling of this case
    /**
     * liczba czarnych kameni przejętych przez białe w ciągu gry
     */
    private ArrayList<Integer> capturedByWhite = new ArrayList<>();
    /**
     * liczba białych kamieni przejętych przez czarne w ciągu gry
     */
    private ArrayList<Integer> capturedByBlack = new ArrayList<>();
    /**
     * wartość liczbowa dodawana do punktów białych pod koniec gry, ponieważ one nie zaczynały
     * zawsze zachodzi floor(komi) != komi aby nie dopuścić remisów
     */
    private final double komi;

    private int getCapturedByWhite() { return capturedByWhite.get(capturedByWhite.size() - 1); }

    private int getCapturedByBlack() { return capturedByBlack.get(capturedByBlack.size() - 1); }

    /**
     * Przedłuż historię liczby przejętych kaniemi oraz zaktualizuj obecne dane
     */
    private void updateCapturedCount(Stone playerCapturing, int n) {
        if (playerCapturing.equals(Stone.White)) {
            capturedByWhite.add(getCapturedByWhite() + n);
            capturedByBlack.add(getCapturedByBlack());
        } else {
            capturedByWhite.add(getCapturedByWhite());
            capturedByBlack.add(getCapturedByBlack() + n);
        }
    }

    /**
     * Przedłuż historię liczby przejętych kamieni i niczego nie modyfikuj
     */
    private void extendCapturedHistory() {
        capturedByWhite.add(getCapturedByWhite());
        capturedByBlack.add(getCapturedByBlack());
    }

    private Move getLastMove() {
        assert !moves.isEmpty();
        return moves.get(moves.size() - 1);
    }

    /***********
     ****** PUBLIC INTERFACE
     ***********/

    public GameplayManager(Board.BoardSize size, double komi) {
        assert Math.floor(komi) != komi; // needed to break ties
        this.komi = komi;
        boards.add(new Board(size));
        capturedByBlack.add(0);
        capturedByWhite.add(0);
    }

    public GameplayManager() {
        this(Board.BoardSize.Size9, 6.5);
    }

    public double getKomi() { return komi; }

    public boolean interrupted() { return gameInterrupted; }

    public void interruptGame() { gameInterrupted = true; }

    public int getCapturedBy(Stone color) {
        return color.equals(Stone.White) ? getCapturedByWhite() : getCapturedByBlack();
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
        assert inProgress() && !gameInterrupted; // TODO
        if (moves.isEmpty() || getLastMove().player.equals(Stone.White))
            return Stone.Black;
        else
            return Stone.White;
    }

    /**
     * gdy nie (m.player == nextTurn() oraz gdy ruch jest poprawny), wtedy zwraca powód, jeśli ruch został przyjęty, zwraca empty()
     * rejestruje ruch gracza m.player i gra toczy się dalej
     * @param m opis ruchu
     */
    public Optional<ReasonMoveImpossible> registerMove(Move m) {
        if (gameInterrupted)
            return Optional.of(ReasonMoveImpossible.GameInterrupted);
        if (!m.player.equals(nextTurn()))
            return Optional.of(ReasonMoveImpossible.NotYourTurn);
        if (m instanceof Pass) {
            if (!moves.isEmpty() && getLastMove() instanceof Pass)
                inProgress = false;
            moves.add(m);
            extendCapturedHistory();
            return Optional.empty();
        } else {
            StonePlacement placement = (StonePlacement) m;
            Optional<ReasonMoveImpossible> reason = gameLogic.movePossible(getBoard(), placement.position.x, placement.position.y, placement.player);
            if (reason.isPresent())
                return reason;

            // compute the next board
            Board nextBoard = getBoard().cloneBoard();
            nextBoard.getBoard()[placement.position.x][placement.position.y] = Optional.of(m.player);
            ArrayList<Pair<Integer, Integer>> captured = gameLogic.captured(nextBoard, m.player);
            for (Pair<Integer, Integer> pos : captured)
                nextBoard.getBoard()[pos.x][pos.y] = Optional.empty();

            if (boards.size() >= 2 && boards.get(boards.size() - 2).equals(nextBoard))
                return Optional.of(ReasonMoveImpossible.ReturnToImmediatePreviousState);

            // changes to the state
            moves.add(placement);
            boards.add(nextBoard);
            updateCapturedCount(m.player, captured.size());
            return Optional.empty();
        }
    }

    /**
     * Wynik gry, remisy są niemożliwe
     * TODO czy ta klasa na pewno powinna być tutaj?
     */
    public class Result {
        public Stone winner;
        public double whitePoints;
        public double blackPoints;
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
        ret.whitePoints -= getCapturedByBlack();
        ret.blackPoints -= getCapturedByWhite();
        ret.whitePoints += komi;
        assert ret.whitePoints != ret.blackPoints; // ties are not possible
        ret.winner = ret.whitePoints > ret.blackPoints ? Stone.White : Stone.Black;
        return ret;
    }
}
