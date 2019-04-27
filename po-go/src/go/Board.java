package go;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class Board {

    public enum BoardSize {
        Size19, Size13, Size9;

        int getSize() {
            if (this == Size19) return 19;
            else if (this == Size13) return 13;
            else if (this == Size9) return 9;
            return 9;
        }
    }

    final private BoardSize size;
    final private Optional<Stone>[][] board;

    public Board(@NotNull BoardSize size) {
        this.size = size;
        board = new Optional[size.getSize()][size.getSize()];
        for (int i = 0; i < size.getSize(); i++) {
            for (int j = 0; j < size.getSize(); j++)
                board[i][j] = Optional.empty();
        }
    }

    public int getSize() {
        return size.getSize();
    }

    public Optional<Stone>[][] getBoard() {
        return board;
    }

    public Optional<Stone> get(int i, int j) throws ArrayIndexOutOfBoundsException {
        if (i < 0 || i >= getSize() || j < 0 || j >= getSize()) throw new ArrayIndexOutOfBoundsException();
        return board[i][j];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Board)) return false;
        Board other = (Board) o;
        if (other.getSize() != getSize()) return false;
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                if (!board[i][j].equals(other.board[i][j]))
                    return false;
            }
        }
        return true;
    }
}

