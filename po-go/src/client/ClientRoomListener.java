package client;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import javafx.application.Platform;

import java.util.Optional;
import java.io.IOException;

/**
 * Listener obsugujacy pokoj po stronie gracza
 */
public class ClientRoomListener implements ClientListener {
    private Client client;
    private Stone myColor;
    private GameplayManager manager = new GameplayManager();
    private RoomGUI rg;

    ClientRoomListener(Client client, Stone color, RoomGUI rg) {
        System.out.println("### ClientRoomListenerCreated");
        this.rg = rg;
        this.client = client;
        this.myColor = color;
    }

    int getSize() {
        return manager.getBoard().getSize();
    }

    Stone getColor() { return myColor; }

    Board getBoard() { return manager.getBoard(); }

    boolean myTurn() {
        return !manager.interrupted() && manager.inProgress() && manager.nextTurn().equals(myColor);
    }

    public synchronized void makeMyMove(GameplayManager.Move move) {
        manager.registerMove(move);
        client.sendMessage(move.toString());
    }

    @Override
    public void receivedInput(String msg) {
        Platform.runLater(() -> rg.addMessage(msg));

        if (msg.startsWith("exitedRoom")) {
            //client.setListener(new ClientLobbyListener(client));
        } else //noinspection StatementWithEmptyBody
            if (msg.startsWith("GAME_BEGINS")) {
            rg.renderBoard();
        } else if (msg.startsWith("GAME_FINISHED")) {
            String[] parts = msg.split(" ");
            System.out.println("Game finished, black points: " + parts[2] + " white: " + parts[3]);
            System.out.println(myColor.toString().equals(parts[1]) ? "WYGRANA!" : "PRZEGRANA");
            rg.renderBoard();
        } else if (msg.startsWith("MOVE_ACCEPTED")) {
            System.out.println("# move was accepted");
        } else if (msg.startsWith("MOVE_REJECTED")) {
            System.out.println("# move was rejected"); // TODO reason
        } else if (msg.startsWith("OPPONENT_DISCONNECTED")) {
            System.out.println("# opponent has disconnected, no more actions are possible");
            manager.interruptGame();
            rg.renderBoard();
        } else if (msg.startsWith("MOVE PASS")) {
            System.out.println("# opponent has passed his turn");
            //noinspection AssertWithSideEffects
            assert (manager.registerMove(new GameplayManager.Pass(myColor.opposite))).isEmpty();
            rg.renderBoard();
        } else if (msg.startsWith("MOVE ")) {
            String[] parts = msg.split(" ");
            assert parts.length == 3;
            int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]);
            System.out.println("# opponent's move: " + x + ", " + y);
            //noinspection AssertWithSideEffects
            Optional<ReasonMoveImpossible> reason = manager.registerMove(new GameplayManager.StonePlacement(myColor.opposite, x, y));
            assert reason.isEmpty();
            rg.renderBoard();
        } else if(msg.equals("lobbyJoined"))  {
                try {
                    rg.returnToLobby();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

            System.out.println("UNRECOGNIZED MSG: " + msg);
            // TODO handle lobbyJoined
        }
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
