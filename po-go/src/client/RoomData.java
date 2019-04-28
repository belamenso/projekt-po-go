package client;

public class RoomData {
    String name, stan;

    public RoomData(String name, String stan) {
        this.name = name;
        this.stan = stan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }
}
