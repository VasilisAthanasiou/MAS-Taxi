package mastutils;

import java.util.ArrayList;

public class Node {
    String location;
    float sGlobal;
    float sLocal;
    ArrayList<Node> neighbours;
    Node parent;

    public Node(String loc, ArrayList<Node> nb, Node par){
        location = loc;
        sGlobal = Float.MAX_VALUE;
        sLocal = Float.MAX_VALUE;
        neighbours = nb; // CAREFUL with assigning arraylists
        parent = par;
    }

}
