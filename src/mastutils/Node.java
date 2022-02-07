package mastutils;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable{
    String location;
    double sGlobal;
    double sLocal;
    public ArrayList<Node> neighbours;
    public ArrayList<Double> edgeCost; // Cost to neighbour
    Node parent;
    public boolean isVisited;

    public Node(String loc){
        this.location = loc;
        this.neighbours = new ArrayList<Node>();
        this.edgeCost = new ArrayList<Double>();
        this.sGlobal = 10000.;
        this.sLocal = 10000.;
        this.parent = null;
        this.isVisited = false;
    }


    public String getLocation(){
        return this.location;
    }

    public void setsGlobal(double value){
        sGlobal = value;
    }



}
