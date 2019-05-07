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

    private GameplayManager manager = new GameplayManager();
    private boolean gameInterrupted = false;

    RoomListener(String name, LobbyListener lobby) {
        clients = new LinkedList<>();
        this.name = name;
        this.lobby = lobby;
    }

    /**
     * @return nazwa pokoju
     * TODO ona chyba nie powinna być dowolna, może coś bez spacji czy coś?
     */
    @SuppressWarnings("WeakerAccess")
    public String getName() { return name; }

    /**
     * @return początkowo w projekcie miały być różne gry, nie wiadomo, czy tak będzie, ale warto się zabezpieczyć
     */
    public String getGameType() { return "Go"; }

    /**
     * @return Rozmiar planszy do Go w rozgrywce
     */
    public int getBoardSize() { return manager.getBoard().getSize(); }

    /**
     * @return może się zdarzyć, że klient i serwer mają różne implementacje zasad gry
     * niekompatybilne pokoje nie powinny być widoczne dla klienta
     */
    public String gameEngineVersion() { return manager.getEngineVersion(); }

    public enum RoomState {
        GameInProgress,
        GameInterrupted,
        GameFinished,
        EmptyRoom,
        WaitingForBlack, // TODO późniejsza wersja może pozwolić na wybór koloru
        WaitingForWhite;
    }

    public RoomState getRoomState() {
        // kolejność sprawdzania jest ważna
        if (gameInterrupted) return RoomState.GameInterrupted;
        if (manager.finished()) return RoomState.GameFinished;
        if (clients.size() == 0) return RoomState.EmptyRoom;
        if (clients.size() == 1) return RoomState.WaitingForWhite; // TODO wybór koloru
        if (manager.inProgress()) return RoomState.GameInProgress;
        else {
            assert false;
            return RoomState.EmptyRoom; // z jakiegoś powodu Java nie pozwala na kończenie metody assert false
        }
    }

    /**
     * @return TODO Późniejsza wersja będzie to implementowała
     */
    public int numberOfSpectators() { return 0; }

    /**
     * @return TODO Późniejsza wersja będzie to implementowała
     */
    public int durationInSeconds() { return 0; }

    /**
     * Pokój nie akceptuje już żadnych graczy w stanie interrupted.
     * Pierwszy gracz w pokoju zawsze jest czasny (TODO)
     * Drugi gracz zawsze biały
     * Chwilowo pokoje wcale nie obsługują spectators (TODO)
     */
    @Override
    public synchronized boolean clientConnected(ServerClient client) {
        // TODO spectators
        if (clients.size() >= 2 || gameInterrupted || manager.finished()) {
            client.sendMessage(new LobbyListener.LobbyMsg(LobbyListener.LobbyMsg.Type.CONNECTION_REFUSED)); // -> ClientLobbyListener // TODO Reason
            return false;
        }

        // not interrupted and game in progress
        if (clients.size() == 0) {
            client.setListener(this);
            clients.add(client);
            assert getRoomState() == RoomState.WaitingForWhite;
            client.sendMessage(new LobbyListener.LobbyMsg.ConnectedMsg(Stone.Black)); // -> ClientLobbyListener
            lobby.broadcastRoomUpdate(); // !!!!!! uwaga na kolejność, roomState zależy od zawartości clients !!!
        } else if (clients.size() == 1) {
            client.setListener(this);
            clients.add(client);
            client.sendMessage(new LobbyListener.LobbyMsg.ConnectedMsg(Stone.White)); // -> ClientLobbyListener
            clients.get(0).sendMessage("OPPONENT_JOINED"); // -> ClientRoomListener
            assert getRoomState() == RoomState.GameInProgress;
            lobby.broadcastRoomUpdate();
        } else {
            assert false;
        }


        if (clients.size() >= 2) {
            clients.forEach(c -> c.sendMessage("GAME_BEGINS"));
        }

        return true;
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {
        clients.removeIf(c -> c == client);

        if (manager.inProgress() && !gameInterrupted) {
            gameInterrupted = true;
            for (ServerClient c : clients) {
                c.sendMessage("OPPONENT_DISCONNECTED");
                lobby.clientConnected(client);
                lobby.broadcastRoomUpdate();
            }
        }
    }

    @Override
    public synchronized void receivedInput(ServerClient client, Message message) {
        String msg = message.msg;

        if(msg.equals("quit")) {
            /// QUITTING

            clientDisconnected(client);
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

            if (manager.finished() || gameInterrupted) {
                client.sendMessage("MOVE_REJECTED");
            } else if (manager.inProgress()) {

                // parse move
                if (xs.length == 3) { // MOVE Player PASS
                    assert xs[2].equals("PASS");
                    move = new GameplayManager.Pass(color);
                } else { // MOVE Player 1 4
                    assert xs.length == 4;
                    int pos_x = Integer.parseInt(xs[2]), pos_y = Integer.parseInt(xs[3]);
                    move = new GameplayManager.StonePlacement(color, pos_x, pos_y);
                }

                Optional<ReasonMoveImpossible> result = manager.registerMove(move);
                if (result.isEmpty()) {
                    client.sendMessage("MOVE_ACCEPTED");

                    clients.forEach(c -> {
                        if (c != client) c.sendMessage(move.toString());
                    });

                    if (manager.finished()) {
                        GameplayManager.Result gameResult = manager.result();
                        clients.forEach(c -> c.sendMessage("GAME_FINISHED " + gameResult.winner + " "
                                + gameResult.blackPoints + " " + gameResult.whitePoints));
                    }
                } else {
                    client.sendMessage("MOVE_REJECTED"); // TODO send reason
                }
            }
        } else if(msg.startsWith("chat")) {
            clients.forEach(c -> {
                if (c != client) c.sendMessage(msg);
            });
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
    public String toString() { return "[Room " + name + "]"; }
}
