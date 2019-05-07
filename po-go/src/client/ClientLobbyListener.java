package client;

import go.Stone;
import javafx.application.Platform;
import server.LobbyListener;
import server.Message;

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

        if(!(message instanceof LobbyListener.LobbyMsg)) { System.out.println("Received incorrect message"); return; }

        LobbyListener.LobbyMsg lobbyMsg = (LobbyListener.LobbyMsg) message;

        switch (lobbyMsg.type) {
            case CONNECTED:
                Stone color = ((LobbyListener.LobbyMsg.ConnectedMsg) lobbyMsg).color;

                ls.moveToRoom(color);
                break;

            case LIST:
                List<RoomData> data = ((LobbyListener.LobbyMsg.ListMsg) lobbyMsg).data;

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

    void sendCreateRequest(String roomName) {
        client.sendMessage(new LobbyListener.LobbyMsg.CreateMessage(roomName)); // CREATE roomName -> LobbyListener
    }

    void sendJoinRoomRequest(String roomName) {
        client.sendMessage(new LobbyListener.LobbyMsg.JoinMsg(roomName)); // JOIN roomName -> LobbyListener
    }

    void sendUpdateRequest() {
        client.sendMessage(new LobbyListener.LobbyMsg(LobbyListener.LobbyMsg.Type.LIST_REQUEST)); // LIST_REQUEST -> LobbyListener
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
