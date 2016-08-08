package boardview;
import gamecontrol.ChessController;
import gamecontrol.GameController;
import gamecontrol.GameState;
import gamecontrol.NetworkedChessController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.IllegalMoveException;
import model.Move;
import model.Piece;
import model.PieceType;
import model.Position;
import model.Side;
import model.chess.ChessPiece.ChessPieceType;

/**
 * A class for a view for a chess board. This class must have a reference to a
 * GameController for chess playing chess
 *
 * @author Gustavo
 * @date Oct 20, 2015
 */
public class BoardView {

    /* You may add more instance data if you need it */
    protected GameController controller;
    private GridPane gridPane;
    private Tile[][] tiles;
    private Text sideStatus;
    private Text state;
    private boolean isRotated;
    private Position selectedPawn;
    Label turnText;
    Label stateText;

    /**
     * Construct a BoardView with an instance of a GameController and a couple
     * of Text object for displaying info to the user
     *
     * @param controller
     *            The controller for the chess game
     * @param state
     *            A Text object used to display state to the user
     * @param sideStatus
     *            A Text object used to display whose turn it is
     */
    public BoardView(GameController controller, Text state, Text sideStatus) {

        this.controller = controller;
        this.state = state;
        this.sideStatus = sideStatus;
        tiles = new Tile[8][8];
        gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color : goldenrod;");
        reset(controller);

        selectedPawn = null;
        controller.beginTurn();
    }

    /**
     * Listener for clicks on a tile
     *
     * @param tile
     *            The tile attached to this listener
     * @return The event handler for all tiles.
     */
    private EventHandler<? super MouseEvent> tileListener(Tile tile) {
        return event -> {
            if (controller instanceof NetworkedChessController
                    && controller.getCurrentSide()
                    != ((NetworkedChessController) controller).getLocalSide()) {
                //not your turn!
                return;
            }

            // Don't change the code above this :
            //Check whose piece it is
            Set<Move> moves;
            ChessController controllerOther = (ChessController) controller;
            moves =
                controllerOther.getMovesForPieceAt(tile.getPosition());
            if (selectedPawn == null && !moves.isEmpty()) {
                selectedPawn = tile.getPosition();
                firstClick(tile);
            } else {
                if (selectedPawn != null) {
                    secondClick(tile);
                }
            }
        };
    }

    /**
     * Perform the first click functions, like displaying which are the valid
     * moves for the piece you clicked.
     *
     * @param tile
     *            The TileView that was clicked
     */
    private void firstClick(Tile tile) {
        Set<Move> moves = controller.getMovesForPieceAt(selectedPawn);
        int movecount =
            tile.getPosition().getRow() + tile.getPosition().getCol();
        if (movecount % 2 == 0) {
            tile.highlight(Color.WHEAT);
        } else {
            tile.highlight(Color.TAN);
        }

        Iterator<Move> moveIterator = moves.iterator();
        while (moveIterator.hasNext()) {
            Move currentMove = moveIterator.next();
            Position finalPosition = currentMove.getDestination();
            if ((finalPosition.getRow() + finalPosition.getCol()) % 2 == 0) {
                tiles[finalPosition.getRow()][finalPosition.getCol()]
                        .highlight(Color.web("#0040FF"));
            } else {
                tiles[finalPosition.getRow()][finalPosition.getCol()]
                        .highlight(Color.web("#58ACFA"));
            }
            if (controller.moveResultsInCapture(currentMove)) {
                tiles[finalPosition.getRow()][finalPosition.getCol()]
                        .highlight(Color.web("#FA5858"));
            }
        }
    }

