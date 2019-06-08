package client;

import go.Board;
import go.Stone;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Optional;

class GoBoardGUI {
    private Circle[][] stones;
    private Rectangle[][] rects;
    private int size;

    /**
     * Tworzy planszę do go w podanym obiekcie Pane i rozmiarze planszy reprezentowanej przez board
     */
    GoBoardGUI(Pane pane, Board board) {
        size = board.getSize();

        Group boardGroup = new Group();

        stones = new Circle[size][size];
        rects = new Rectangle[size][size];

        // Połowa odległości między przecięciami,
        // Związana z szerokością pane
        DoubleProperty r = new SimpleDoubleProperty();
        r.bind(pane.widthProperty().multiply(0.5 / size));

        for(int i = 0; i < size; ++ i) {
            // Linia pozioma
            Line line1 = new Line();
            line1.startXProperty().bind(r);
            line1.startYProperty().bind(r.multiply(2*i + 1));
            line1.endXProperty().bind(r.multiply(2*size - 1));
            line1.endYProperty().bind(r.multiply(2*i + 1));
            //line1.setStrokeWidth(2);

            // Linia pionowa
            Line line2 = new Line();
            line2.startXProperty().bind(r.multiply(2*i + 1));
            line2.startYProperty().bind(r);
            line2.endXProperty().bind(r.multiply(2*i + 1));
            line2.endYProperty().bind(r.multiply(2*size - 1));
            //line2.setStrokeWidth(2);

            // Tekst u góry
            Text text1 = new Text(board.columnNumeral(i));
            text1.xProperty().bind(r.multiply(2*i+1).subtract(text1.getBoundsInLocal().getWidth()/2.0));
            text1.setY(-3);

            // Tekst po lewej
            Text text2 = new Text(Integer.toString(i+1));
            text2.yProperty().bind(r.multiply(2 * (size-i-1) + 1).add(text2.getBoundsInLocal().getHeight()/2.0-2));
            text2.setX(-text2.getBoundsInLocal().getWidth()-3);

            boardGroup.getChildren().addAll(line1, line2, text1, text2);
        }

        for(int y = 0; y < size; ++ y) {
            for(int x = 0; x < size; ++ x) {
                // Prostokąt wokół przecięcia (x, y)
                Rectangle rect = new Rectangle();
                rect.xProperty().bind(r.multiply(2*x));
                rect.yProperty().bind(r.multiply(2*y));
                rect.widthProperty().bind(r.multiply(2));
                rect.heightProperty().bind(r.multiply(2));
                rect.getStyleClass().add("area");

                // Kamień na przecięciu (x, y)
                Circle stone = new Circle();
                stone.centerXProperty().bind(r.multiply(2*x+1));
                stone.centerYProperty().bind(r.multiply(2*y+1));
                stone.radiusProperty().bind(r.multiply(0.8));
                stone.setMouseTransparent(true);

                rects[x][y] = rect;
                stones[x][y] = stone;
                colorStone(x, y, "stone-invisible");
                boardGroup.getChildren().addAll(rect, stone);
            }
        }

        pane.getChildren().add(boardGroup);
    }

    void render(Board toRender) {
        for(int i = 0; i < toRender.getSize(); ++ i) {
            for(int j = 0; j < toRender.getSize(); ++ j) {
                Optional<Stone> stone = toRender.get(i, j);
                if(stone.isEmpty()) {
                    colorStone(i, j, "stone-invisible");
                } else if(stone.get().equals(Stone.White)) {
                    colorStone(i, j, "stone-white");
                } else {
                    colorStone(i, j, "stone-black");
                }
            }
        }
    }

    /**
     * Zwraca rozmiar planszy
     */
    int getSize() { return size; }

    /**
     * Zmienia klasę kamienia na pozycji (x, y) na podaną
     */
    void colorStone(int x, int y, String color) {
        if(x < 0 || x >= size || y < 0 || y >= size) throw new ArrayIndexOutOfBoundsException();
        stones[x][y].getStyleClass().clear();
        stones[x][y].getStyleClass().add(color);
    }

    /**
     * Zwraca prostokąt na pozycji (x, y)
     */
    Rectangle getArea(int x, int y) {
        if(x < 0 || x >= size || y < 0 || y >= size) throw new ArrayIndexOutOfBoundsException();
        return rects[x][y];
    }

    /**
     * Zwracają nazwy klas css odpowiednich stanów kamieni
     */
    String colorToCapturedClass(Stone color) {
        return color == Stone.White ? "stone-potentially-captured-white" : "stone-potentially-captured-black";
    }

    String colorToTerritoryClass(Stone color) {
        return color == Stone.White ? "white-territory" : "black-territory";
    }

    String colorToLastMoveClass(Stone color) {
        return color == Stone.White ? "stone-last-move-white" : "stone-last-move-black";
    }
}
