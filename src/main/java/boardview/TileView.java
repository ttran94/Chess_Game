package boardview;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Position;

/**
 * View class for a tile on a chess board
 * A tile should be able to display a chess piece
 * as well as highlight itself during the game.
 *
 * @author <Yourname here>
 */
public class TileView implements Tile {

    private Position position;
    private StackPane root;
    private String symbol;
    private Color color;

    /**
     * Creates a TileView with a specified position
     * @param p
     */
    public TileView(Position p) {
        position = p;
        if ((p.getRow() + p.getCol()) % 2 == 0) {
            color = Color.web("#759F4F");
        } else {
            color = Color.web("#EDF0CF");
        }
        root = new StackPane();
        root.setCache(false);
        root.setMinWidth(50);
        root.setMinHeight(50);
        root.setStyle("-fx-background-color: #"
                        + color.toString().substring(2));
    }


    @Override
    public Position getPosition() {
        return position;
    }


    @Override
    public Node getRootNode() {
        return root;
    }

    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
        if (symbol.equals("")) {
            root.getChildren().clear();
        } else {
            Canvas canvasSymbol = new Canvas(50, 50);
            GraphicsContext gc = canvasSymbol.getGraphicsContext2D();
            gc.setFont(new Font(50));
            gc.fillText(getSymbol(), 0, 45);
            root.getChildren().add(canvasSymbol);
        }
    }


    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void highlight(Color colorHighLight) {
        root.setStyle(
            "-fx-background-color: #"
            + colorHighLight.toString().substring(2));
    }

    @Override
    public void clear() {
        root.setStyle("-fx-background-color: #"
                        + color.toString().substring(2));
    }
}
