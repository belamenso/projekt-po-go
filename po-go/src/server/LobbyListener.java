package server;

import java.util.LinkedList;
import java.util.List;

/**
 * Podstawowy listener
 * Przyjmuje nowych klientow na serwerze
 * moze zwracac liste pokoi, tworzyc nowe pokoje
 * i laczyc klientow z istniejacymi
 */
public class LobbyListener implements ServerListener {
    private final List<ServerClient> clients;
    private final List<RoomListener> rooms;

    LobbyListener() {
        clients = new LinkedList<>();
        rooms = new LinkedList<>();
    }

    @Override
    public synchronized boolean clientConnected(ServerClient client) {
        System.out.println(client + " connected to lobby");
        clients.add(client);
        client.setListener(this);
        client.sendMessage("lobbyJoined");
        return true;
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        System.out.println(client + " dissconected from lobby");
        clients.remove(client);
    }

    /**
     * Obsluguje operacje:
     * list - zwraca liste pokoi
     * create [name]- tworzy nowy pokoj o danej nazwie
     * join [name] - laczy klienta z pokojem o danej nazwie
     * remove [name] - usuwa pokoj o danej nazwie wysylana tylko przez roomlistener
     *                  gdy skonczy sie w nim gra
     *
     * @param client
     * @param msg
     */
    @Override
    public synchronized void receivedInput(ServerClient client, String msg) {
        System.out.println("Lobby received " + msg + " from " + client);

        if(msg.equals("list")) {

            String message = "list";
            for(RoomListener room : rooms) {
                message = message + ";" + room.getName() + "," + room.getRoomState().name();
            }
            //System.out.println("listing: "  + message);
            client.sendMessage(message);

        } else if(msg.startsWith("create")) {
            if(msg.split(" ").length < 2) return;

            String name = msg.split(" ")[1];
            for(RoomListener room : rooms) {
                if(room.getName().equals(name)) {
                    client.sendMessage("nameTaken");
                    return;
                }
            }

            System.out.println("Adding room " + name);
            rooms.add(new RoomListener(name, this));

            for(ServerClient sc : clients) {
                sc.sendMessage("changeAccured");
            }

        } else if(msg.startsWith("join")) {

            String name = msg.split(" ")[1];
            for(RoomListener room : rooms) {
                if(room.getName().equals(name)) {
                    if(room.clientConnected(client))
                        clients.remove(client);
                    return;
                }
            }

            client.sendMessage("roomNotFound");

        } else if(msg.startsWith("remove")) {
            final String name = msg.split(" ")[1];
            rooms.removeIf(roomListener -> roomListener.getName().equals(name));
        }
    }

    @Override
    public synchronized void serverClosed() {
        System.out.println("Server closed for lobby");
    }

    @Override
    public String toString() { return "LOBBY"; }
}