    /**
     * Perform the second click functions, like sending moves to the controller
     * but also checking that the user clicked on a valid position. If they
     * click on the same piece they clicked on for the first click then you
     * should reset to click state back to the first click and clear the
     * highlighting effected on the board.
     *
     * @param tile
     *            the TileView at which the second click occurred
     */
    private void secondClick(Tile tile) {
        Set<Move> moves = controller.getMovesForPieceAt(selectedPawn);
        Iterator<Move> moveIterator = moves.iterator();
        while (moveIterator.hasNext()) {
            Move currentMove = moveIterator.next();
            Position finalPosition = currentMove.getDestination();
            if ((finalPosition.getRow() + finalPosition.getCol()) % 2 == 0) {
                tiles[finalPosition.getRow()][finalPosition.getCol()].clear();
            } else {
                tiles[finalPosition.getRow()][finalPosition.getCol()].clear();
            }
        }

        getTileAt(selectedPawn).clear();

        Position target = tile.getPosition();

        Move currentMove = new Move(selectedPawn, target);
        if (controller.getMovesForPieceAt(selectedPawn).contains(currentMove)) {
            try {
                tiles[target.getRow()][target.getCol()].setSymbol("");
                controller.makeMove(currentMove);
                controller.endTurn();
                controller.beginTurn();
            } catch (IllegalMoveException e) {
                e.printStackTrace();
            }
        }

        selectedPawn = null;

    }

