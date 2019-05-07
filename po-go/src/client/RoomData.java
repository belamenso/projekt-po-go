package client;

import java.io.Serializable;

public class RoomData implements Serializable {
    String name, state;

    public RoomData(String name, String state) {
        this.name  =  name;
        this.state = state;
    }

    public String  getName() { return  name; }
    public String getState() { return state; }

    public void  setName(String  name) { this.name  =  name; }
    public void setState(String state) { this.state = state; }
}
