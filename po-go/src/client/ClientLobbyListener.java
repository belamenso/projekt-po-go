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
    private LobbyScreen ls;
    private Client client;

    ClientLobbyListener(LobbyScreen ls, Client client) {
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
                Stone           color = ((LobbyMsg.Connected) lobbyMsg).color;
                Board.BoardSize size  = ((LobbyMsg.Connected) lobbyMsg).size;

                ls.moveToRoom(color, size);
                break;

            case CONNECTED_SPECTATOR:
                Board.BoardSize             bsize = ((LobbyMsg.ConnectedSpectator) lobbyMsg).size;
                List<GameplayManager.Move>  moves = ((LobbyMsg.ConnectedSpectator) lobbyMsg).moves;
                List<RoomEvent>            events = ((LobbyMsg.ConnectedSpectator) lobbyMsg).events;

                ls.moveToRoomSpectator(bsize, moves, events);
                break;

            case LISTING:
                List<RoomData> data = ((LobbyMsg.Listing) lobbyMsg).data;

                Platform.runLater(() -> ls.updateList(data));
                break;

            case CONNECTION_REFUSED:
                Platform.runLater(() -> ls.setMessage("Nie udało się połączyć z pokojem"));
                break;

            case NAME_TAKEN:
                Platform.runLater(() -> ls.setMessage("Nazwa zajęta"));
                break;

            case ROOM_NOT_FOUND:
                Platform.runLater(() -> ls.setMessage("Nie znalezino pokoju"));
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
