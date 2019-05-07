package server;

import shared.LobbyMsg;
import shared.RoomData;
import go.Board;
import shared.Message;

import java.util.ArrayList;
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
        client.sendMessage(new LobbyMsg(LobbyMsg.Type.LOBBY_JOINED));
        return true;
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        System.out.println(client + " dissconected from lobby");
        clients.remove(client);
    }

    private LobbyMsg.ListMsg roomListing() {
        List<RoomData> data = new ArrayList<>();
        for(RoomListener room : rooms)
            data.add(new RoomData(room.getName(), room.getRoomState().name(), room.getBoardSize()));
        return new LobbyMsg.ListMsg(data);
    }

    public void broadcastRoomUpdate() {
        Message msg = roomListing();
        for (ServerClient c : clients) {
            if (c == null) {
                System.out.println("dlaczego null tutaj?");
            } else {
                c.sendMessage(msg);
            }
        }
    }

    @Override
    public synchronized void receivedInput(ServerClient client, Message message) {
        String msg = message.msg;

        System.out.println("Lobby received " + msg + " from " + client);

        if(!(message instanceof LobbyMsg)) { System.out.println("Lobby received incorrect message"); return; }

        LobbyMsg lobbyMessage = (LobbyMsg) message;

        switch(lobbyMessage.type) {
            case LIST_REQUEST:
                client.sendMessage(roomListing());
                break;

            case CREATE:
                String          toCreate = ((LobbyMsg.CreateMessage) lobbyMessage).roomName;
                Board.BoardSize size     = ((LobbyMsg.CreateMessage) lobbyMessage).size;

                for(RoomListener room : rooms) {
                    if(room.getName().equals(toCreate)) {
                        client.sendMessage(new LobbyMsg(LobbyMsg.Type.NAME_TAKEN));
                        return;
                    }
                }

                System.out.println("Adding room " + toCreate);
                rooms.add(new RoomListener(toCreate, size, this));

                broadcastRoomUpdate();
                break;

            case JOIN:
                String toJoin = ((LobbyMsg.JoinMsg) lobbyMessage).roomName;

                for(RoomListener room : rooms) {
                    if(room.getName().equals(toJoin)) {
                        if(room.clientConnected(client))
                            clients.remove(client);
                        return;
                    }
                }

                client.sendMessage(new LobbyMsg(LobbyMsg.Type.ROOM_NOT_FOUND));
                break;

            case REMOVE:
                final String name = msg.split(" ")[1];

                if(rooms.removeIf(roomListener -> roomListener.getName().equals(name)))
                    broadcastRoomUpdate();
                break;

            default:
                System.out.println("Unsopported lobby message " + lobbyMessage.type.name());
        }
    }

    @Override
    public synchronized void serverClosed() {
        System.out.println("Server closed for lobby");
    }

    @Override
    public String toString() { return "LOBBY"; }
}
