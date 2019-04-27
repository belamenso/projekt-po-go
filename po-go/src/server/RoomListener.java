package server;

import java.util.LinkedList;
import java.util.List;

/**
 * Listener obsugujacy pojedynczy pokoj
 * na ta chwile jedyne co robi to wysyla otrzymane wiadomosci do obywdu graczy
 * w finalnej wersji te wiadomosci beda ruchami graczy
 */
public class RoomListener implements ServerListener {
    private List<ServerClient> clients;
    private String name;
    private LobbyListener lobby;

    RoomListener(String name, LobbyListener lobby) {
        clients = new LinkedList<>();
        this.name = name;
        this.lobby = lobby;
    }

    public String getName() { return name; }

    @Override
    public synchronized boolean clientConnected(ServerClient client) {
        if(clients.size() == 2) {
            client.sendMessage("roomFull");
            return false;
        }
        for(ServerClient c : clients) {
            c.sendMessage("opponentJoined " + client);
        }
        clients.add(client);
        client.setListener(this);
        client.sendMessage("connectedToRoom " + this);

        if(clients.size() == 2) {
            clients.forEach(serverClient -> serverClient.sendMessage("gameBegin"));
        }

        return true;
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        clients.removeIf(serverClient -> serverClient == client);

        for(ServerClient c : clients) {
            c.sendMessage("opponentDisconnected");
            lobby.clientConnected(client);
        }

        if(clients.size() == 0)
            lobby.receivedInput(null, "remove " + getName());
    }

    @Override
    public synchronized void receivedInput(ServerClient client, String msg) {
        String out = this + " received " + msg + " from " + client;
        System.out.println(out);
        if(msg.equals("quit")) {
            clientDisconnected(client);
            client.sendMessage("exitedRoom");
            lobby.clientConnected(client);
        } else {
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
