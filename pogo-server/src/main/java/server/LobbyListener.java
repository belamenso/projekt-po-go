package server;

import go.Stone;
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

    private LobbyMsg.Listing roomListing() {
        List<RoomData> data = new ArrayList<>();
        for(RoomListener room : rooms)
            data.add(new RoomData(room.getName(), room.getRoomState().name(), room.getBoardSize(), room.numberOfSpectators()));
        return new LobbyMsg.Listing(data);
    }

    public void broadcastRoomUpdate() {
        Message msg = roomListing();
        for (ServerClient c : clients) {
            if (c != null)
                c.sendMessage(msg);
        }
    }

    @Override
    public synchronized void receivedInput(ServerClient client, Message message) {
        String msg = message.msg;

        //System.out.println("Lobby received " + msg + " from " + client);

        if(!(message instanceof LobbyMsg)) { System.out.println("Unrecognied message " + message + " in " + this); return; }

        LobbyMsg lobbyMessage = (LobbyMsg) message;

        switch(lobbyMessage.type) {
            case LIST_REQUEST:
                client.sendMessage(roomListing());
                break;

            case CREATE:
                String          toCreate = ((LobbyMsg.Create) lobbyMessage).roomName;
                Board.BoardSize size     = ((LobbyMsg.Create) lobbyMessage).size;

                createRoom(toCreate, size, client);

                break;

            case JOIN:
                String toJoin = ((LobbyMsg.Join) lobbyMessage).roomName;
                Stone   color = ((LobbyMsg.Join) lobbyMessage).color;

                if(joinRoom(toJoin, color, client))
                    clients.remove(client);

                break;

            case SPECTATE:
                String toSpectate = ((LobbyMsg.Spectate) lobbyMessage).roomName;

                for(RoomListener room : rooms) {
                    if(room.getName().equals(toSpectate)) {
                        room.joinSpectator(client);
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
                System.out.println("Unsopported lobby message " + lobbyMessage.type.name() + " in " + this);
        }
    }

    boolean checkRoomAvailabe(String name, ServerClient client) {
        for(RoomListener room : rooms) {
            if(room.getName().equals(name)) {
                client.sendMessage(new LobbyMsg(LobbyMsg.Type.NAME_TAKEN));
                return false;
            }
        }
        return true;
    }

    synchronized void createRoom(String name, Board.BoardSize size, ServerClient client) {
        if(!checkRoomAvailabe(name, client)) return;

        System.out.println("Adding room " + name);
        rooms.add(new RoomListener(name, size, this));
        client.sendMessage(new LobbyMsg(LobbyMsg.Type.ROOM_CREATED));

        broadcastRoomUpdate();
    }

    synchronized void createForkRoom(String name, int turns, RoomListener room, ServerClient client) {
        if(!checkRoomAvailabe(name, client)) return;

        System.out.println("Creating fork of " + room + " after " + turns + " turns");
        rooms.add(new RoomListener(name, room, turns, this));
        client.sendMessage(new LobbyMsg(LobbyMsg.Type.ROOM_CREATED));

        broadcastRoomUpdate();
    }

    synchronized boolean joinRoom(String toJoin, Stone color, ServerClient client) {
        for(RoomListener room : rooms) {
            if (room.getName().equals(toJoin)) {
                return room.joinPlayer(client, color);
            }
        }

        client.sendMessage(new LobbyMsg(LobbyMsg.Type.ROOM_NOT_FOUND));
        return false;
    }

    @Override
    public synchronized void serverClosed() {
        //System.out.println("Server closed for lobby");
    }

    @Override
    public String toString() { return "[LOBBY]"; }
}
