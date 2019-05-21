package server;

import go.Board;
import go.GameplayManager;
import go.ReasonMoveImpossible;
import go.Stone;
import shared.LobbyMsg;
import shared.Message;
import shared.RoomEvent;
import shared.RoomMsg;

import java.util.Date;
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
    private List<ServerClient> spectators;
    private String name;
    private LobbyListener lobby;

    private GameplayManager manager;
    private boolean gameInterrupted = false;

    private List<RoomEvent> events;
    private Date start;

    RoomListener(String name, Board.BoardSize size, LobbyListener lobby) {
        this.name = name;
        this.lobby = lobby;
        start = null;
        events = new LinkedList<>();
        clients = new LinkedList<>();
        spectators = new LinkedList<>();
        manager = new GameplayManager(size, 6.5);
    }

    /**
     * @return nazwa pokoju
     */
    public String getName() { return name; }

    /**
     * @return początkowo w projekcie miały być różne gry, nie wiadomo, czy tak będzie, ale warto się zabezpieczyć
     */
    public String getGameType() { return "Go"; }

    /**
     * @return Rozmiar planszy do Go w rozgrywce
     */
    int getBoardSize() { return manager.getBoard().getSize(); }

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

    public synchronized RoomState getRoomState() {
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
     * @return Aktualna liczba spectatorów
     */
    public synchronized int numberOfSpectators() { return spectators.size(); }

    /**
     * @return TODO Późniejsza wersja będzie to implementowała
     */
    public synchronized int durationInSeconds() { return 0; }



    private synchronized void registerEvent(String event) {
        RoomEvent toAdd;
        if (start == null) {
            events.add(toAdd = new RoomEvent(event, "", manager.getHistorySize()));
        } else {
            long interval = (new Date().getTime() - start.getTime()) / 1000;
            String seconds = Long.toString(interval % 60);
            if (seconds.length() == 1) seconds = "0" + seconds;
            String timestamp = interval / 60 + ":" + seconds;
            events.add(toAdd = new RoomEvent(event, timestamp, manager.getHistorySize()));
        }

        clients.forEach(c -> c.sendMessage(new RoomMsg.AddEvent(toAdd)));
        spectators.forEach(s -> s.sendMessage(new RoomMsg.AddEvent(toAdd)));
    }

    /**
     * Pokój nie akceptuje już żadnych graczy w stanie interrupted.
     * Pierwszy gracz w pokoju zawsze jest czasny (TODO)
     * Drugi gracz zawsze biały
     * Chwilowo pokoje wcale nie obsługują spectators (TODO)
     */
    @Override
    public synchronized boolean clientConnected(ServerClient client) {
        if (clients.size() >= 2 || gameInterrupted || manager.finished()) {
            client.sendMessage(new LobbyMsg(LobbyMsg.Type.CONNECTION_REFUSED)); // -> ClientLobbyListener // TODO Reason
            return false;
        }

        // not interrupted and game in progress
        if (clients.size() == 0) {
            client.setListener(this);
            clients.add(client);

            client.sendMessage(new LobbyMsg.Connected(Stone.Black, manager.getSize())); // -> ClientLobbyListener

            registerEvent(Stone.Black.pictogram + " joined the game");

            assert getRoomState() == RoomState.WaitingForWhite;
        } else if (clients.size() == 1) {
            client.setListener(this);
            clients.add(client);
            client.sendMessage(new LobbyMsg.Connected(Stone.White, manager.getSize())); // -> ClientLobbyListener
            clients.get(0).sendMessage(new RoomMsg(RoomMsg.Type.OPPONENT_JOINED)); // -> ClientRoomListener

            registerEvent(Stone.White.pictogram + " joined the game");

            assert getRoomState() == RoomState.GameInProgress;
        } else {
            assert false;
        }

        lobby.broadcastRoomUpdate();


        if (clients.size() >= 2) {
            clients.forEach(c -> c.sendMessage(new RoomMsg(RoomMsg.Type.GAME_BEGINS)));
            spectators.forEach(s -> s.sendMessage(new RoomMsg(RoomMsg.Type.GAME_BEGINS)));

            start = new Date();
            registerEvent("Game begins!");
        }

        return true;
    }

    public synchronized void joinSpectator(ServerClient client) {
        client.setListener(this);
        spectators.add(client);

        System.out.println(client + " joined " + this + " sending " +
                manager.getMoveHistory().size() + " of " + manager.getHistorySize() + " movess, and " +
                events.size() + " events");

        client.sendMessage(new LobbyMsg.ConnectedSpectator(manager.getMoveHistory(), events, manager.getSize()));
    }

    @Override
    public synchronized void clientDisconnected(ServerClient client) {


        if(clients.contains(client)) {
            Stone color = client == clients.get(0) ? Stone.White : Stone.Black;

            if (manager.inProgress() && !gameInterrupted) {
                gameInterrupted = true;
                for (ServerClient c : clients)
                    c.sendMessage(new RoomMsg(RoomMsg.Type.OPPONENT_DISCONNECTED));
            }

            registerEvent(color.pictogram + " left the game");

            lobby.broadcastRoomUpdate();
        } else {
            spectators.remove(client);
        }
    }

    @Override
    public synchronized void receivedInput(ServerClient client, Message message) {
        if(!(message instanceof RoomMsg)) { System.out.println("Unrecognized message in " + this); return; }

        RoomMsg roomMsg = (RoomMsg) message;

        switch(roomMsg.type) {
            case QUIT:
                clientDisconnected(client);
                lobby.clientConnected(client);
                break;

            case MOVE:
                if (clients.size() <= 1) {
                    clients.forEach(c -> c.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_REJECTED)));
                    return;
                }

                GameplayManager.Move move = ((RoomMsg.Move) roomMsg).move;

                if (manager.finished() || gameInterrupted) {
                    client.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_REJECTED));
                } else if (manager.inProgress()) {

                    Optional<ReasonMoveImpossible> result = manager.registerMove(move);
                    if (result.isEmpty()) {
                        client.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_ACCEPTED));

                        clients.forEach(c -> { if(c != client) c.sendMessage(message); });
                        spectators.forEach(s -> s.sendMessage(message));

                        if (manager.finished()) {
                            GameplayManager.Result r = manager.result();
                            clients.forEach(c -> c.sendMessage(new RoomMsg.GameFinished(r)));
                            spectators.forEach(s -> s.sendMessage(new RoomMsg.GameFinished(r)));

                            registerEvent("Game is finished: " + manager.result().winner + " won!");
                        }

                        if(move instanceof GameplayManager.Pass) {
                            registerEvent(move.player.pictogram + " has passed their turn");
                        } else {
                            registerEvent(move.player.pictogram + " places stone at " +
                                    manager.getBoard().positionToNumeral(((GameplayManager.StonePlacement) move).position));
                        }
                    } else {
                        client.sendMessage(new RoomMsg(RoomMsg.Type.MOVE_REJECTED));
                    }
                }
                break;

            case CHAT:
                RoomMsg.Chat msg = (RoomMsg.Chat) message;
                registerEvent("(" + msg.player.pictogram + "): " + msg.msg);

            default:
                System.out.println("Unrecognized message: " + message.msg);
        }
    }

    @Override
    public synchronized void serverClosed() {
        System.out.println("server closed");
    }

    @Override
    public String toString() { return "[Room " + name + "]"; }
}
