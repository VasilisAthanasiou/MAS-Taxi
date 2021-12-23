package mastutils;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
    String location;
    double sGlobal;
    double sLocal;
    public ArrayList<Node> neighbours;
    public ArrayList<Double> edgeCost; // Cost to neighbour
    Node parent;
    public boolean isVisited;

    public Node(String loc){
        location = loc;
        neighbours = new ArrayList<Node>();
        edgeCost = new ArrayList<Double>();
        sGlobal = 10000.;
        sLocal = 10000.;
        parent = null;
        isVisited = false;
    }

    public String getLocation(){
        return this.location;
    }

//    public ArrayList<Integer> getEdgeCost(){
//        return this.edgeCost;
//    }
    public void setsGlobal(double value){
        sGlobal = value;
    }



}
