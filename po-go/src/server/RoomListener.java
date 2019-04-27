package server;

import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Listener obsugujacy pojedynczy pokoj
 * na ta chwile jedyne co robi to wysyla otrzymane wiadomosci do obywdu graczy
 * w finalnej wersji te wiadomosci beda ruchami graczy
 */
public class RoomListener implements ServerListener {
    private List<ServerClient> clients;
    private String name;
    private LobbyListener lobby;
    private go.GameplayManager gameManager = new GameplayManager();

    RoomListener(String name, LobbyListener lobby) {
        clients = new LinkedList<>();
        this.name = name;
        this.lobby = lobby;
    }

    public String getName() { return name; }

    @Override
    public synchronized boolean clientConnected(ServerClient client) {
        if (clients.size() >= 2) {
            client.sendMessage("CONNECTION_REFUSED");
            return false;
        }

        if (clients.size() == 0) {
            client.sendMessage("CONNECTED BLACK");
        } else if (clients.size() == 1) {
            client.sendMessage("CONNECTED WHITE");
            clients.get(0).sendMessage("OPPONENT_JOINED");
        } else {
            assert false;
        }

        client.setListener(this);
        clients.add(client);

        if (clients.size() >= 2)
            clients.forEach(c -> c.sendMessage("GAME_BEGINS"));

        return true;
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        clients.removeIf(c -> c == client);

        for(ServerClient c : clients) {
            c.sendMessage("OPPONENT_DISCONNECTED");
            lobby.clientConnected(client);
        }

        if(clients.isEmpty())
            lobby.receivedInput(null, "remove " + getName());
    }

    @Override
    public synchronized void receivedInput(ServerClient client, String msg) {

        if(msg.equals("quit")) {
            /// QUITTING

            clientDisconnected(client);
            client.sendMessage("exitedRoom");
            lobby.clientConnected(client);

        } else if (msg.startsWith("MOVE ")) {
            /// MOVES

            String[] xs = msg.split(" ");
            GameplayManager.Move move;

            if (clients.size() <= 1) {
                clients.forEach(c -> c.sendMessage("MOVE_REJECTED"));
                return;
            }

            Stone color = clients.get(0) == client ? Stone.Black : Stone.White; // TODO ugly

            if (gameManager.finished()) {
                client.sendMessage("MOVE_REJECTED");
            } else if (gameManager.inProgress()) {

                // parse move
                if (xs.length == 2) { // MOVE PASS
                    assert xs[1].equals("PASS");
                    move = new GameplayManager.Pass(color);
                } else { // MOVE 1 4
                    int pos_x = Integer.parseInt(xs[1]), pos_y = Integer.parseInt(xs[2]);
                    move = new GameplayManager.StonePlacement(color, pos_x, pos_y);
                }

                Optional<ReasonMoveImpossible> result = gameManager.registerMove(move);
                if (result.isEmpty()) {
                    client.sendMessage("MOVE_ACCEPTED");

                    // TODO really the same thing?
                    // TODO what if the client misses this message?
                    clients.forEach(c -> {
                        if (c != client) c.sendMessage(msg);
                    });

                    if (gameManager.finished()) {
                        GameplayManager.Result gameResult = gameManager.result();
                        clients.forEach(c -> c.sendMessage("GAME_FINISHED " + gameResult.winner + " "
                                                            + gameResult.blackPoints + " " + gameResult.whitePoints));
                    }
                } else {
                    client.sendMessage("MOVE_REJECTED"); // TODO send reason
                }
            }
        } else {
            // UNRECOGNIZED COMMAND

            String out = this + " received unrecognized <<" + msg + ">> from <<" + client + ">>";
            for (ServerClient c : clients)
                c.sendMessage(out);
        }
    }

    @Override
    public synchronized void serverClosed() {
        System.out.println("server closed");
    }

    @Override
    public String toString() { return "R " + name + "(" + clients.size() + "/2)"; }
}