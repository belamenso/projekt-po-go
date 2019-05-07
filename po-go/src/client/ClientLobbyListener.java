package client;

import go.Board;
import go.Stone;
import javafx.application.Platform;
import shared.LobbyMsg;
import shared.Message;
import shared.RoomData;

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
                Stone           color = ((LobbyMsg.ConnectedMsg) lobbyMsg).color;
                Board.BoardSize size  = ((LobbyMsg.ConnectedMsg) lobbyMsg).size;

                ls.moveToRoom(color, size);
                break;

            case LIST:
                List<RoomData> data = ((LobbyMsg.ListMsg) lobbyMsg).data;

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
        client.sendMessage(new LobbyMsg.CreateMessage(roomName, size)); // CREATE roomName -> LobbyListener
    }

    void sendJoinRoomRequest(String roomName) {
        client.sendMessage(new LobbyMsg.JoinMsg(roomName)); // JOIN roomName -> LobbyListener
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
