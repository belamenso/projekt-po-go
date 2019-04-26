package go;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Optional;


public class Board {

    enum BoardSize {
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


}

