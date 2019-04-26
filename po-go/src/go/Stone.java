package go;

public enum Stone {
    White, Black;

    public Stone opposite;

    static {
        White.opposite = Black;
        Black.opposite = White;
    }
}
