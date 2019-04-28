package client;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import javafx.application.Platform;
import util.Pair;

import java.util.Optional;
import java.io.IOException;

/**
 * Listener obsugujacy pokoj po stronie gracza
 */
public class ClientRoomListener implements ClientListener {
    private Client client;
    private Stone myColor;
    GameplayManager manager = new GameplayManager();
    private RoomGUI rg;
    private boolean gameStarted = false;
    String myRepresentation, opponentRepresentation;

    ClientRoomListener(Client client, Stone color, RoomGUI rg) {
        System.out.println("### ClientRoomListenerCreated");
        this.rg = rg;
        this.client = client;
        this.myColor = color;
        myRepresentation = myColor == Stone.White ? "○" : "●";
        opponentRepresentation = myColor == Stone.Black ? "○" : "●";
    }

    int getSize() {
        return manager.getBoard().getSize();
    }

    Stone getColor() { return myColor; }

    Board getBoard() { return manager.getBoard(); }

    boolean myTurn() {
        return gameStarted && !manager.interrupted() && manager.inProgress() && manager.nextTurn().equals(myColor);
    }

    public synchronized void makeMyMove(GameplayManager.Move move) {
        manager.registerMove(move);
        client.sendMessage(move.toString());
    }

    @Override
    public void receivedInput(String msg) {

        if (msg.startsWith("list")) {
            System.out.println("Received an unsolicited room listing");
        } else if (msg.startsWith("exitedRoom")) {
            //client.setListener(new ClientLobbyListener(client));
        } else if (msg.startsWith("GAME_BEGINS")) {
            gameStarted = true;
            Platform.runLater(() -> rg.addMessage("The game begins"));
        } else if (msg.startsWith("GAME_FINISHED")) {
            String[] parts = msg.split(" ");
            System.out.println("Game finished, black points: " + parts[2] + " white: " + parts[3]);
            System.out.println(myColor.toString().equals(parts[1]) ? "WYGRANA!" : "PRZEGRANA");
            Platform.runLater(() -> rg.addMessage(parts[1] + " won"));
        } else if (msg.startsWith("MOVE_ACCEPTED")) {
            System.out.println("# move was accepted");
        } else if (msg.startsWith("MOVE_REJECTED")) {
            System.out.println("# move was rejected"); // TODO reason
            Platform.runLater(() -> rg.addMessage("ERROR: move rejected by the server"));
        } else if (msg.startsWith("OPPONENT_DISCONNECTED")) {
            manager.interruptGame();
            Platform.runLater(() -> rg.addMessage(opponentRepresentation + " has disconnected"));
        } else if (msg.startsWith("MOVE PASS")) {
            Optional<ReasonMoveImpossible> reason = manager.registerMove(new GameplayManager.Pass(myColor.opposite));
            Platform.runLater(() -> rg.addMessage(opponentRepresentation + " has passed their turn"));
            assert reason.isEmpty();
        } else if (msg.startsWith("MOVE ")) {
            String[] parts = msg.split(" ");
            assert parts.length == 3;
            int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]);
            Platform.runLater(() -> rg.addMessage(opponentRepresentation + " places stone at " + getBoard().positionToNumeral(new Pair<>(y, x))));
            Optional<ReasonMoveImpossible> reason = manager.registerMove(new GameplayManager.StonePlacement(myColor.opposite, x, y));
            assert reason.isEmpty();
        } else if (msg.equals("lobbyJoined"))  {
            try {
                rg.returnToLobby();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("UNRECOGNIZED MSG: " + msg);
            Platform.runLater(() -> rg.addMessage("UNRECOGNIZED MESSAGE: " + msg));
        }
        Platform.runLater(() -> rg.renderBoard());
    }

    @Override
    public void serverClosed() {
        System.out.println("server was closed (ClientRoomListener " + this + ")");
    }

    @Override
    public void unknownHost() {}

    @Override
    public void couldNotConnect() {}

    @Override
    public void disconnected() {}

    @Override
    public void connectedToServer() {}
}
