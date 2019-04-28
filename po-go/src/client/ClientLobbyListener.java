package client;

import go.Stone;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener obsulugujacy lobby po stronie gracza
 * póki co wypisuje otrzymane wiadomosci
 * w finalnej wersji ma wyswietlac na gui liste pokoi
 * i inofrmacje o tym czy dalo sie polaczyc
 */
public class ClientLobbyListener implements ClientListener {
    private Client client;
    private LobbyScreen ls;

    ClientLobbyListener(LobbyScreen ls) {
        this.ls = ls;
        this.client = ls.client;
    }

    @Override
    public void unknownHost() {}

    @Override
    public void couldNotConnect() {}

    @Override
    public void receivedInput(String msg) {
        System.out.println("receivedInput >"+msg+"<");

        if(msg.startsWith("CONNECTED ")) {
            String[] parts = msg.split(" ");
            assert parts.length == 2;
            try {
                ls.moveToRoom(parts[1].equals("WHITE") ? Stone.White : Stone.Black);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(msg.startsWith("list")){
            List<RoomData> data = new ArrayList<>();
            String[] rooms = msg.split(";");
            for(int i = 1; i < rooms.length; ++ i) {
                String[] roomData = rooms[i].split(",");
                data.add(new RoomData(roomData[0], roomData[1]));
                Platform.runLater(() -> ls.updateList(data));
            }
        } else if(msg.equals("changeAccured")) {
            Platform.runLater(() -> ls.update());
        } else if(msg.equals("CONNECTION_REFUSED")) {
            Platform.runLater(() -> ls.setMessage("Nie udało się połączyć z pokojem"));
        }
    }

    @Override
    public void serverClosed() {
        System.out.println("serverClosed");
    }

    @Override
    public void disconnected() {
        System.out.println("disconnected");
        Platform.runLater(() -> {
            try {
                ls.returnToConnecting();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void connectedToServer() {}
}
