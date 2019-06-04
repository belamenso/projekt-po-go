package go;

import util.Pair;

import java.io.Serializable;
import java.util.*;

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
    public static abstract class Move implements Serializable {
        public Stone player;

        Move(Stone color) {
            player = color;
        }
    }

    /**
     * gracz player przepuścił kolejkę
     */
    public static class Pass extends Move {
        public Pass(Stone color) { super(color); }

        /**
         * @return Renderuje komendę
         */
        @Override
        public String toString() { return "MOVE " + player + " PASS"; }
    }

    /**
     * Gracz player umieścił swój kamień na pozycji position
     */
    public static class StonePlacement extends Move {
        public Pair<Integer, Integer> position;

        public StonePlacement(Stone color, int x, int y) {
            super(color);
            position = new Pair<>(x, y);
            // TODO error handling
        }

        /**
         * @return Renderuje komendę
         */
        @Override
        public String toString() {
            return "MOVE " + player + " " + position.x + " " + position.y;
        }
    }

    /**
     * Ususniecie martwych kamieni
     * Jako ruch poniewaz inaczej trzeba by było wysyłać do spectatora czy gra się skończyła i ustawić finished, nie byłoby tego w historii
     */
    public static class DeadStonesRemoval extends Move {
        public Set<Pair<Integer, Integer>> toRemove;

        public DeadStonesRemoval(Set<Pair<Integer, Integer>> toRemove) {
            super(null);
            this.toRemove = toRemove;
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

    private final Board.BoardSize size;

    private ArrayList<Pair<GameplayManager, Integer>> myForks = new ArrayList<>();

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

    private GameplayManager(GameplayManager other, int afterNMoves) {
        assert afterNMoves >= 0 && afterNMoves <= other.moves.size();
        this.komi = other.komi;
        this.size = other.size;

        boards.add(new Board(size));
        for (int i = 0; i < afterNMoves; i++) {
            boards.add(other.boards.get(i + 1));
            moves.add(other.moves.get(i));
            capturedByWhite.add(other.capturedByWhite.get(i));
            capturedByBlack.add(other.capturedByBlack.get(i));
        }

        if (other.inProgress) {
            inProgress = true;
        } else {
            inProgress = afterNMoves != other.moves.size();
        }
    }

    /***********
     ****** PUBLIC INTERFACE
     ***********/

    public GameplayManager(Board.BoardSize size, double komi) {
        assert Math.floor(komi) != komi; // needed to break ties
        this.komi = komi;
        this.size = size;
        boards.add(new Board(size));
        capturedByBlack.add(0);
        capturedByWhite.add(0);
    }

    public GameplayManager() {
        this(Board.BoardSize.Size9, 6.5);
    }

    public GameplayManager fork(int afterNMoves) {
        GameplayManager ret = new GameplayManager(this, afterNMoves);
        myForks.add(new Pair<>(ret, afterNMoves));
        myForks.sort(Comparator.comparingInt(o -> o.y));
        return ret;
    }

    public double getKomi() { return komi; }

    public Board.BoardSize getSize() { return size; }

    /**
     * @return może się zdarzyć, że klient i serwer mają różne implementacje zasad gry, niekompatybilne pokoje nie powinny być widoczne
     */
    public String getEngineVersion() { return "0.0.1"; }

    public int getCapturedBy(Stone color) {
        return color.equals(Stone.White) ? getCapturedByWhite() : getCapturedByBlack();
    }

    public int getCapturedBy(Stone color, int time) {
        return color.equals(Stone.White) ? capturedByWhite.get(time-1) : capturedByBlack.get(time-1);
    }

    public Board getBoard() {
        return boards.get(boards.size() - 1);
    }

    public Board getBoardByNumber(int n) { return boards.get(n - 1); }

    public List<Move> getMoveHistory() { return moves; }

    public boolean inProgress() { return inProgress; }

    public boolean finished() { return !inProgress(); }

    public int getHistorySize() { return boards.size(); }

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
     * gdy nie (m.player == nextTurn() oraz gdy ruch jest poprawny), wtedy zwraca powód, jeśli ruch został przyjęty, zwraca empty()
     * rejestruje ruch gracza m.player i gra toczy się dalej
     * @param m opis ruchu
     */
    public Optional<ReasonMoveImpossible> registerMove(Move m) {
        if(m instanceof DeadStonesRemoval)
        {
            assert hadTwoPasses();
            Set<Pair<Integer, Integer>> toRemove = ((DeadStonesRemoval) m).toRemove;
            inProgress = false;

            Board board = getBoard().cloneBoard();

            if(toRemove != null) {
                for (Pair<Integer, Integer> p : toRemove) {
                    board.getBoard()[p.x][p.y] = Optional.empty();
                }
            }

            moves.add(m);
            extendCapturedHistory();
            boards.add(board);
            return Optional.empty();
        }
        if (!m.player.equals(nextTurn()))
            return Optional.of(ReasonMoveImpossible.NotYourTurn);
        if (m instanceof Pass) {
            moves.add(m);
            extendCapturedHistory();
            boards.add(getBoard().cloneBoard());
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

    public boolean hadTwoPasses() {
        int count = 0;
        for(int i = moves.size() - 1; i > 0; -- i)
            if(moves.get(i) instanceof Pass)
                ++ count;
            else break;
        return count > 0 && count % 2 == 0;
    }

    /**
     * Wynik gry, remisy są niemożliwe
     * TODO czy ta klasa na pewno powinna być tutaj?
     */
    public class Result implements Serializable {
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