    /**
     * This method should be called any time a move is made on the back end. It
     * should update the tiles' highlighting and symbols to reflect the change
     * in the board state.
     *
     * @param moveMade
     *            the move to show on the view
     * @param capturedPositions
     *            a list of positions where pieces were captured
     *
     */
    public void updateView(Move moveMade, List<Position> capturedPositions) {
        Position startPosition = moveMade.getStart();
        Position finalPosition = moveMade.getDestination();
        getTileAt(startPosition).setSymbol("");
        getTileAt(finalPosition).setSymbol("");
        getTileAt(finalPosition).setSymbol(
                controller.getSymbolForPieceAt(finalPosition));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tiles[i][j].clear();
            }
        }
        tiles[finalPosition.getRow()][finalPosition.getCol()]
                .highlight(Color.SANDYBROWN);
        tiles[startPosition.getRow()][startPosition.getCol()]
                .highlight(Color.BURLYWOOD);

    }

    /**
     * Asks the user which PieceType they want to promote to (suggest using
     * Alert). Then it returns the Piecetype user selected.
     *
     * @return the PieceType that the user wants to promote their piece to
     */
    private PieceType handlePromotion() {
        List<String> promoteChoices = new ArrayList<>();
        for (PieceType type : controller.getPromotionTypes()) {
            promoteChoices.add(((ChessPieceType) type).name());
        }
        ChoiceDialog<String> dialogBox = new ChoiceDialog<>("", promoteChoices);
        dialogBox.setTitle("Promotion");
        dialogBox.setHeaderText("You can promote your Pawn!");
        dialogBox.setContentText("Choose your piece type:");

        // Traditional way to get the response value.
        Optional<String> result = dialogBox.showAndWait();
        String pawnChoice = result.orElse(ChessPieceType.PAWN.name());

        return ChessPieceType.valueOf(pawnChoice);
    }

    /**
     * Handles a change in the GameState (ie someone in check or stalemate). If
     * the game is over, it should open an Alert and ask to keep playing or
     * exit.
     *
     * @param s
     *            The new Game State
     */
    public void handleGameStateChange(GameState s) {
        stateText.setText(s.toString());
        if (!s.toString().equals("White is in Check")
                && !s.toString().equals("Black is in Check")
                && !s.toString().equals("Ongoing")) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(s.toString());
            alert.setHeaderText("Result : " + s.toString());
            alert.setContentText("Continue playing or exit?");
            ButtonType buttonContinue = new ButtonType("Continue");
            ButtonType buttonExit = new ButtonType("Exit");
            alert.getButtonTypes().setAll(buttonContinue, buttonExit);
            Optional<ButtonType> getResult = alert.showAndWait();
            if (getResult.get() == buttonContinue) {
                Platform.setImplicitExit(false);
                GameController controller = new NetworkedChessController();
                ChessFX newGame = new ChessFX();
                Stage primaryStage = new Stage();
                newGame.start(primaryStage);
            } else if (getResult.get() == buttonExit) {
                Platform.exit();
            }
        }
    }

    /**
     * Updates UI that depends upon which Side's turn it is
     *
     * @param s
     *            The new Side whose turn it currently is
     */
    public void handleSideChange(Side s) {
        turnText.setText(s.toString() + "'s Turn");
    }

    /**
     * Resets this BoardView with a new controller. This moves the chess pieces
     * back to their original configuration and calls startGame() at the end of
     * the method
     *
     * @param newController
     *            The new controller for this BoardView
     */
    public void reset(GameController newController) {

        if (controller instanceof NetworkedChessController) {
            ((NetworkedChessController) controller).close();
        }

        controller = newController;
        isRotated = false;

        if (controller instanceof NetworkedChessController) {
            Side mySide
                = ((NetworkedChessController) controller).getLocalSide();
            if (mySide == Side.BLACK) {
                isRotated = true;
            }
        }

        sideStatus.setText(controller.getCurrentSide() + "'s Turn");

        // controller event handlers
        // We must force all of these to run on the UI thread
        controller.addMoveListener(
                (Move move, List<Position> capturePositions) ->
                Platform.runLater(
                    () -> updateView(move, capturePositions)));

        controller.addCurrentSideListener(
                (Side side) -> Platform.runLater(
                    () -> handleSideChange(side)));

        controller.addGameStateChangeListener(
                (GameState state) -> Platform.runLater(
                    () -> handleGameStateChange(state)));

        controller.setPromotionListener(() -> handlePromotion());


        addPieces();
        controller.startGame();

        if (isRotated) {
            setBoardRotation(180);
        } else {
            setBoardRotation(0);
        }
    }

    /**
     * Initializes the gridPane object with the pieces from the GameController.
     * This method should only be called once before starting the game.
     */
    private void addPieces() {
        gridPane.getChildren().clear();
        Map<Piece, Position> pieces = controller.getAllActivePiecesPositions();
        /* Add the tiles */
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Tile tile = new TileView(new Position(row, col));
                gridPane.add(tile.getRootNode(), 1 + tile.getPosition()
                        .getCol(), 1 + tile.getPosition().getRow());
                GridPane.setHgrow(tile.getRootNode(), Priority.ALWAYS);
                GridPane.setVgrow(tile.getRootNode(), Priority.ALWAYS);
                getTiles()[row][col] = tile;
                tile.getRootNode().setOnMouseClicked(tileListener(tile));
                tile.clear();
                tile.setSymbol("");

            }
        }

        /* Add the pieces */
        for (Piece p : pieces.keySet()) {
            Position placeAt = pieces.get(p);
            getTileAt(placeAt).setSymbol(p.getType().getSymbol(p.getSide()));
        }
        /* Add the coordinates around the perimeter */
        for (int i = 1; i <= 8; i++) {
            Text coord1 = new Text((char) (64 + i) + "");
            GridPane.setHalignment(coord1, HPos.CENTER);
            gridPane.add(coord1, i, 0);

            Text coord2 = new Text((char) (64 + i) + "");
            GridPane.setHalignment(coord2, HPos.CENTER);
            gridPane.add(coord2, i, 9);

            Text coord3 = new Text(9 - i + "");
            GridPane.setHalignment(coord3, HPos.CENTER);
            gridPane.add(coord3, 0, i);

            Text coord4 = new Text(9 - i + "");
            GridPane.setHalignment(coord4, HPos.CENTER);
            gridPane.add(coord4, 9, i);
        }
    }

    private void setBoardRotation(int degrees) {
        gridPane.setRotate(degrees);
        for (Node n : gridPane.getChildren()) {
            n.setRotate(degrees);
        }
    }

    /**
     * Gets the view to add to the scene graph
     *
     * @return A pane that is the node for the chess board
     */
    public Pane getView() {
        return gridPane;
    }

    /**
     * Gets the tiles that belong to this board view
     *
     * @return A 2d array of TileView objects
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    private Tile getTileAt(int row, int col) {
        return getTiles()[row][col];
    }

    private Tile getTileAt(Position p) {
        return getTileAt(p.getRow(), p.getCol());
    }
}
