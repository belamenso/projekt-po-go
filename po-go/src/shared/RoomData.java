package shared;

import java.io.Serializable;

public class RoomData implements Serializable {
    public String name, state;
    public Integer size, spectatorNo;

    public RoomData(String name, String state, Integer size, Integer spectatorNo) {
        this.name        =  name;
        this.state       = state;
        this.size        =  size;
        this.spectatorNo = spectatorNo;
    }

    public String         getName() { return        name; }
    public String        getState() { return       state; }
    public Integer        getSize() { return        size; }
    public Integer getSpectatorNo() { return spectatorNo; }

    public void              setName(String   name) { this.name        =        name; }
    public void             setState(String  state) { this.state       =       state; }
    public void              setSize(Integer  size) { this.size        =        size; }
    public void setSpectatorNo(Integer spectatorNo) { this.spectatorNo = spectatorNo; }
}
