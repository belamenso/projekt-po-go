package client;

import java.io.Serializable;

public class RoomData implements Serializable {
    String name, state;
    Integer size;

    public RoomData(String name, String state, Integer size) {
        this.name  =  name;
        this.state = state;
        this.size  =  size;
    }

    public String   getName() { return  name; }
    public String  getState() { return state; }
    public Integer  getSize() { return  size; }

    public void  setName(String   name) { this.name  =  name; }
    public void setState(String  state) { this.state = state; }
    public void  setSize(Integer  size) { this.size  =  size; }
}
