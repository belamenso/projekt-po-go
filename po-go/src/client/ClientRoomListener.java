package client;

import go.GameplayManager;
import go.Stone;

/**
 * Listener obsugujacy pokoj po stronie gracza
 */
public class ClientRoomListener implements ClientListener {
    private Client client;
    private Stone myColor;
    private GameplayManager manager = new GameplayManager();

    ClientRoomListener(Client client, Stone color) {
        System.out.println("### ClientRoomListenerCreated");
        this.client = client;
        this.myColor = color;
    }

    @Override
    public void receivedInput(String msg) {
        if (msg.startsWith("exitedRoom")) {
            client.setListener(new ClientLobbyListener(client));
        } else if (msg.startsWith("GAME_BEGINS")) {

        } else if (msg.startsWith("GAME_FINISHED")) {
            String[] parts = msg.split(" ");
            System.out.println("Game finished, black points: " + parts[2] + " white: " + parts[3]);
            System.out.println(myColor.toString().equals(parts[1]) ? "WYGRANA!" : "PRZEGRANA");
        } else if (msg.startsWith("MOVE_ACCEPTED")) {
            System.out.println("# move was accepted");
        } else if (msg.startsWith("MOVE_REJECTED")) {
            System.out.println("# move was rejected"); // TODO reason
        } else if (msg.startsWith("OPPONENT_DISCONNECTED")) {
            System.out.println("# opponent has disconnected, no more actions are possible");
            manager.interruptGame();
        } else if (msg.startsWith("MOVE PASS")) {
            System.out.println("# opponent has passed his turn");
            assert (manager.registerMove(new GameplayManager.Pass(myColor.opposite))).isEmpty();
        } else if (msg.startsWith("MOVE ")) {
            String[] parts = msg.split(" ");
            assert parts.length == 3;
            int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]);
            System.out.println("# opponent's move: " + x + ", " + y);
            assert (manager.registerMove(new GameplayManager.StonePlacement(myColor.opposite, x, y))).isPresent();
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
