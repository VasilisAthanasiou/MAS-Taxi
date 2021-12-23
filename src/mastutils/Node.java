package mastutils;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
    String location;
    double sGlobal;
    double sLocal;
    public ArrayList<Node> neighbours;
    public ArrayList<Integer> edgeCost; // Cost to neighbour
    Node parent;
    public boolean isVisited;

    public Node(String loc){
        location = loc;
        neighbours = new ArrayList<Node>();
        edgeCost = new ArrayList<Integer>();
        sGlobal = Double.MAX_VALUE;
        sLocal = Double.MAX_VALUE;
        parent = null;
        isVisited = false;
    }

    public String getLocation(){
        return this.location;
    }

//    public ArrayList<Integer> getEdgeCost(){
//        return this.edgeCost;
//    }



}
