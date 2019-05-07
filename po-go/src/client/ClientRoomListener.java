package client;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import javafx.application.Platform;
import server.Message;
import util.Pair;

import java.util.Date;
import java.util.Optional;

import static go.GameLogic.gameLogic;

/**
 * Listener obsugujacy pokoj po stronie gracza
 */
public class ClientRoomListener implements ClientListener {
    private Client client;
    private RoomGUI rg;

    private Stone myColor;

    GameplayManager manager;
    private boolean gameStarted = false;
    private boolean gameInterrupted = false;
    Date start;

    ClientRoomListener(RoomGUI rg, Client client, Stone color, Board.BoardSize size) {
        System.out.println("### ClientRoomListenerCreated");
        this.rg = rg;
        this.client = client;
        client.setListener(this);
        this.myColor = color;
        manager = new GameplayManager(size, 6.5);
    }

    Stone getColor() { return myColor; }

    Board getBoard() { return manager.getBoard(); }

    boolean myTurn() {
        return gameStarted && !gameInterrupted && manager.inProgress() && manager.nextTurn().equals(myColor);
    }

    boolean wasInterruped() { return gameInterrupted; }

    private synchronized void makeMyMove(GameplayManager.Move move) {
        manager.registerMove(move);
        client.sendMessage(move.toString());
    }

    @Override
    public void receivedInput(Message message) {
        String msg = message.msg;

        if (msg.startsWith("list")) {
            System.out.println("Received an unsolicited room listing");
        } else if (msg.startsWith("GAME_BEGINS")) {
            gameStarted = true;
            start = new Date();

            Platform.runLater(() -> rg.addMessage("The game begins, you are " + myColor.pictogram, start));

        } else if (msg.startsWith("GAME_FINISHED")) {
            String[] parts = msg.split(" ");
            Platform.runLater(() -> rg.addMessage((parts[1].equals(myColor.toString()) ? myColor.pictogram : myColor.opposite.pictogram) + " won", start));

        } else if (msg.startsWith("MOVE_ACCEPTED")) {
            System.out.println("# move was accepted");

        } else if (msg.startsWith("MOVE_REJECTED")) {
            System.out.println("# move was rejected"); // TODO reason
            Platform.runLater(() -> rg.addMessage("ERROR: move rejected by the server", start));

        } else if (msg.startsWith("OPPONENT_DISCONNECTED")) {
            gameInterrupted = true;
            Platform.runLater(() -> rg.addMessage(myColor.pictogram + " has disconnected", start));

        } else if (msg.startsWith("MOVE ") && msg.split(" ").length == 3) { // MOVE Player PASS
            assert msg.split(" ")[1] == myColor.opposite.toString();
            Optional<ReasonMoveImpossible> reason = manager.registerMove(new GameplayManager.Pass(myColor.opposite));
            Platform.runLater(() -> rg.addMessage(myColor.opposite.pictogram + " has passed their turn", start));
            assert reason.isEmpty();

        } else if (msg.startsWith("MOVE ") && msg.split(" ").length == 4) { // MOVE Player 1 2
            String[] parts = msg.split(" ");
            int x = Integer.parseInt(parts[2]), y = Integer.parseInt(parts[3]);
            Platform.runLater(() -> rg.addMessage(myColor.opposite.pictogram + " places stone at " + getBoard().positionToNumeral(new Pair<>(y, x)), start));
            Optional<ReasonMoveImpossible> reason = manager.registerMove(new GameplayManager.StonePlacement(myColor.opposite, x, y));
            assert reason.isEmpty();

        } else if (msg.startsWith("MOVE ")) {
            assert false;

        } else if (msg.equals("LOBBY_JOINED")) {

            rg.returnToLobby();

        } else if (msg.equals("OPPONENT_JOINED")) {
            Platform.runLater(() -> rg.addMessage(myColor.opposite.pictogram + " has joined the game", null));

        } else if(msg.startsWith("chat")) {
            String chat = msg.split(";")[1];
            Platform.runLater(() -> rg.addMessage("("+myColor.opposite.pictogram + "): " + chat, start));

        } else {
            System.out.println("UNRECOGNIZED MSG: " + msg);
            Platform.runLater(() -> rg.addMessage("UNRECOGNIZED MESSAGE: " + msg, null));
        }

        Platform.runLater(() -> rg.renderBoard());
    }

    /**
     * called from the GUI thread
     */
    void attemptedToPass() {
        if (myTurn()) {
            makeMyMove(new GameplayManager.Pass(myColor));
            rg.addMessage("You (" + myColor.pictogram + ") passed", start);
        } else {
            handleAttemptToSkipTurn();
        }
    }

    /**
     * called from the GUI thread
     */
    void attemptedToMakeMove(int x, int y) {
        if (myTurn()) {
            Optional<ReasonMoveImpossible> reason = gameLogic.movePossible(getBoard(), x, y, myColor);
            if (reason.isPresent()) {
                System.out.println("Move impossible: " + reason.get());
            } else {
                makeMyMove(new GameplayManager.StonePlacement(myColor, x, y));
                rg.addMessage("You (" + myColor.pictogram + ") moved to " + getBoard().positionToNumeral(new Pair<>(y, x)), start); // nie ruszać kolejności
                rg.renderBoard();
            }
        } else {
            handleAttemptToSkipTurn();
        }
    }

    /**
     * Sends chat message
     */
    void sendChat(String msg) {
        if(msg == null || msg.length()==0) return;
        Platform.runLater(() -> rg.addMessage("("+myColor.pictogram+"): " + msg, start));
        client.sendMessage("chat;"+msg);
    }

    private void handleAttemptToSkipTurn() { // TODO
        System.out.println("Not your turn!");
    }

    void sendQuitRequest() {
        client.sendMessage("quit");
    }

    @Override
    public void disconnected() {
        SceneManager.loadConnectionScreen();
    }

    @Override
    public void couldNotConnect() {}

    @Override
    public void connectedToServer() {}
}
