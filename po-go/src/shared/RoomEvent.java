package shared;

import java.io.Serializable;

public class RoomEvent implements Serializable {
    private String name, time;
    public int turnNumber;

    public RoomEvent(){ this.name = ""; this.time=""; turnNumber = 0; }
    public RoomEvent(String name, String time, int turnNumber){ this.name = name; this.time = time; this.turnNumber = turnNumber; }

    public String getName() { return name; }
    public String getTime() { return time; }

    public void setName(String name) { this.name = name; }
    public void setTime(String time) { this.time = time; }
}
