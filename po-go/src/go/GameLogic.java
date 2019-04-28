package go;

import util.Pair;

import java.util.Queue;
import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Singleron realizujący podstawowe operacje logiki gdy na obiektach typu Board
 * Napisany funkcyjnie, niczego (trwale) nie modyfikuje
 */
public enum GameLogic {
    gameLogic;

    /**
     * Przesunięcia indeksów do sąsiadów na lewo, prawo, w górę i dół
     */
    private final ArrayList<Pair<Integer, Integer>> offsets;
    {
        offsets = new ArrayList<>(4);
        offsets.add(new Pair<>(0, -1));
        offsets.add(new Pair<>(0, 1));
        offsets.add(new Pair<>(-1, 0));
        offsets.add(new Pair<>(1,0));
    }

    /**
     * Tworzy świerzą macierz boolowską wielkości planszy podanej jako argument
     */
    private boolean[][] boolMatrix(Board board) {
        return new boolean[board.getSize()][board.getSize()];
    }

    /**
     * Reprezentuje maksymalną spójną grupę kamieni jednego koloru na planszy
     * liberties to liczba pustych przecięć, które bezpośrednio sąsiadują z jakimś zewnętrznym kamieniem grupy
     */
    private class Group {
        ArrayList<Pair<Integer, Integer>> group = new ArrayList<>();
        int liberties = 0;
    }

    /**
     * Algorytm BFS rozpoznawania grupy z kamieniem w miejscu (i, j)
     * @param seen macierz boolowska w której zaznacza się, czy dany kamien (nie puste przecięcie!) był już widziany,
                   jeśli prowadzi się przegląd całej planszy, należy zachowywać macierz pomiędzy wywołaniami, jeśli nie,
                   należy podawać ją świerzą za każdym razem
     * @return Rozpoznana grupa kamieni
     */
    private Group collectGroup(Board board, int i, int j, boolean[][] seen) {
        Group ret = new Group();
        boolean[][] libertiesSeen = boolMatrix(board);
        assert board.indicesOk(i, j) && board.get(i, j).isPresent(); // TODO
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

                if (!board.indicesOk(x, y)) continue;

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

    /**
     * Reprezentuje spójne terytorium <b>pustych przecięć</b>
     * captor - gracz do kótrego należy terytorium lub Optional.empty() jeśli nie należy do żadnego
     */
    public class Territory {
        public ArrayList<Pair<Integer, Integer>> territory = new ArrayList<>();
        public Optional<Stone> captor = Optional.empty();
    }

    /**
     * Algorytm BFS znajdowania maksymalnego spójnego terytorium do którego należy puste przecięcie (i, j)
     * TODO chwilowo to nie obejmuje usuwania dead stones pod koniec gry, czyli nie jest zgodne z zasadami
     * @param seen macierz boolowska w której zaznacza się, czy dane <b>puste przecięcie</b> było już odwiedzone
                   jeśli prowadzi się przegląd całej planszy, należy zachowywać macierz pomiędzy wywołaniami, jeśli nie,
                   należy podawać ją świerzą za każdym razem
     */
    private Territory collectTerritory(Board board, int i, int j, boolean[][] seen) {
        assert board.indicesOk(i, j) && board.get(i, j).isEmpty(); // TODO
        assert !seen[i][j];

        Territory ret = new Territory();
        seen[i][j] = true;
        ret.territory.add(new Pair<>(i, j));

        // seenAnyColor && captor.isEmpty() => nie ma jednoznacznego koloru, który przejął to terytorium
        boolean seenAnyColor = false;

        Queue<Pair<Integer, Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(i, j));
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> curr = queue.remove();
            for (Pair<Integer, Integer> d : offsets) {
                int x = curr.x + d.x, y = curr.y + d.y;
                if (!board.indicesOk(x, y) || seen[x][y]) continue;
                if (board.get(x, y).isEmpty()) {
                    queue.add(new Pair<>(x, y));
                    seen[x][y] = true;
                    ret.territory.add(new Pair<>(x, y));
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


    /**
     * determinuje, czy ruch gracza color na miejsce (i, j) jest dozwolony, to znaczy czy
     * <b>uwaga! ta metoda nie sprawdza czy gracz ma teraz kolejkę ani czy stan gry się nie powtórzy, ponieważ za to odpowiedzialny jest GameplayManager</b>
     * <ol>
     * <li>indeks jest poprawny</li>
     * <li>nie ma tam innego kamienia</li>
     * <li>ruch nie jest samobójczy albo jest samobójczy, ale natychmiast przerywa zagrożenie</li>
     * </ol>
     * @return empty jeśli ruch jest możliwy, powód jeśli nie jest
     */
    public Optional<ReasonMoveImpossible> movePossible(Board board, int i, int j, Stone color) {
        if (!board.indicesOk(i, j)) return Optional.of(ReasonMoveImpossible.PositionOutOfBounds);
        if (board.get(i, j).isPresent()) return Optional.of(ReasonMoveImpossible.PositionOccupied);

        board.getBoard()[i][j] = Optional.of(color);
        Group group = collectGroup(board, i, j, boolMatrix(board));
        board.getBoard()[i][j] = Optional.empty();

        if (group.liberties > 0) return Optional.empty();
        else { // sprawdź, czy ten ruch pojmie jakieś kamyki, jeśli tak, pozwól na niego
            assert group.liberties == 0;

            Optional<ReasonMoveImpossible> ret = Optional.of(ReasonMoveImpossible.SuicidalMove);
            board.getBoard()[i][j] = Optional.of(color);
            for (Pair<Integer, Integer> d : offsets) {
                int x = i + d.x, y = j + d.y;
                if (!board.indicesOk(x, y)) continue;
                assert board.get(x, y).isPresent();
                if (board.get(x, y).get() == color.opposite) {
                    if (collectGroup(board, x, y, boolMatrix(board)).liberties == 0) {
                        ret = Optional.empty();
                        break;
                    }
                }
            }
            board.getBoard()[i][j] = Optional.empty();
            return ret;
        }
    }

    /**
     * @return Zwrava listę pól, na których znajdują się kamienie przeciwnika, które zostały właśnie przejęte
     * @param colorJustPlaced gracz, który właśnie dokonał ruchu
     */
    public ArrayList<Pair<Integer, Integer>> captured(Board board, Stone colorJustPlaced) {
        ArrayList<Pair<Integer, Integer>> ret = new ArrayList<>();
        boolean[][] seen = boolMatrix(board);
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.get(i, j).isPresent() && !seen[i][j] && board.get(i, j).get() == colorJustPlaced.opposite) {
                    Group group = collectGroup(board, i, j, seen);
                    if (group.liberties == 0)
                        ret.addAll(group.group);
                }
            }
        }
        return ret;
    }

    /**
     * @return Zwraca listę przejętych (<b>czyli z dobrze zdefiniowanym captorem</b>) terytoriów jakie powstały na planszy
     * TODO to nie usuwa martwych kamieni z planszy, czyli technicznie jest niezgodne z zasadami gry
     */
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
