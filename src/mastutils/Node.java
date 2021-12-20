package mastutils;

import java.util.ArrayList;

public class Node {
    String location;
    float sGlobal;
    float sLocal;
    public ArrayList<Node> neighbours;
    public ArrayList<Integer> edgeCost; // Cost to neighbour
    Node parent;

    public Node(String loc){
        location = loc;
        neighbours = new ArrayList<Node>();
        edgeCost = new ArrayList<Integer>();
        sGlobal = Float.MAX_VALUE;
        sLocal = Float.MAX_VALUE;
        parent = null;
    }

    public String getLocation(){
        return this.location;
    }

//    public ArrayList<Integer> getEdgeCost(){
//        return this.edgeCost;
//    }



}
