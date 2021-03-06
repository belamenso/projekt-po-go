package client;

import go.Board;
import go.GameplayManager;
import go.Stone;
import javafx.application.Platform;
import shared.LobbyMsg;
import shared.Message;
import shared.RoomData;
import shared.RoomEvent;

import java.util.List;

/**
 * Listener obsulugujacy lobby po stronie gracza
 */
public class ClientLobbyListener implements ClientListener {
    private LobbyScene ls;
    private Client client;

    ClientLobbyListener(LobbyScene ls, Client client) {
        this.ls = ls;
        this.client = client;
        client.setListener(this);
    }

    @Override
    public void receivedInput(Message message) {
        String msg = message.msg;

        System.out.println("receivedInput >"+msg+"<");

        if(!(message instanceof LobbyMsg)) { System.out.println("Received incorrect message"); return; }

        LobbyMsg lobbyMsg = (LobbyMsg) message;

        switch (lobbyMsg.type) {
            case CONNECTED:
                Stone                       color = ((LobbyMsg.Connected) lobbyMsg).color;
                Board.BoardSize              size = ((LobbyMsg.Connected) lobbyMsg).size;
                List<GameplayManager.Move>  moves = ((LobbyMsg.Connected) lobbyMsg).moves;
                List<RoomEvent>            events = ((LobbyMsg.Connected) lobbyMsg).events;

                ls.moveToRoom(color, size, moves, events);
                break;

            case LISTING:
                List<RoomData> data = ((LobbyMsg.Listing) lobbyMsg).data;

                Platform.runLater(() -> ls.updateList(data));
                break;

            case CONNECTION_REFUSED:
                Platform.runLater(() -> ls.setMessage("Couldn't join the room"));
                break;

            case NAME_TAKEN:
                Platform.runLater(() -> ls.setMessage("Name taken"));
                break;

            case ROOM_NOT_FOUND:
                Platform.runLater(() -> ls.setMessage("Room not found"));
                break;

            default:
                System.out.println("Unsopported message: " + lobbyMsg.type.name());
        }
    }

    void sendCreateRequest(String roomName, Board.BoardSize size) {
        client.sendMessage(new LobbyMsg.Create(roomName, size)); // CREATE roomName -> LobbyListener
    }

    void sendJoinRoomRequest(String roomName, Stone color) {
        client.sendMessage(new LobbyMsg.Join(roomName, color)); // JOIN roomName -> LobbyListener
    }

    void sendSpectateRequest(String roomName) {
        client.sendMessage(new LobbyMsg.Spectate(roomName)); // SPECTATE roomName -> LobbyListener
    }

    void sendUpdateRequest() {
        client.sendMessage(new LobbyMsg(LobbyMsg.Type.LIST_REQUEST)); // LIST_REQUEST -> LobbyListener
    }

    @Override
    public void disconnected() {
        System.out.println("disconnected");
        ls.returnToConnecting();
    }

    @Override
    public void couldNotConnect() {}

    @Override
    public void connectedToServer() {}
}
