package mastutils;

import java.io.Serializable;

public class Itinerary implements Serializable {
    String clientLocation;
    String clientDestination;

    public Itinerary(String clientLoc, String clientDest){
        clientLocation = clientLoc;
        clientDestination = clientDest;
    }

    public String getClientDestination() {
        return clientDestination;
    }

    public String getClientLocation() {
        return clientLocation;
    }
}
