package go;

import java.io.Serializable;

/**
 * Klasa reprezentująca kolor kamienia. Nie ma kamieni pustych, gra reprezentuje je jako Optional.empty()
 */
public enum Stone implements Serializable {
    White, Black;

    /**
     * Wskazuje na kolor kamienia przeciwnika
     */
    public Stone opposite;
    static {
        White.opposite = Black;
        Black.opposite = White;
    }

    @Override
    public String toString() {
        return this == White ? "WHITE" : "BLACK"; // TODO bardzo niebezpieczne, na wyniku tej metody opiera się protokół klient-serwer
    }

    public String pictogram;
    static {
        White.pictogram = "○";
        Black.pictogram = "●";
    }
}
